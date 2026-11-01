package sv.edu.itca.ticka.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

/** Códigos de boleto (RF-20) y su imagen QR. */
public class GeneradorCodigoTest {

    @Test
    public void formatoTkMas32Hexadecimales() {
        String codigo = GeneradorCodigo.nuevo();
        assertTrue(codigo.matches("^TK-[0-9A-F]{32}$"));
        assertTrue(codigo.length() <= 40);
    }

    @Test
    public void milCodigosSinRepetirse() {
        Set<String> codigos = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            codigos.add(GeneradorCodigo.nuevo());
        }
        assertEquals(1000, codigos.size());
    }

    @Test
    public void normalizaLoQueEscribeElPersonal() {
        assertEquals("TK-ABC", GeneradorCodigo.normalizar("  tk-abc "));
    }

    @Test
    public void generaUnaImagenPngDelQr() {
        byte[] png = GeneradorQr.png(GeneradorCodigo.nuevo(), 200);
        assertTrue(png.length > 100);
        assertEquals((byte) 0x89, png[0]);
        assertEquals((byte) 'P', png[1]);
    }
}
