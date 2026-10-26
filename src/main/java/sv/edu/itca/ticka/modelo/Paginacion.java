package sv.edu.itca.ticka.modelo;

/**
 * Datos de paginación de un listado (RF-55): página actual, total de
 * registros y cantidad de páginas. La página pedida se ajusta al rango válido.
 */
public class Paginacion {

    public static final int TAMANO_PAGINA = 10;

    private final int pagina;
    private final int total;
    private final int paginas;

    public Paginacion(Integer paginaPedida, int total) {
        this.total = total;
        this.paginas = Math.max(1, (int) Math.ceil(total / (double) TAMANO_PAGINA));
        int p = paginaPedida == null ? 1 : paginaPedida;
        this.pagina = Math.min(Math.max(1, p), paginas);
    }

    public int getDesplazamiento() {
        return (pagina - 1) * TAMANO_PAGINA;
    }

    public int getTamano() { return TAMANO_PAGINA; }
    public int getPagina() { return pagina; }
    public int getTotal() { return total; }
    public int getPaginas() { return paginas; }
}
