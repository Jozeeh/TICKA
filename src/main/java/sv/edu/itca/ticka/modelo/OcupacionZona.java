package sv.edu.itca.ticka.modelo;

import sv.edu.itca.ticka.util.Formatos;

/**
 * Conteo de asientos por estado de una zona en un evento. Se lee de la
 * vista v_ocupacion_evento_zona (dashboard, reportes y detalle del evento).
 */
public class OcupacionZona {

    private Long idEvento;
    private String evento;
    private String zona;
    private int total;
    private int vendidos;
    private int reservados;
    private int disponibles;
    private int bloqueados;

    public int getPorcentaje() {
        return Formatos.porcentaje(vendidos, total);
    }

    public Long getIdEvento() { return idEvento; }
    public void setIdEvento(Long idEvento) { this.idEvento = idEvento; }

    public String getEvento() { return evento; }
    public void setEvento(String evento) { this.evento = evento; }

    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getVendidos() { return vendidos; }
    public void setVendidos(int vendidos) { this.vendidos = vendidos; }

    public int getReservados() { return reservados; }
    public void setReservados(int reservados) { this.reservados = reservados; }

    public int getDisponibles() { return disponibles; }
    public void setDisponibles(int disponibles) { this.disponibles = disponibles; }

    public int getBloqueados() { return bloqueados; }
    public void setBloqueados(int bloqueados) { this.bloqueados = bloqueados; }
}
