package sv.edu.itca.ticka.modelo;

import java.time.LocalDate;

/**
 * Filtros de los reportes del Administrador (RF-48): rango de fechas del
 * evento, evento, tipo, categoría y zona. Un valor null significa "todos".
 * El filtro de zona no aplica al reporte de incidencias (no tienen zona).
 */
public class FiltroReporte {

    private LocalDate desde;
    private LocalDate hasta;
    private Long idEvento;
    private TipoEvento tipoEvento;
    private Long idCategoria;
    private Long idZona;

    public LocalDate getDesde() { return desde; }
    public void setDesde(LocalDate desde) { this.desde = desde; }

    public LocalDate getHasta() { return hasta; }
    public void setHasta(LocalDate hasta) { this.hasta = hasta; }

    public Long getIdEvento() { return idEvento; }
    public void setIdEvento(Long idEvento) { this.idEvento = idEvento; }

    public TipoEvento getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(TipoEvento tipoEvento) { this.tipoEvento = tipoEvento; }

    public Long getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Long idCategoria) { this.idCategoria = idCategoria; }

    public Long getIdZona() { return idZona; }
    public void setIdZona(Long idZona) { this.idZona = idZona; }
}
