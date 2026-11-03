package sv.edu.itca.ticka.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

/** Códigos de boleto (RF-20) y su imagen QR. */
public class GeneradorCodigoTest {

    @Test
    public void formatoCortoTkMasCuatroCaracteres() {
        for (int i = 0; i < 200; i++) {
            String codigo = GeneradorCodigo.nuevo();
            assertTrue(codigo, codigo.matches("^TK-[A-HJ-NP-Z2-9]{4}$"));
        }
    }

    @Test
    public void noUsaCaracteresQueSeConfunden() {
        for (char c : "01IO".toCharArray()) {
            assertTrue(GeneradorCodigo.ALFABETO.indexOf(c) < 0);
        }
        assertEquals(32, GeneradorCodigo.ALFABETO.length());
    }

    @Test
    public void sonAleatorios() {
        Set<String> codigos = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            codigos.add(GeneradorCodigo.nuevo());
        }
        // En 100 códigos de un millón de combinaciones casi nunca hay repetidos.
        assertTrue(codigos.size() >= 98);
    }

    @Test
    public void normalizaLoQueEscribeElPersonal() {
        assertEquals("TK-AB2C", GeneradorCodigo.normalizar("  tk-ab2c "));
    }

    @Test
    public void generaUnaImagenPngDelQr() {
        byte[] png = GeneradorQr.png(GeneradorCodigo.nuevo(), 200);
        assertTrue(png.length > 100);
        assertEquals((byte) 0x89, png[0]);
        assertEquals((byte) 'P', png[1]);
    }
}
