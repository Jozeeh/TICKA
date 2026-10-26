package sv.edu.itca.ticka.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

public class JsonTest {

    @Test
    public void serieConservaElOrden() {
        Map<String, Number> datos = new LinkedHashMap<>();
        datos.put("Web", new BigDecimal("1620.00"));
        datos.put("Mostrador", 360);
        assertEquals("{\"etiquetas\":[\"Web\",\"Mostrador\"],\"valores\":[1620.00,360]}", Json.serie(datos));
    }

    @Test
    public void escapaComillasYEtiquetasHtml() {
        String json = Json.texto("Evento \"especial\" </script>");
        assertEquals("\"Evento \\\"especial\\\" \\u003c/script\\u003e\"", json);
        assertFalse(json.contains("</script>"));
    }
}
