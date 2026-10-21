package sv.edu.itca.ticka.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import org.junit.Test;
import sv.edu.itca.ticka.modelo.Reporte;

public class ExportadorPdfTest {

    private static String inicio(byte[] pdf) {
        return new String(pdf, 0, 5, StandardCharsets.ISO_8859_1);
    }

    @Test
    public void generaUnPdfConDatosYTotales() {
        Reporte r = new Reporte("Ventas", "Evento", "Zona", "Boletos").numericas(2);
        r.agregarFila("Ópera de invierno", "Platea", 12);
        r.setTotales("Total", "", 12);
        byte[] pdf = ExportadorPdf.generar(r, "tipo Ópera", "21/10/2026 10:00");
        assertEquals("%PDF-", inicio(pdf));
        assertTrue(pdf.length > 500);
    }

    @Test
    public void generaUnPdfAunqueNoHayaFilas() {
        Reporte r = new Reporte("Incidencias", "Fecha", "Evento");
        byte[] pdf = ExportadorPdf.generar(r, null, "21/10/2026 10:00");
        assertEquals("%PDF-", inicio(pdf));
    }
}
