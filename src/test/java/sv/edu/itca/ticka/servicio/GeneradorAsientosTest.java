package sv.edu.itca.ticka.servicio;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;
import sv.edu.itca.ticka.modelo.EstadoEvento;

/** Pruebas del cálculo de filas y del flujo de estados del evento. */
public class GeneradorAsientosTest {

    @Test
    public void deAaJGeneraDiezFilas() {
        assertEquals(10, GeneradorAsientos.calcularFilas("A", "J", 20).size());
        assertEquals(Arrays.asList("A", "B", "C"), GeneradorAsientos.calcularFilas("a", "c", 5));
    }

    @Test(expected = ServicioException.class)
    public void filaInicialDespuesDeLaFinalEsInvalida() {
        GeneradorAsientos.calcularFilas("J", "A", 10);
    }

    @Test(expected = ServicioException.class)
    public void filaQueNoEsLetraEsInvalida() {
        GeneradorAsientos.calcularFilas("1", "5", 10);
    }

    @Test(expected = ServicioException.class)
    public void masDeCincuentaPorFilaEsInvalido() {
        GeneradorAsientos.calcularFilas("A", "B", 51);
    }

    @Test(expected = ServicioException.class)
    public void masDeMilAsientosALaVezEsInvalido() {
        GeneradorAsientos.calcularFilas("A", "Z", 50);
    }

    @Test
    public void flujoDeEstadosManual() {
        assertEquals(EstadoEvento.EN_CURSO, EstadoEvento.PUBLICADO.siguienteManual());
        assertEquals(EstadoEvento.EN_CURSO, EstadoEvento.AGOTADO.siguienteManual());
        assertEquals(EstadoEvento.FINALIZADO, EstadoEvento.EN_CURSO.siguienteManual());
        assertEquals(null, EstadoEvento.FINALIZADO.siguienteManual());
        assertEquals(null, EstadoEvento.BORRADOR.siguienteManual());
    }
}
