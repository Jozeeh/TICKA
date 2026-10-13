package sv.edu.itca.ticka.servicio;

import java.time.LocalDateTime;
import sv.edu.itca.ticka.modelo.Evento;

/**
 * Validaciones de datos de un evento que no necesitan la base de datos
 * (RF-06). Se prueban con JUnit en ValidadorEventoTest.
 */
public final class ValidadorEvento {

    private ValidadorEvento() {
    }

    /**
     * Devuelve el primer error encontrado o null si los datos son válidos.
     *
     * @param ahora   fecha y hora actual (se recibe para poder probarlo)
     * @param esNuevo los eventos nuevos deben iniciar en el futuro
     */
    public static String validar(Evento e, LocalDateTime ahora, boolean esNuevo) {
        if (e.getNombre() == null || e.getNombre().isBlank()) {
            return "El nombre del evento es obligatorio.";
        }
        if (e.getNombre().length() > 150) {
            return "El nombre no puede superar los 150 caracteres.";
        }
        if (e.getTipo() == null) {
            return "Seleccione el tipo de evento.";
        }
        if (e.getIdRecinto() == null) {
            return "Seleccione el recinto.";
        }
        if (e.getParticipantes() != null && e.getParticipantes().length() > 200) {
            return "Los participantes no pueden superar los 200 caracteres.";
        }
        if (e.getImagen() != null && e.getImagen().length() > 255) {
            return "La ruta de la imagen no puede superar los 255 caracteres.";
        }
        if (e.getAperturaPuertas() == null || e.getFechaInicio() == null || e.getFechaFin() == null) {
            return "La apertura de puertas, el inicio y el fin son obligatorios.";
        }
        if (!e.getFechaFin().isAfter(e.getFechaInicio())) {
            return "La fecha y hora de fin debe ser posterior al inicio.";
        }
        if (e.getAperturaPuertas().isAfter(e.getFechaInicio())) {
            return "La apertura de puertas no puede ser después del inicio del evento.";
        }
        if (esNuevo && !e.getFechaInicio().isAfter(ahora)) {
            return "La fecha de inicio debe ser futura.";
        }
        return null;
    }

    /**
     * Regla de choque de horario (RN-05): dos rangos [inicioA, finA) y
     * [inicioB, finB) se traslapan si cada uno empieza antes de que termine
     * el otro. Es la misma condición que usa EventoDAO.existeChoque en SQL.
     */
    public static boolean seTraslapan(LocalDateTime inicioA, LocalDateTime finA,
            LocalDateTime inicioB, LocalDateTime finB) {
        return inicioA.isBefore(finB) && inicioB.isBefore(finA);
    }
}
