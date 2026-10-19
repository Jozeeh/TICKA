package sv.edu.itca.ticka.modelo;

import java.math.BigDecimal;
import sv.edu.itca.ticka.util.Formatos;

/** Indicadores generales del dashboard del Administrador (RF-42). */
public class Indicadores {

    private int eventosActivos;
    private int boletosVendidos;
    private BigDecimal ingresos = BigDecimal.ZERO;
    /** Asientos vendidos / asientos totales de los eventos activos. */
    private int asientosVendidosActivos;
    private int asientosTotalesActivos;
    /** Ingresados / vendidos de los eventos finalizados. */
    private int ingresadosFinalizados;
    private int vendidosFinalizados;
    private int incidenciasAbiertas;
    private int clientesActivos;

    public int getOcupacion() {
        return Formatos.porcentaje(asientosVendidosActivos, asientosTotalesActivos);
    }

    public int getAsistencia() {
        return Formatos.porcentaje(ingresadosFinalizados, vendidosFinalizados);
    }

    public String getIngresosTexto() {
        return Formatos.dinero(ingresos);
    }

    public int getEventosActivos() { return eventosActivos; }
    public void setEventosActivos(int eventosActivos) { this.eventosActivos = eventosActivos; }

    public int getBoletosVendidos() { return boletosVendidos; }
    public void setBoletosVendidos(int boletosVendidos) { this.boletosVendidos = boletosVendidos; }

    public BigDecimal getIngresos() { return ingresos; }
    public void setIngresos(BigDecimal ingresos) { this.ingresos = ingresos == null ? BigDecimal.ZERO : ingresos; }

    public int getAsientosVendidosActivos() { return asientosVendidosActivos; }
    public void setAsientosVendidosActivos(int v) { this.asientosVendidosActivos = v; }

    public int getAsientosTotalesActivos() { return asientosTotalesActivos; }
    public void setAsientosTotalesActivos(int v) { this.asientosTotalesActivos = v; }

    public int getIngresadosFinalizados() { return ingresadosFinalizados; }
    public void setIngresadosFinalizados(int v) { this.ingresadosFinalizados = v; }

    public int getVendidosFinalizados() { return vendidosFinalizados; }
    public void setVendidosFinalizados(int v) { this.vendidosFinalizados = v; }

    public int getIncidenciasAbiertas() { return incidenciasAbiertas; }
    public void setIncidenciasAbiertas(int incidenciasAbiertas) { this.incidenciasAbiertas = incidenciasAbiertas; }

    public int getClientesActivos() { return clientesActivos; }
    public void setClientesActivos(int clientesActivos) { this.clientesActivos = clientesActivos; }
}
