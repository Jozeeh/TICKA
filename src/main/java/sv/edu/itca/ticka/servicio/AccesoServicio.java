package sv.edu.itca.ticka.servicio;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import sv.edu.itca.ticka.dao.AccesoDAO;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.EventoDAO;
import sv.edu.itca.ticka.dao.IncidenciaDAO;
import sv.edu.itca.ticka.modelo.BoletoPuerta;
import sv.edu.itca.ticka.modelo.Evento;
import sv.edu.itca.ticka.modelo.Incidencia;
import sv.edu.itca.ticka.modelo.RegistroAcceso;
import sv.edu.itca.ticka.modelo.ResultadoValidacion;
import sv.edu.itca.ticka.persistencia.ConexionBD;
import sv.edu.itca.ticka.util.Configuracion;
import sv.edu.itca.ticka.util.GeneradorCodigo;

/**
 * Validación de boletos en puerta (M4):
 * - Busca el boleto bloqueando su fila: si dos puertas leen el mismo código
 *   al mismo tiempo, la segunda espera y lo ve como usado (RN-07).
 * - Aplica ValidadorAcceso, registra el acceso (válido o rechazado, con
 *   puerta y usuario, RF-27) y marca el boleto como Usado.
 * - Los rechazos pueden generar una incidencia "Acceso denegado" (RF-35).
 * - Anula un ingreso registrado por error, con motivo (RF-29).
 */
public class AccesoServicio {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final AccesoDAO accesoDAO;
    private final EventoDAO eventoDAO;
    private final IncidenciaDAO incidenciaDAO;

    public AccesoServicio(AccesoDAO accesoDAO, EventoDAO eventoDAO, IncidenciaDAO incidenciaDAO) {
        this.accesoDAO = accesoDAO;
        this.eventoDAO = eventoDAO;
        this.incidenciaDAO = incidenciaDAO;
    }

    public ResultadoValidacion validar(String codigoLeido, long idEvento, String puerta, long idUsuario) {
        String codigo = GeneradorCodigo.normalizar(codigoLeido);
        if (codigo.isEmpty()) {
            throw new ServicioException("Escriba o escanee el código del boleto.");
        }
        String p = validarPuerta(puerta);
        Evento evento = eventoDAO.buscarPorId(idEvento)
                .orElseThrow(() -> new ServicioException("Seleccione un evento válido."));
        LocalDateTime ahora = LocalDateTime.now();

        try (Connection cn = ConexionBD.obtenerConexion()) {
            cn.setAutoCommit(false);
            try {
                BoletoPuerta boleto = accesoDAO.buscarPorCodigo(cn, codigo).orElse(null);
                String motivo = ValidadorAcceso.motivoRechazo(boleto, idEvento, evento.getAperturaPuertas(),
                        evento.getFechaFin(), ahora, Configuracion.reentrada());
                Long idEntrada = boleto == null ? null : boleto.getIdEntrada();
                accesoDAO.registrar(cn, idEvento, idEntrada, codigo, p, idUsuario, motivo == null, motivo);
                if (motivo == null) {
                    accesoDAO.cambiarEstadoEntrada(cn, boleto.getIdEntrada(), "Usada");
                } else if (Configuracion.incidenciaPorRechazo()) {
                    Incidencia i = new Incidencia();
                    i.setIdEvento(idEvento);
                    i.setTipo("Acceso denegado");
                    i.setGravedad("Baja");
                    i.setPuerta(p);
                    i.setIdUsuario(idUsuario);
                    i.setDescripcion("Rechazo automático en puerta. Código " + codigo + ": " + motivo + ".");
                    incidenciaDAO.insertar(cn, i);
                }
                cn.commit();
                return new ResultadoValidacion(motivo == null, motivo, codigo, boleto, ahora.format(HORA));
            } catch (ServicioException | DAOException ex) {
                cn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo validar el boleto", ex);
        }
    }

    /** Anula un ingreso válido registrado por error (RF-29); el boleto vuelve a quedar Emitido. */
    public void anularIngreso(long idAcceso, String motivo) {
        String m = motivo == null ? "" : motivo.trim();
        if (m.length() < 5 || m.length() > 200) {
            throw new ServicioException("Escriba el motivo de la anulación (de 5 a 200 caracteres).");
        }
        try (Connection cn = ConexionBD.obtenerConexion()) {
            cn.setAutoCommit(false);
            try {
                RegistroAcceso a = accesoDAO.buscarParaAnular(cn, idAcceso)
                        .orElseThrow(() -> new ServicioException("El ingreso no existe."));
                if (!a.isValido() || a.isAnulado()) {
                    throw new ServicioException("Solo se puede anular un ingreso válido que no esté anulado.");
                }
                accesoDAO.anular(cn, idAcceso, m);
                if (a.getIdEntrada() != null && accesoDAO.contarIngresosValidos(cn, a.getIdEntrada()) == 0) {
                    accesoDAO.cambiarEstadoEntrada(cn, a.getIdEntrada(), "Emitida");
                }
                cn.commit();
            } catch (ServicioException | DAOException ex) {
                cn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo anular el ingreso", ex);
        }
    }

    static String validarPuerta(String puerta) {
        String p = puerta == null ? "" : puerta.trim();
        if (p.isEmpty() || p.length() > 20) {
            throw new ServicioException("Escriba el nombre de la puerta (máximo 20 caracteres).");
        }
        return p;
    }
}
