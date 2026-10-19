package sv.edu.itca.ticka.servicio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;
import sv.edu.itca.ticka.dao.AsientoEventoDAO;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.EventoDAO;
import sv.edu.itca.ticka.dao.RecintoDAO;
import sv.edu.itca.ticka.modelo.EstadoEvento;
import sv.edu.itca.ticka.modelo.Evento;
import sv.edu.itca.ticka.modelo.Recinto;
import sv.edu.itca.ticka.persistencia.ConexionBD;

/**
 * Reglas de negocio de los eventos del Organizador:
 * - Guardar con validaciones y choque de horario en el recinto (RN-05, RN-06).
 * - Precios por zona (RF-05).
 * - Publicar: genera la disponibilidad de asientos en una transacción (RF-07).
 * - Cambiar estado y cancelar (RF-02, RN-09).
 * - Bloquear asientos (RF-08) y recalcular "Agotado" (RN-11).
 *
 * Las operaciones de varias sentencias usan una sola Connection con
 * commit/rollback: o se aplica todo o no se aplica nada.
 */
public class EventoServicio {

    private static final BigDecimal PRECIO_MAXIMO = new BigDecimal("99999.99");

    private final EventoDAO eventoDAO;
    private final RecintoDAO recintoDAO;
    private final AsientoEventoDAO asientoEventoDAO;

    public EventoServicio(EventoDAO eventoDAO, RecintoDAO recintoDAO, AsientoEventoDAO asientoEventoDAO) {
        this.eventoDAO = eventoDAO;
        this.recintoDAO = recintoDAO;
        this.asientoEventoDAO = asientoEventoDAO;
    }

    /** Registra o actualiza un evento. Devuelve su id. */
    public long guardar(Evento e, long idUsuario) {
        boolean esNuevo = e.getId() == null;
        String error = ValidadorEvento.validar(e, LocalDateTime.now(), esNuevo);
        if (error != null) {
            throw new ServicioException(error);
        }
        Recinto recinto = recintoDAO.buscarPorId(e.getIdRecinto())
                .orElseThrow(() -> new ServicioException("El recinto seleccionado no existe."));

        if (esNuevo) {
            if (!recinto.isActivo()) {
                throw new ServicioException("El recinto está desactivado; elija otro.");
            }
            validarChoque(e);
            e.setCreadoPor(idUsuario);
            return eventoDAO.insertar(e);
        }

        Evento actual = eventoDAO.buscarPorId(e.getId())
                .orElseThrow(() -> new ServicioException("El evento no existe."));
        if (!actual.getEstado().isEditable()) {
            throw new ServicioException("Un evento " + actual.getEstado().getBd().toLowerCase()
                    + " ya no se puede modificar.");
        }
        boolean cambiaRecinto = !actual.getIdRecinto().equals(e.getIdRecinto());
        if (cambiaRecinto && actual.getEstado() != EstadoEvento.BORRADOR) {
            throw new ServicioException("El recinto no se puede cambiar después de publicar el evento.");
        }
        boolean cambianFechas = !actual.getFechaInicio().equals(e.getFechaInicio())
                || !actual.getFechaFin().equals(e.getFechaFin());
        if ((cambiaRecinto || cambianFechas) && eventoDAO.tieneVentas(e.getId())) {
            throw new ServicioException("El evento ya tiene boletos vendidos: no se puede cambiar el recinto "
                    + "ni las fechas sin anular antes los boletos (RN-06).");
        }
        validarChoque(e);
        eventoDAO.actualizar(e);
        return e.getId();
    }

    private void validarChoque(Evento e) {
        if (eventoDAO.existeChoque(e.getIdRecinto(), e.getFechaInicio(), e.getFechaFin(), e.getId())) {
            throw new ServicioException("El recinto ya tiene otro evento en ese horario. "
                    + "Elija otra fecha u otro recinto (RN-05).");
        }
    }

    /** Verifica que cada precio esté entre $0.00 y $99,999.99 con dos decimales como máximo. */
    public static void validarPrecios(Map<Long, BigDecimal> precios) {
        for (BigDecimal precio : precios.values()) {
            if (precio.signum() < 0 || precio.compareTo(PRECIO_MAXIMO) > 0 || precio.scale() > 2) {
                throw new ServicioException("Cada precio debe estar entre $0.00 y $99,999.99, con dos decimales.");
            }
        }
    }

    /**
     * Guarda los precios por zona del evento (RF-05), que llegan en el mismo
     * formulario del evento. Si el recinto cambió, borra los precios de las
     * zonas del recinto anterior.
     */
    public void guardarPrecios(long idEvento, long idRecinto, Map<Long, BigDecimal> precios) {
        validarPrecios(precios);
        asientoEventoDAO.eliminarPreciosDeOtrosRecintos(idEvento, idRecinto);
        if (!precios.isEmpty()) {
            asientoEventoDAO.guardarPrecios(idEvento, precios);
        }
    }

    /**
     * Publica un evento en Borrador (RF-07): verifica que el recinto tenga
     * asientos, que todas sus zonas tengan precio y que no haya choque de
     * horario; luego copia los asientos al evento y cambia el estado.
     * Devuelve la cantidad de asientos habilitados para la venta.
     */
    public int publicar(long idEvento) {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            cn.setAutoCommit(false);
            try {
                Evento e = eventoDAO.buscarParaActualizar(cn, idEvento)
                        .orElseThrow(() -> new ServicioException("El evento no existe."));
                if (e.getEstado() != EstadoEvento.BORRADOR) {
                    throw new ServicioException("Solo se puede publicar un evento en Borrador.");
                }
                if (!e.getFechaInicio().isAfter(LocalDateTime.now())) {
                    throw new ServicioException("No se puede publicar un evento cuya fecha de inicio ya pasó.");
                }
                if (asientoEventoDAO.contarAsientosActivos(cn, e.getIdRecinto()) == 0) {
                    throw new ServicioException("El recinto no tiene asientos activos. Genere los asientos de sus zonas.");
                }
                int sinPrecio = asientoEventoDAO.contarZonasSinPrecio(cn, idEvento, e.getIdRecinto());
                if (sinPrecio > 0) {
                    throw new ServicioException("Faltan precios en " + sinPrecio
                            + (sinPrecio == 1 ? " zona" : " zonas") + ". Complete todos los precios antes de publicar.");
                }
                if (eventoDAO.existeChoque(e.getIdRecinto(), e.getFechaInicio(), e.getFechaFin(), e.getId())) {
                    throw new ServicioException("El recinto ya tiene otro evento en ese horario (RN-05).");
                }
                int asientos = asientoEventoDAO.generarDisponibilidad(cn, idEvento, e.getIdRecinto());
                eventoDAO.cambiarEstado(cn, idEvento, EstadoEvento.PUBLICADO);
                cn.commit();
                return asientos;
            } catch (ServicioException | DAOException ex) {
                cn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo publicar el evento", ex);
        }
    }

    /** Avanza al siguiente estado manual: Publicado/Agotado → En curso → Finalizado. */
    public EstadoEvento avanzarEstado(long idEvento) {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            cn.setAutoCommit(false);
            try {
                Evento e = eventoDAO.buscarParaActualizar(cn, idEvento)
                        .orElseThrow(() -> new ServicioException("El evento no existe."));
                EstadoEvento siguiente = e.getEstado().siguienteManual();
                if (siguiente == null) {
                    throw new ServicioException("El evento en estado " + e.getEstado().getBd()
                            + " no tiene un siguiente estado.");
                }
                eventoDAO.cambiarEstado(cn, idEvento, siguiente);
                cn.commit();
                return siguiente;
            } catch (ServicioException | DAOException ex) {
                cn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo cambiar el estado", ex);
        }
    }

    /**
     * Cancela el evento (RN-09): anula sus ventas y boletos (reembolso
     * simulado), libera los asientos y pasa el estado a Cancelado.
     * Devuelve la cantidad de boletos anulados.
     */
    public int cancelar(long idEvento) {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            cn.setAutoCommit(false);
            try {
                Evento e = eventoDAO.buscarParaActualizar(cn, idEvento)
                        .orElseThrow(() -> new ServicioException("El evento no existe."));
                if (!e.getEstado().isCancelable()) {
                    throw new ServicioException("Un evento " + e.getEstado().getBd().toLowerCase()
                            + " no se puede cancelar.");
                }
                int anulados = eventoDAO.anularVentas(cn, idEvento, "Evento cancelado");
                eventoDAO.cambiarEstado(cn, idEvento, EstadoEvento.CANCELADO);
                cn.commit();
                return anulados;
            } catch (ServicioException | DAOException ex) {
                cn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo cancelar el evento", ex);
        }
    }

    /**
     * Bloquea o desbloquea un asiento del evento (RF-08) y actualiza el
     * estado Publicado/Agotado según queden asientos libres (RN-11).
     */
    public void cambiarBloqueo(long idEvento, long idEventoAsiento, boolean bloquear) {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            cn.setAutoCommit(false);
            try {
                Evento e = eventoDAO.buscarParaActualizar(cn, idEvento)
                        .orElseThrow(() -> new ServicioException("El evento no existe."));
                if (e.getEstado() != EstadoEvento.PUBLICADO && e.getEstado() != EstadoEvento.AGOTADO) {
                    throw new ServicioException("Solo se pueden bloquear asientos de eventos publicados o agotados.");
                }
                if (!asientoEventoDAO.cambiarBloqueo(cn, idEvento, idEventoAsiento, bloquear)) {
                    throw new ServicioException(bloquear
                            ? "Solo se pueden bloquear asientos disponibles."
                            : "El asiento no estaba bloqueado.");
                }
                int libres = asientoEventoDAO.contarLibres(cn, idEvento);
                if (libres == 0 && e.getEstado() == EstadoEvento.PUBLICADO) {
                    eventoDAO.cambiarEstado(cn, idEvento, EstadoEvento.AGOTADO);
                } else if (libres > 0 && e.getEstado() == EstadoEvento.AGOTADO) {
                    eventoDAO.cambiarEstado(cn, idEvento, EstadoEvento.PUBLICADO);
                }
                cn.commit();
            } catch (ServicioException | DAOException ex) {
                cn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo cambiar el asiento", ex);
        }
    }
}
