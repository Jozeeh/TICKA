package sv.edu.itca.ticka.modelo;

import java.time.LocalDate;

/** Criterios de búsqueda y paginación del listado de eventos (RF-03). */
public class FiltroEvento {

    public static final int TAMANO_PAGINA = 10;

    private String texto;
    private TipoEvento tipo;
    private EstadoEvento estado;
    private Long idRecinto;
    private LocalDate desde;
    private LocalDate hasta;
    private int pagina = 1;

    public int getDesplazamiento() {
        return (pagina - 1) * TAMANO_PAGINA;
    }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public TipoEvento getTipo() { return tipo; }
    public void setTipo(TipoEvento tipo) { this.tipo = tipo; }

    public EstadoEvento getEstado() { return estado; }
    public void setEstado(EstadoEvento estado) { this.estado = estado; }

    public Long getIdRecinto() { return idRecinto; }
    public void setIdRecinto(Long idRecinto) { this.idRecinto = idRecinto; }

    public LocalDate getDesde() { return desde; }
    public void setDesde(LocalDate desde) { this.desde = desde; }

    public LocalDate getHasta() { return hasta; }
    public void setHasta(LocalDate hasta) { this.hasta = hasta; }

    public int getPagina() { return pagina; }
    public void setPagina(int pagina) { this.pagina = Math.max(1, pagina); }
}
