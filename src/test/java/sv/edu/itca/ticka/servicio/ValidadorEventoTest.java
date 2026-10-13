package sv.edu.itca.ticka.servicio;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import org.junit.Test;
import sv.edu.itca.ticka.modelo.Evento;
import sv.edu.itca.ticka.modelo.TipoEvento;

/** Pruebas de las validaciones de fechas y de la regla de choque de horario. */
public class ValidadorEventoTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 13, 12, 0);

    private static Evento eventoValido() {
        Evento e = new Evento();
        e.setNombre("Noche de Rock");
        e.setTipo(TipoEvento.CONCIERTO);
        e.setIdRecinto(1L);
        e.setAperturaPuertas(LocalDateTime.of(2026, 11, 20, 18, 30));
        e.setFechaInicio(LocalDateTime.of(2026, 11, 20, 20, 0));
        e.setFechaFin(LocalDateTime.of(2026, 11, 20, 23, 0));
        return e;
    }

    @Test
    public void eventoCompletoEsValido() {
        assertNull(ValidadorEvento.validar(eventoValido(), AHORA, true));
    }

    @Test
    public void finAntesDelInicioEsInvalido() {
        Evento e = eventoValido();
        e.setFechaFin(LocalDateTime.of(2026, 11, 20, 19, 0));
        assertNotNull(ValidadorEvento.validar(e, AHORA, true));
    }

    @Test
    public void aperturaDespuesDelInicioEsInvalida() {
        Evento e = eventoValido();
        e.setAperturaPuertas(LocalDateTime.of(2026, 11, 20, 21, 0));
        assertNotNull(ValidadorEvento.validar(e, AHORA, true));
    }

    @Test
    public void eventoNuevoEnElPasadoEsInvalidoPeroUnoExistenteSePuedeEditar() {
        Evento e = eventoValido();
        e.setAperturaPuertas(LocalDateTime.of(2026, 9, 1, 18, 0));
        e.setFechaInicio(LocalDateTime.of(2026, 9, 1, 19, 0));
        e.setFechaFin(LocalDateTime.of(2026, 9, 1, 22, 0));
        assertNotNull(ValidadorEvento.validar(e, AHORA, true));
        assertNull(ValidadorEvento.validar(e, AHORA, false));
    }

    @Test
    public void sinNombreTipoORecintoEsInvalido() {
        Evento sinNombre = eventoValido();
        sinNombre.setNombre(" ");
        Evento sinTipo = eventoValido();
        sinTipo.setTipo(null);
        Evento sinRecinto = eventoValido();
        sinRecinto.setIdRecinto(null);
        assertNotNull(ValidadorEvento.validar(sinNombre, AHORA, true));
        assertNotNull(ValidadorEvento.validar(sinTipo, AHORA, true));
        assertNotNull(ValidadorEvento.validar(sinRecinto, AHORA, true));
    }

    @Test
    public void horariosTraslapadosChocan() {
        LocalDateTime ocho = LocalDateTime.of(2026, 11, 20, 20, 0);
        LocalDateTime once = LocalDateTime.of(2026, 11, 20, 23, 0);
        // 21:00 a 22:00 está dentro de 20:00 a 23:00
        assertTrue(ValidadorEvento.seTraslapan(ocho, once, ocho.plusHours(1), ocho.plusHours(2)));
        // empieza antes y termina dentro
        assertTrue(ValidadorEvento.seTraslapan(ocho, once, ocho.minusHours(1), ocho.plusMinutes(30)));
    }

    @Test
    public void eventosSeguidosNoChocan() {
        LocalDateTime ocho = LocalDateTime.of(2026, 11, 20, 20, 0);
        LocalDateTime once = LocalDateTime.of(2026, 11, 20, 23, 0);
        // uno termina exactamente cuando empieza el otro
        assertFalse(ValidadorEvento.seTraslapan(ocho, once, once, once.plusHours(2)));
        assertFalse(ValidadorEvento.seTraslapan(ocho, once, ocho.minusHours(3), ocho));
    }
}
