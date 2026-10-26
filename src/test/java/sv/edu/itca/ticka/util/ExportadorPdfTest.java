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
        r.indicador("Boletos", 12).setNota("Nota de prueba.");
        byte[] pdf = ExportadorPdf.generar(r, "RPT-VENTAS", "tipo Ópera", "21/10/2026 10:00", "Admin (Administrador)");
        assertEquals("%PDF-", inicio(pdf));
        assertTrue(pdf.length > 500);
    }

    @Test
    public void generaUnPdfAgrupadoPorSecciones() {
        Reporte r = new Reporte("Reporte general: Maná", "Sección", "Dato", "Valor");
        r.setAgrupado(true);
        r.agregarFila("Evento", "Nombre", "Maná");
        r.agregarFila("Asientos", "Vendidos", 26);
        byte[] pdf = ExportadorPdf.generar(r, "RPT-GENERAL", null, "21/10/2026 10:00", null);
        assertEquals("%PDF-", inicio(pdf));
    }

    @Test
    public void generaUnPdfAunqueNoHayaFilas() {
        Reporte r = new Reporte("Incidencias", "Fecha", "Evento");
        byte[] pdf = ExportadorPdf.generar(r, "RPT-INCIDENCIAS", null, "21/10/2026 10:00", null);
        assertEquals("%PDF-", inicio(pdf));
    }
}
