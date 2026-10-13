package sv.edu.itca.ticka.servicio;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Pruebas del hash BCrypt. */
public class SeguridadClaveTest {

    @Test
    public void mismaClaveGeneraHashesDistintosPorElSalt() {
        assertNotEquals(SeguridadClave.generarHash("Ticka2026"), SeguridadClave.generarHash("Ticka2026"));
    }

    @Test
    public void verificaClaveCorrectaEIncorrecta() {
        String hash = SeguridadClave.generarHash("Ticka2026");
        assertTrue(SeguridadClave.verificar("Ticka2026", hash));
        assertFalse(SeguridadClave.verificar("ticka2026", hash));
    }

    @Test
    public void hashNoBcryptNuncaCoincide() {
        assertFalse(SeguridadClave.verificar("!", "!"));
        assertFalse(SeguridadClave.verificar("cualquiera", null));
    }
}
