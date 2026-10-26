package sv.edu.itca.ticka.servicio;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import sv.edu.itca.ticka.modelo.DiaCalendario;
import sv.edu.itca.ticka.modelo.Evento;

/**
 * Arma la cuadrícula del calendario mensual (RF-50): semanas de lunes a
 * domingo que cubren todo el mes, con los eventos ubicados en su día.
 */
public final class GeneradorCalendario {

    private GeneradorCalendario() {
    }

    /** Primer día visible: el lunes de la semana en que empieza el mes. */
    public static LocalDate primerDiaVisible(YearMonth mes) {
        return mes.atDay(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /** Día siguiente al último visible: el lunes después de la semana en que termina el mes. */
    public static LocalDate finVisible(YearMonth mes) {
        return mes.atEndOfMonth().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).plusDays(1);
    }

    public static List<List<DiaCalendario>> semanas(YearMonth mes, List<Evento> eventos, LocalDate hoy) {
        List<List<DiaCalendario>> semanas = new ArrayList<>();
        LocalDate dia = primerDiaVisible(mes);
        LocalDate fin = finVisible(mes);
        while (dia.isBefore(fin)) {
            List<DiaCalendario> semana = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                DiaCalendario celda = new DiaCalendario(dia, YearMonth.from(dia).equals(mes), dia.equals(hoy));
                for (Evento e : eventos) {
                    if (e.getFechaInicio().toLocalDate().equals(dia)) {
                        celda.getEventos().add(e);
                    }
                }
                semana.add(celda);
                dia = dia.plusDays(1);
            }
            semanas.add(semana);
        }
        return semanas;
    }
}
