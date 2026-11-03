package sv.edu.itca.ticka.servicio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

/** Pago simulado: solo se exige que los datos estén escritos (RF-21). */
public class ValidadorPagoTest {

    private static Map<String, String> completo() {
        Map<String, String> m = new HashMap<>();
        m.put("nombre", "Ana López");
        m.put("correo", "ana@correo.test");
        m.put("pais", "El Salvador");
        m.put("titular", "ANA LOPEZ");
        m.put("numero", "4242 4242 4242 4242");
        m.put("vencimiento", "12/29");
        m.put("cvv", "123");
        return m;
    }

    @Test
    public void aceptaDatosCompletos() {
        assertNull(ValidadorPago.validar(completo()::get, true));
    }

    @Test
    public void noRevisaLongitudNiQueLaTarjetaExista() {
        Map<String, String> m = completo();
        m.put("numero", "1");
        m.put("vencimiento", "x");
        m.put("cvv", "9");
        assertNull(ValidadorPago.validar(m::get, true));
    }

    @Test
    public void exigeLosDatosYLosTerminos() {
        Map<String, String> m = completo();
        m.put("numero", "  ");
        assertEquals("Escriba el número de la tarjeta.", ValidadorPago.validar(m::get, true));
        assertTrue(ValidadorPago.validar(completo()::get, false).contains("términos"));
    }
}
