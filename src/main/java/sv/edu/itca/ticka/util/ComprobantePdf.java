package sv.edu.itca.ticka.util;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import sv.edu.itca.ticka.modelo.Cliente;
import sv.edu.itca.ticka.modelo.CompraCliente;

/**
 * Comprobante de compra en PDF (RF-22): datos de la venta y del cliente, y
 * un recuadro por boleto con su asiento, precio, estado y código QR, listo
 * para mostrarlo en la puerta desde el celular o impreso.
 */
public final class ComprobantePdf {

    private static final Color AZUL = new Color(21, 42, 69);
    private static final Color ACENTO = new Color(228, 87, 46);
    private static final Color BORDE = new Color(191, 199, 209);
    private static final Color GRIS = new Color(95, 107, 122);
    private static final Color FONDO = new Color(243, 246, 250);

    private ComprobantePdf() {
    }

    public static byte[] generar(CompraCliente compra, Cliente cliente) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.LETTER, 40, 40, 36, 36);
        try {
            PdfWriter.getInstance(doc, salida);
            doc.addTitle("Comprobante de compra #" + compra.getId());
            doc.open();

            doc.add(new Paragraph("TICKA · SISTEMA DE EVENTOS EN VIVO", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, ACENTO)));
            Paragraph titulo = new Paragraph("Comprobante de compra #" + compra.getId(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, AZUL));
            titulo.setSpacingAfter(10);
            doc.add(titulo);

            PdfPTable datos = new PdfPTable(new float[]{1.3f, 2.7f, 1.3f, 2.7f});
            datos.setWidthPercentage(100);
            par(datos, "Evento", compra.getEvento());
            par(datos, "Fecha del evento", compra.getFechaEventoTexto());
            par(datos, "Recinto", compra.getRecinto());
            par(datos, "Cliente", cliente.getNombreCompleto() + " (" + cliente.getDocumento() + ")");
            par(datos, "Fecha de compra", compra.getFechaTexto());
            par(datos, "Canal / pago", compra.getCanal() + " · " + compra.getFormaPago());
            par(datos, "Estado", compra.getEstado());
            par(datos, "Total", compra.getTotalTexto());
            doc.add(datos);

            Paragraph sub = new Paragraph("BOLETOS (" + compra.getBoletos().size() + ")",
                    FontFactory.getFont(FontFactory.HELVETICA, 11, AZUL));
            sub.setSpacingBefore(16);
            sub.setSpacingAfter(6);
            doc.add(sub);

            // Dos boletos por fila, cada uno con su QR.
            PdfPTable boletos = new PdfPTable(2);
            boletos.setWidthPercentage(100);
            for (CompraCliente.Boleto b : compra.getBoletos()) {
                boletos.addCell(boleto(b));
            }
            if (compra.getBoletos().size() % 2 == 1) {
                PdfPCell vacia = new PdfPCell();
                vacia.setBorder(Rectangle.NO_BORDER);
                boletos.addCell(vacia);
            }
            doc.add(boletos);

            Paragraph nota = new Paragraph("Presente el código QR de cada boleto en la puerta. Cada código permite "
                    + "un solo ingreso y solo para el asiento indicado. El pago se registró en el sistema; no se "
                    + "procesó ningún cobro en línea.", FontFactory.getFont(FontFactory.HELVETICA, 8.5f, GRIS));
            nota.setSpacingBefore(12);
            doc.add(nota);
        } catch (DocumentException | IOException ex) {
            throw new IllegalStateException("No se pudo generar el comprobante", ex);
        } finally {
            doc.close();
        }
        return salida.toByteArray();
    }

    private static void par(PdfPTable t, String etiqueta, String valor) {
        PdfPCell e = new PdfPCell(new Phrase(etiqueta.toUpperCase(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, AZUL)));
        e.setBackgroundColor(FONDO);
        e.setBorderColor(BORDE);
        e.setPadding(5);
        t.addCell(e);
        PdfPCell v = new PdfPCell(new Phrase(valor == null ? "" : valor, FontFactory.getFont(FontFactory.HELVETICA, 9.5f)));
        v.setBorderColor(BORDE);
        v.setPadding(5);
        t.addCell(v);
    }

    private static PdfPCell boleto(CompraCliente.Boleto b) throws DocumentException, IOException {
        PdfPTable interna = new PdfPTable(new float[]{1.1f, 1.4f});
        Image qr = Image.getInstance(GeneradorQr.png(b.getCodigo(), 200));
        qr.scaleToFit(110, 110);
        PdfPCell celdaQr = new PdfPCell(qr, false);
        celdaQr.setBorder(Rectangle.NO_BORDER);
        celdaQr.setPadding(4);
        celdaQr.setHorizontalAlignment(Element.ALIGN_CENTER);
        interna.addCell(celdaQr);

        Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, AZUL);
        Font fTexto = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Font fCodigo = FontFactory.getFont(FontFactory.COURIER, 6.5f, GRIS);
        Paragraph p = new Paragraph();
        p.add(new Phrase(b.getZona() + " · " + b.getAsiento() + "\n", fTitulo));
        p.add(new Phrase(b.getPrecioTexto() + " · " + b.getEstado() + "\n\n", fTexto));
        p.add(new Phrase(b.getCodigo(), fCodigo));
        PdfPCell texto = new PdfPCell(p);
        texto.setBorder(Rectangle.NO_BORDER);
        texto.setVerticalAlignment(Element.ALIGN_MIDDLE);
        interna.addCell(texto);

        PdfPCell contenedor = new PdfPCell(interna);
        contenedor.setBorderColor(BORDE);
        contenedor.setPadding(6);
        return contenedor;
    }
}
