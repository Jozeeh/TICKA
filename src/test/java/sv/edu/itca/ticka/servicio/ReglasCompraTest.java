package sv.edu.itca.ticka.servicio;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import org.junit.Test;
import sv.edu.itca.ticka.modelo.EstadoEvento;
import sv.edu.itca.ticka.modelo.VentaAnulable;

/** Reglas de compra y anulación (RN-03, RN-04, RN-12). */
public class ReglasCompraTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 1, 10, 0);

    private static VentaAnulable venta(String estado, EstadoEvento evento, LocalDateTime apertura, int usados) {
        VentaAnulable v = new VentaAnulable();
        v.setEstado(estado);
        v.setEstadoEvento(evento);
        v.setAperturaPuertas(apertura);
        v.setBoletosUsados(usados);
        return v;
    }

    @Test
    public void soloSeVendeUnEventoPublicadoQueNoHaEmpezado() {
        assertNull(ReglasCompra.validarVentaAbierta(EstadoEvento.PUBLICADO, AHORA.plusDays(3), AHORA));
        assertTrue(ReglasCompra.validarVentaAbierta(EstadoEvento.AGOTADO, AHORA.plusDays(3), AHORA).contains("agotado"));
        assertNotNull(ReglasCompra.validarVentaAbierta(EstadoEvento.BORRADOR, AHORA.plusDays(3), AHORA));
        assertNotNull(ReglasCompra.validarVentaAbierta(EstadoEvento.PUBLICADO, AHORA.minusMinutes(1), AHORA));
    }

    @Test
    public void respetaElMaximoDeAsientosPorCompra() {
        assertNotNull(ReglasCompra.validarCantidad(0, 6));
        assertNull(ReglasCompra.validarCantidad(6, 6));
        assertTrue(ReglasCompra.validarCantidad(7, 6).contains("6"));
    }

    @Test
    public void anulaDentroDelPlazoSiNingunBoletoSeUso() {
        assertNull(ReglasCompra.motivoNoAnulable(
                venta("Pagada", EstadoEvento.PUBLICADO, AHORA.plusDays(2), 0), AHORA, 24));
    }

    @Test
    public void noAnulaFueraDePlazoNiConBoletosUsadosNiDosVeces() {
        assertNotNull(ReglasCompra.motivoNoAnulable(
                venta("Pagada", EstadoEvento.PUBLICADO, AHORA.plusHours(5), 0), AHORA, 24));
        assertNotNull(ReglasCompra.motivoNoAnulable(
                venta("Pagada", EstadoEvento.PUBLICADO, AHORA.plusDays(2), 1), AHORA, 24));
        assertNotNull(ReglasCompra.motivoNoAnulable(
                venta("Anulada", EstadoEvento.PUBLICADO, AHORA.plusDays(2), 0), AHORA, 24));
        assertNotNull(ReglasCompra.motivoNoAnulable(
                venta("Pagada", EstadoEvento.CANCELADO, AHORA.plusDays(2), 0), AHORA, 24));
    }
}
