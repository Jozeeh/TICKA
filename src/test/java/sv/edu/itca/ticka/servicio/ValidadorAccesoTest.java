package sv.edu.itca.ticka.servicio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import org.junit.Test;
import sv.edu.itca.ticka.modelo.BoletoPuerta;

/** Validación en puerta (RF-26, RF-30, RF-31, RN-07, RN-08) y transiciones de incidencias (RF-33). */
public class ValidadorAccesoTest {

    private static final LocalDateTime APERTURA = LocalDateTime.of(2026, 12, 21, 16, 0);
    private static final LocalDateTime FIN = LocalDateTime.of(2026, 12, 21, 22, 30);
    private static final LocalDateTime DURANTE = APERTURA.plusHours(2);

    private static BoletoPuerta boleto(long idEvento, String estado) {
        BoletoPuerta b = new BoletoPuerta();
        b.setIdEvento(idEvento);
        b.setEvento("Maná");
        b.setEstado(estado);
        return b;
    }

    @Test
    public void boletoEmitidoDelEventoEsValido() {
        assertNull(ValidadorAcceso.motivoRechazo(boleto(5, "Emitida"), 5, APERTURA, FIN, DURANTE, false));
    }

    @Test
    public void rechazaInexistenteAnuladoUsadoYDeOtroEvento() {
        assertEquals("Código inexistente", ValidadorAcceso.motivoRechazo(null, 5, APERTURA, FIN, DURANTE, false));
        assertEquals("Boleto anulado", ValidadorAcceso.motivoRechazo(boleto(5, "Anulada"), 5, APERTURA, FIN, DURANTE, false));
        assertEquals("Boleto ya utilizado", ValidadorAcceso.motivoRechazo(boleto(5, "Usada"), 5, APERTURA, FIN, DURANTE, false));
        assertTrue(ValidadorAcceso.motivoRechazo(boleto(2, "Emitida"), 5, APERTURA, FIN, DURANTE, false).startsWith("Boleto de otro evento"));
    }

    @Test
    public void soloValidaEntreAperturaYFin() {
        assertTrue(ValidadorAcceso.motivoRechazo(boleto(5, "Emitida"), 5, APERTURA, FIN, APERTURA.minusMinutes(1), false)
                .startsWith("Fuera de horario"));
        assertTrue(ValidadorAcceso.motivoRechazo(boleto(5, "Emitida"), 5, APERTURA, FIN, FIN.plusMinutes(1), false)
                .startsWith("Fuera de horario"));
    }

    @Test
    public void conReentradaUnBoletoUsadoVuelveAEntrar() {
        assertNull(ValidadorAcceso.motivoRechazo(boleto(5, "Usada"), 5, APERTURA, FIN, DURANTE, true));
    }

    @Test
    public void transicionesDeIncidencias() {
        assertTrue(IncidenciaServicio.transicionPermitida("Abierta", "En atencion"));
        assertTrue(IncidenciaServicio.transicionPermitida("Abierta", "Cerrada"));
        assertTrue(IncidenciaServicio.transicionPermitida("En atencion", "Cerrada"));
        assertFalse(IncidenciaServicio.transicionPermitida("Cerrada", "Abierta"));
        assertFalse(IncidenciaServicio.transicionPermitida("En atencion", "Abierta"));
    }
}
