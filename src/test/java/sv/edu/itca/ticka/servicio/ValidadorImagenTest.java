package sv.edu.itca.ticka.servicio;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Pruebas de la validación del afiche adjunto. */
public class ValidadorImagenTest {

    @Test
    public void detectaPngYJpgPorSuFirma() {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0};
        byte[] jpg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0};
        assertEquals("image/png", ValidadorImagen.validar(png));
        assertEquals("image/jpeg", ValidadorImagen.validar(jpg));
    }

    @Test(expected = ServicioException.class)
    public void archivoQueNoEsImagenSeRechazaAunqueSeLlameAfichePng() {
        ValidadorImagen.validar("<script>alert(1)</script>".getBytes());
    }

    @Test(expected = ServicioException.class)
    public void archivoDeMasDeDosMegasSeRechaza() {
        byte[] grande = new byte[ValidadorImagen.TAMANO_MAXIMO + 1];
        grande[0] = (byte) 0xFF;
        grande[1] = (byte) 0xD8;
        grande[2] = (byte) 0xFF;
        ValidadorImagen.validar(grande);
    }
}
