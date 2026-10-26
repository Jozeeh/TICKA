package sv.edu.itca.ticka.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Una celda del calendario mensual de eventos (RF-50). */
public class DiaCalendario {

    private final LocalDate fecha;
    /** false para los días del mes anterior o siguiente que completan la semana. */
    private final boolean delMes;
    private final boolean hoy;
    private final List<Evento> eventos = new ArrayList<>();

    public DiaCalendario(LocalDate fecha, boolean delMes, boolean hoy) {
        this.fecha = fecha;
        this.delMes = delMes;
        this.hoy = hoy;
    }

    public LocalDate getFecha() { return fecha; }
    public int getDia() { return fecha.getDayOfMonth(); }
    public boolean isDelMes() { return delMes; }
    public boolean isHoy() { return hoy; }
    public List<Evento> getEventos() { return eventos; }
}
