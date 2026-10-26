package sv.edu.itca.ticka.servicio;

import java.util.logging.Level;
import java.util.logging.Logger;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.EventoDAO;

/**
 * Finalización automática de eventos (RF-51). La ejecuta cada minuto el
 * AplicacionListener: los eventos que ya empezaron pasan a En curso, los que
 * ya terminaron a Finalizado, y las reservas vencidas se liberan (RN-02).
 *
 * Si MySQL no está disponible se registra el error y se intenta en la
 * siguiente vuelta; nunca se lanza la excepción, porque detendría la tarea.
 */
public class TareaEstadosEventos implements Runnable {

    private static final Logger LOG = Logger.getLogger(TareaEstadosEventos.class.getName());

    private final EventoDAO eventoDAO;

    public TareaEstadosEventos(EventoDAO eventoDAO) {
        this.eventoDAO = eventoDAO;
    }

    @Override
    public void run() {
        try {
            int[] r = eventoDAO.actualizarEstadosPorHorario();
            if (r[0] + r[1] + r[2] > 0) {
                LOG.info("Estados automáticos: " + r[0] + " en curso, " + r[1] + " finalizados, "
                        + r[2] + " reservas liberadas.");
            }
        } catch (DAOException | IllegalStateException ex) {
            LOG.log(Level.WARNING, "No se pudo actualizar los estados de los eventos: {0}", ex.getMessage());
        }
    }
}
