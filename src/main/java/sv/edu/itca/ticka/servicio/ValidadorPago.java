package sv.edu.itca.ticka.servicio;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Validación del formulario de pago simulado del cliente (RF-21).
 *
 * El pago es ficticio: solo se revisa que cada dato obligatorio esté
 * escrito. NO se verifica la cantidad de dígitos, el vencimiento ni si la
 * tarjeta existe, y los datos de la tarjeta nunca se guardan ni se registran
 * en el log: se descartan después de esta validación.
 */
public final class ValidadorPago {

    /** Campo del formulario → nombre que se muestra en el mensaje. */
    private static final Map<String, String> OBLIGATORIOS = new LinkedHashMap<>();

    static {
        OBLIGATORIOS.put("nombre", "el nombre completo");
        OBLIGATORIOS.put("correo", "el correo");
        OBLIGATORIOS.put("pais", "el país");
        OBLIGATORIOS.put("titular", "el nombre del titular de la tarjeta");
        OBLIGATORIOS.put("numero", "el número de la tarjeta");
        OBLIGATORIOS.put("vencimiento", "el vencimiento");
        OBLIGATORIOS.put("cvv", "el código de seguridad (CVV)");
    }

    private ValidadorPago() {
    }

    /**
     * @param valores lector de los campos del formulario (por ejemplo req::getParameter)
     * @param terminos true si aceptó los términos de compra
     * @return el mensaje del primer dato que falta, o null si se puede pagar
     */
    public static String validar(java.util.function.Function<String, String> valores, boolean terminos) {
        for (Map.Entry<String, String> campo : OBLIGATORIOS.entrySet()) {
            String v = valores.apply(campo.getKey());
            if (v == null || v.trim().isEmpty()) {
                return "Escriba " + campo.getValue() + ".";
            }
        }
        if (!terminos) {
            return "Debe aceptar los términos de compra.";
        }
        return null;
    }
}
