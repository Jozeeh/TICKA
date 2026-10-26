package sv.edu.itca.ticka.util;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import sv.edu.itca.ticka.modelo.Reporte;

/**
 * Convierte un Reporte a PDF (RF-47, RF-56) con un formato formal tipo
 * "informe ejecutivo":
 *
 *   1. Título grande y franja de datos del informe (nombre, código, fecha,
 *      quién lo generó y filtros aplicados).
 *   2. RESUMEN: las cifras principales del reporte en recuadros.
 *   3. DETALLE: la tabla con encabezado sombreado y fila de totales. Si el
 *      reporte es agrupado (general de un evento) las filas se ordenan por
 *      secciones numeradas 1.0, 1.1, 1.2... como en una plantilla de estado.
 *   4. INFORMACIÓN ADICIONAL: la nota aclaratoria del reporte.
 *   5. Pie en cada página con el sistema y "Página X de Y".
 *
 * Las tablas de hasta 4 columnas usan hoja carta vertical; las más anchas,
 * horizontal.
 */
public final class ExportadorPdf {

    private static final Color AZUL = new Color(21, 42, 69);
    private static final Color AZUL_MEDIO = new Color(31, 58, 95);
    private static final Color ENCABEZADO = new Color(214, 220, 228);
    private static final Color SECCION = new Color(233, 237, 243);
    private static final Color ALTERNA = new Color(247, 249, 251);
    private static final Color BORDE = new Color(191, 199, 209);
    private static final Color GRIS = new Color(95, 107, 122);
    private static final Color ACENTO = new Color(228, 87, 46);

    private static final Font F_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, AZUL);
    private static final Font F_MARCA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, ACENTO);
    private static final Font F_ETIQUETA = FontFactory.getFont(FontFactory.HELVETICA, 7, GRIS);
    private static final Font F_DATO = FontFactory.getFont(FontFactory.HELVETICA, 9.5f, AZUL);
    private static final Font F_SECCION_TITULO = FontFactory.getFont(FontFactory.HELVETICA, 11, AZUL);
    private static final Font F_ENCABEZADO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, AZUL);
    private static final Font F_CELDA = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Color.BLACK);
    private static final Font F_CELDA_NEGRITA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, AZUL);
    private static final Font F_KPI_VALOR = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, AZUL_MEDIO);
    private static final Font F_NOTA = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Color.DARK_GRAY);

    private ExportadorPdf() {
    }

    /**
     * Genera el PDF en memoria.
     *
     * @param codigo   código corto del informe (ej. RPT-VENTAS)
     * @param filtros  filtros aplicados en texto (puede ser null)
     * @param generado fecha y hora de generación ya formateada
     * @param usuario  nombre de quien lo generó (puede ser null)
     */
    public static byte[] generar(Reporte r, String codigo, String filtros, String generado, String usuario) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        boolean vertical = r.getColumnas().size() <= 4;
        Document doc = new Document(vertical ? PageSize.LETTER : PageSize.LETTER.rotate(), 36, 36, 36, 48);
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, salida);
            writer.setPageEvent(new PiePagina());
            doc.addTitle(r.getTitulo());
            doc.addCreator("TICKA");
            doc.open();

            encabezado(doc, r, codigo, filtros, generado, usuario);
            if (!r.getIndicadores().isEmpty()) {
                tituloSeccion(doc, "Resumen");
                doc.add(resumen(r));
            }
            tituloSeccion(doc, "Detalle");
            doc.add(r.isAgrupado() ? tablaAgrupada(r) : tabla(r));
            if (r.getNota() != null && !r.getNota().isEmpty()) {
                tituloSeccion(doc, "Información adicional");
                doc.add(cuadroNota(r.getNota()));
            }
        } catch (DocumentException ex) {
            throw new IllegalStateException("No se pudo generar el PDF", ex);
        } finally {
            doc.close();
        }
        return salida.toByteArray();
    }

    // ------------------------------------------------------------------ partes

    private static void encabezado(Document doc, Reporte r, String codigo, String filtros, String generado,
            String usuario) throws DocumentException {
        doc.add(new Paragraph("TICKA · SISTEMA DE EVENTOS EN VIVO", F_MARCA));
        Paragraph titulo = new Paragraph(r.getTitulo(), F_TITULO);
        titulo.setSpacingAfter(10);
        doc.add(titulo);

        // Franja de datos del informe, como la cabecera de una plantilla formal.
        PdfPTable datos = new PdfPTable(new float[]{4, 2, 2.2f, 2.6f});
        datos.setWidthPercentage(100);
        datos.addCell(campo("NOMBRE DEL REPORTE", r.getTitulo(), 1));
        datos.addCell(campo("CÓDIGO", codigo, 1));
        datos.addCell(campo("FECHA DE GENERACIÓN", generado, 1));
        datos.addCell(campo("GENERADO POR", usuario == null ? "—" : usuario, 1));
        datos.addCell(campo("FILTROS APLICADOS", filtros == null || filtros.isEmpty() ? "Ninguno" : filtros, 4));
        doc.add(datos);
    }

    /** Celda con etiqueta pequeña arriba y el dato en un recuadro, como un formulario. */
    private static PdfPCell campo(String etiqueta, String valor, int columnas) {
        PdfPTable interna = new PdfPTable(1);
        interna.setWidthPercentage(100);
        PdfPCell e = new PdfPCell(new Phrase(etiqueta, F_ETIQUETA));
        e.setBorder(Rectangle.NO_BORDER);
        e.setPadding(1);
        e.setPaddingBottom(2);
        interna.addCell(e);
        PdfPCell v = new PdfPCell(new Phrase(valor == null ? "" : valor, F_DATO));
        v.setBorderColor(BORDE);
        v.setPadding(5);
        v.setMinimumHeight(20);
        interna.addCell(v);

        PdfPCell contenedor = new PdfPCell(interna);
        contenedor.setBorder(Rectangle.NO_BORDER);
        contenedor.setColspan(columnas);
        contenedor.setPadding(2);
        return contenedor;
    }

    private static void tituloSeccion(Document doc, String texto) throws DocumentException {
        Paragraph p = new Paragraph(texto.toUpperCase(), F_SECCION_TITULO);
        p.setSpacingBefore(14);
        p.setSpacingAfter(5);
        doc.add(p);
    }

    /** Recuadros con las cifras principales: etiqueta arriba (sombreada) y valor grande. */
    private static PdfPTable resumen(Reporte r) {
        List<String[]> kpis = r.getIndicadores();
        PdfPTable t = new PdfPTable(kpis.size());
        t.setWidthPercentage(100);
        for (String[] k : kpis) {
            PdfPCell e = new PdfPCell(new Phrase(k[0].toUpperCase(), F_ENCABEZADO));
            e.setBackgroundColor(ENCABEZADO);
            e.setBorderColor(BORDE);
            e.setPadding(5);
            e.setHorizontalAlignment(Element.ALIGN_CENTER);
            t.addCell(e);
        }
        for (String[] k : kpis) {
            PdfPCell v = new PdfPCell(new Phrase(k[1], F_KPI_VALOR));
            v.setBorderColor(BORDE);
            v.setPadding(8);
            v.setHorizontalAlignment(Element.ALIGN_CENTER);
            t.addCell(v);
        }
        return t;
    }

    /** Tabla normal: encabezado sombreado, filas alternas y fila de totales. */
    private static PdfPTable tabla(Reporte r) {
        int n = r.getColumnas().size();
        PdfPTable t = new PdfPTable(anchos(r));
        t.setWidthPercentage(100);
        t.setHeaderRows(1);
        for (int i = 0; i < n; i++) {
            t.addCell(encabezado(r.getColumnas().get(i), r.esNumerica(i)));
        }
        boolean alterna = false;
        for (List<String> fila : r.getFilas()) {
            for (int i = 0; i < fila.size(); i++) {
                t.addCell(celda(fila.get(i), F_CELDA, alterna ? ALTERNA : Color.WHITE, r.esNumerica(i)));
            }
            alterna = !alterna;
        }
        if (r.getFilas().isEmpty()) {
            PdfPCell vacia = celda("No hay datos con esos filtros.", F_CELDA, Color.WHITE, false);
            vacia.setColspan(n);
            vacia.setHorizontalAlignment(Element.ALIGN_CENTER);
            vacia.setPadding(10);
            t.addCell(vacia);
        } else if (r.getTotales() != null) {
            for (int i = 0; i < r.getTotales().size(); i++) {
                String valor = i == 0 ? r.getTotales().get(i).toUpperCase() : r.getTotales().get(i);
                t.addCell(celda(valor, F_CELDA_NEGRITA, ENCABEZADO, r.esNumerica(i)));
            }
        }
        return t;
    }

    /**
     * Ancho relativo de cada columna: la primera (nombre del evento o fecha)
     * es la más ancha, las de texto medianas y las numéricas angostas.
     */
    private static float[] anchos(Reporte r) {
        int n = r.getColumnas().size();
        float[] a = new float[n];
        for (int i = 0; i < n; i++) {
            a[i] = i == 0 ? 2.4f : r.esNumerica(i) ? 1.3f : 1.6f;
        }
        return a;
    }

    /**
     * Tabla por secciones (primera columna = sección): REF. | DATO | VALOR,
     * con una fila sombreada por sección (1.0 EVENTO) y sus datos (1.1, 1.2...).
     */
    private static PdfPTable tablaAgrupada(Reporte r) {
        Map<String, List<List<String>>> secciones = new LinkedHashMap<>();
        for (List<String> fila : r.getFilas()) {
            secciones.computeIfAbsent(fila.get(0), k -> new ArrayList<>()).add(fila);
        }
        PdfPTable t = new PdfPTable(new float[]{1.1f, 4, 5});
        t.setWidthPercentage(100);
        t.setHeaderRows(1);
        t.addCell(encabezado("REF.", false));
        t.addCell(encabezado(r.getColumnas().get(1).toUpperCase(), false));
        t.addCell(encabezado(r.getColumnas().get(2).toUpperCase(), false));
        int numero = 0;
        for (Map.Entry<String, List<List<String>>> s : secciones.entrySet()) {
            numero++;
            t.addCell(celda(numero + ".0", F_CELDA_NEGRITA, SECCION, false));
            PdfPCell nombre = celda(s.getKey().toUpperCase(), F_CELDA_NEGRITA, SECCION, false);
            nombre.setColspan(2);
            t.addCell(nombre);
            int item = 0;
            for (List<String> fila : s.getValue()) {
                item++;
                t.addCell(celda(numero + "." + item, F_CELDA, Color.WHITE, false));
                t.addCell(celda(fila.get(1), F_CELDA, Color.WHITE, false));
                t.addCell(celda(fila.get(2), F_CELDA_NEGRITA, Color.WHITE, false));
            }
        }
        if (secciones.isEmpty()) {
            PdfPCell vacia = celda("El evento no existe o fue desactivado.", F_CELDA, Color.WHITE, false);
            vacia.setColspan(3);
            t.addCell(vacia);
        }
        return t;
    }

    private static PdfPTable cuadroNota(String nota) {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        PdfPCell c = new PdfPCell(new Phrase(nota, F_NOTA));
        c.setBorderColor(BORDE);
        c.setPadding(8);
        c.setMinimumHeight(40);
        t.addCell(c);
        return t;
    }

    private static PdfPCell encabezado(String texto, boolean numerica) {
        PdfPCell c = new PdfPCell(new Phrase(texto.toUpperCase(), F_ENCABEZADO));
        c.setBackgroundColor(ENCABEZADO);
        c.setBorderColor(BORDE);
        c.setPadding(5);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setHorizontalAlignment(numerica ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
        return c;
    }

    private static PdfPCell celda(String texto, Font fuente, Color fondo, boolean numerica) {
        PdfPCell c = new PdfPCell(new Phrase(texto == null ? "" : texto, fuente));
        c.setBackgroundColor(fondo);
        c.setBorderColor(BORDE);
        c.setPadding(4.5f);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setHorizontalAlignment(numerica ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
        return c;
    }

    // ------------------------------------------------------------------ pie de página

    /** Escribe en cada página el pie con el sistema y "Página X de Y" (Y se completa al cerrar). */
    private static final class PiePagina extends PdfPageEventHelper {

        private PdfTemplate total;
        private BaseFont fuente;

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            total = writer.getDirectContent().createTemplate(30, 12);
            try {
                fuente = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            } catch (DocumentException | IOException ex) {
                throw new IllegalStateException("No se pudo cargar la fuente del pie", ex);
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            float y = document.bottom() - 18;
            cb.setColorStroke(BORDE);
            cb.setLineWidth(0.6f);
            cb.moveTo(document.left(), y + 10);
            cb.lineTo(document.right(), y + 10);
            cb.stroke();

            Font fPie = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, GRIS);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase(new Chunk("TICKA · Sistema de Eventos en Vivo · Equipo 11 · ITCA-FEPADE", fPie)),
                    document.left(), y, 0);

            String texto = "Página " + writer.getPageNumber() + " de ";
            float ancho = fuente.getWidthPoint(texto, 7.5f);
            float x = document.right() - ancho - 14;
            cb.beginText();
            cb.setFontAndSize(fuente, 7.5f);
            cb.setColorFill(GRIS);
            cb.setTextMatrix(x, y);
            cb.showText(texto);
            cb.endText();
            cb.addTemplate(total, x + ancho, y);
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            total.beginText();
            total.setFontAndSize(fuente, 7.5f);
            total.setColorFill(GRIS);
            total.setTextMatrix(0, 0);
            // Al cerrar, el escritor ya avanzó a la página siguiente: el total es uno menos.
            total.showText(String.valueOf(writer.getPageNumber() - 1));
            total.endText();
        }
    }
}
