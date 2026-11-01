package sv.edu.itca.ticka.servicio;

import java.util.Arrays;
import sv.edu.itca.ticka.dao.EventoDAO;
import sv.edu.itca.ticka.dao.IncidenciaDAO;
import sv.edu.itca.ticka.modelo.Incidencia;

/**
 * Incidencias de los eventos (M5): registro con validaciones (RF-32) y
 * seguimiento por estado Abierta → En atención → Cerrada (RF-33).
 */
public class IncidenciaServicio {

    private final IncidenciaDAO incidenciaDAO;
    private final EventoDAO eventoDAO;

    public IncidenciaServicio(IncidenciaDAO incidenciaDAO, EventoDAO eventoDAO) {
        this.incidenciaDAO = incidenciaDAO;
        this.eventoDAO = eventoDAO;
    }

    public long registrar(Incidencia i) {
        if (i.getIdEvento() == null || eventoDAO.buscarPorId(i.getIdEvento()).isEmpty()) {
            throw new ServicioException("Seleccione el evento.");
        }
        if (Arrays.stream(Incidencia.TIPOS).noneMatch(t -> t[0].equals(i.getTipo()))) {
            throw new ServicioException("Seleccione el tipo de incidencia.");
        }
        if (!Arrays.asList(Incidencia.GRAVEDADES).contains(i.getGravedad())) {
            throw new ServicioException("Seleccione la gravedad.");
        }
        String d = i.getDescripcion() == null ? "" : i.getDescripcion().trim();
        if (d.length() < 5 || d.length() > 500) {
            throw new ServicioException("Describa la incidencia (de 5 a 500 caracteres).");
        }
        i.setDescripcion(d);
        String p = i.getPuerta() == null ? "" : i.getPuerta().trim();
        if (p.length() > 20) {
            throw new ServicioException("La puerta o ubicación no puede superar 20 caracteres.");
        }
        i.setPuerta(p.isEmpty() ? null : p);
        return incidenciaDAO.insertar(i);
    }

    /** Cambia el estado si la transición está permitida; al cerrar, las observaciones son obligatorias. */
    public void cambiarEstado(long id, String nuevoEstado, String observaciones) {
        Incidencia actual = incidenciaDAO.buscarPorId(id)
                .orElseThrow(() -> new ServicioException("La incidencia no existe."));
        if (!transicionPermitida(actual.getEstado(), nuevoEstado)) {
            throw new ServicioException("No se puede pasar de " + actual.getEstadoTexto() + " a ese estado.");
        }
        String obs = observaciones == null ? "" : observaciones.trim();
        if (obs.length() > 500) {
            throw new ServicioException("Las observaciones no pueden superar 500 caracteres.");
        }
        if ("Cerrada".equals(nuevoEstado) && obs.isEmpty()) {
            throw new ServicioException("Escriba cómo se resolvió la incidencia antes de cerrarla.");
        }
        incidenciaDAO.actualizarEstado(id, nuevoEstado, obs.isEmpty() ? actual.getObservaciones() : obs);
    }

    /** RF-33: Abierta → En atención o Cerrada; En atención → Cerrada; Cerrada no cambia. */
    public static boolean transicionPermitida(String actual, String nuevo) {
        if ("Abierta".equals(actual)) {
            return "En atencion".equals(nuevo) || "Cerrada".equals(nuevo);
        }
        if ("En atencion".equals(actual)) {
            return "Cerrada".equals(nuevo);
        }
        return false;
    }
}
