package sv.edu.itca.ticka.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

/** Genera la imagen PNG del código QR de un boleto con ZXing (RF-20, RNF-15). */
public final class GeneradorQr {

    private GeneradorQr() {
    }

    public static byte[] png(String contenido, int tamano) {
        Map<EncodeHintType, Object> opciones = new EnumMap<>(EncodeHintType.class);
        opciones.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        opciones.put(EncodeHintType.MARGIN, 1);
        opciones.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        try {
            BitMatrix matriz = new QRCodeWriter().encode(contenido, BarcodeFormat.QR_CODE, tamano, tamano, opciones);
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matriz, "PNG", salida);
            return salida.toByteArray();
        } catch (WriterException | IOException ex) {
            throw new IllegalStateException("No se pudo generar el código QR", ex);
        }
    }
}
