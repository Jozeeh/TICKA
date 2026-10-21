package sv.edu.itca.ticka.util;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;
import sv.edu.itca.ticka.modelo.Reporte;

/**
 * Convierte un Reporte a PDF (RF-47) con OpenPDF: hoja carta horizontal,
 * título, descripción de los filtros y la tabla con sus totales. Usa la
 * misma estructura Reporte que la vista y el CSV.
 */
public final class ExportadorPdf {

    private static final Color AZUL = new Color(31, 58, 95);
    private static final Color GRIS = new Color(243, 245, 248);

    private ExportadorPdf() {
    }

    /**
     * Genera el PDF en memoria.
     *
     * @param filtros texto con los filtros aplicados (puede ser null)
     * @param generado fecha y hora de generación ya formateada
     */
    public static byte[] generar(Reporte r, String filtros, String generado) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.LETTER.rotate(), 30, 30, 30, 30);
        try {
            PdfWriter.getInstance(doc, salida);
            doc.addTitle(r.getTitulo());
            doc.open();

            Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, AZUL);
            Font fNota = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
            doc.add(new Paragraph("TICKA · " + r.getTitulo(), fTitulo));
            doc.add(new Paragraph("Generado: " + generado
                    + (filtros == null || filtros.isEmpty() ? "" : "   ·   Filtros: " + filtros), fNota));
            doc.add(new Paragraph(" ", fNota));

            PdfPTable tabla = new PdfPTable(r.getColumnas().size());
            tabla.setWidthPercentage(100);
            tabla.setHeaderRows(1);
            Font fEncabezado = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            for (int i = 0; i < r.getColumnas().size(); i++) {
                tabla.addCell(celda(r.getColumnas().get(i), fEncabezado, AZUL, r.esNumerica(i)));
            }
            Font fCelda = FontFactory.getFont(FontFactory.HELVETICA, 8.5f);
            boolean par = false;
            for (List<String> fila : r.getFilas()) {
                for (int i = 0; i < fila.size(); i++) {
                    tabla.addCell(celda(fila.get(i), fCelda, par ? GRIS : Color.WHITE, r.esNumerica(i)));
                }
                par = !par;
            }
            if (r.getFilas().isEmpty()) {
                PdfPCell vacia = celda("No hay datos con esos filtros.", fCelda, Color.WHITE, false);
                vacia.setColspan(r.getColumnas().size());
                vacia.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabla.addCell(vacia);
            } else if (r.getTotales() != null) {
                Font fTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
                for (int i = 0; i < r.getTotales().size(); i++) {
                    tabla.addCell(celda(r.getTotales().get(i), fTotal, GRIS, r.esNumerica(i)));
                }
            }
            doc.add(tabla);
        } catch (DocumentException ex) {
            throw new IllegalStateException("No se pudo generar el PDF", ex);
        } finally {
            doc.close();
        }
        return salida.toByteArray();
    }

    private static PdfPCell celda(String texto, Font fuente, Color fondo, boolean numerica) {
        PdfPCell c = new PdfPCell(new Phrase(texto == null ? "" : texto, fuente));
        c.setBackgroundColor(fondo);
        c.setPadding(4);
        c.setBorderColor(new Color(221, 227, 234));
        c.setHorizontalAlignment(numerica ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
        return c;
    }
}
