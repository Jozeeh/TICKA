package sv.edu.itca.ticka.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import sv.edu.itca.ticka.modelo.Reporte;

/** Pruebas de la exportación de reportes a CSV. */
public class ExportadorCsvTest {

    @Test
    public void generaEncabezadoFilasYTotalesConBom() {
        Reporte r = new Reporte("Ventas", "Evento", "Boletos");
        r.agregarFila("La Traviata", 33);
        r.setTotales("Total", 33);
        String csv = ExportadorCsv.generar(r);
        assertTrue(csv.startsWith(ExportadorCsv.BOM));
        assertEquals(ExportadorCsv.BOM + "\"Evento\",\"Boletos\"\r\n\"La Traviata\",\"33\"\r\n\"Total\",\"33\"\r\n", csv);
    }

    @Test
    public void escapaComillasYEvitaFormulas() {
        assertEquals("\"Concierto \"\"Acústico\"\"\"", ExportadorCsv.escapar("Concierto \"Acústico\""));
        assertEquals("\"'=SUMA(A1:A2)\"", ExportadorCsv.escapar("=SUMA(A1:A2)"));
    }
}
