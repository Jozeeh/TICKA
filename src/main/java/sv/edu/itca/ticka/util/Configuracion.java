package sv.edu.itca.ticka.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parámetros de negocio configurables (RNF-06) leídos de ticka.properties
 * (src/main/resources). Si el archivo falta o un valor no es válido se usa
 * el valor por defecto, así la aplicación siempre arranca.
 */
public final class Configuracion {

    private static final Logger LOG = Logger.getLogger(Configuracion.class.getName());
    private static final Properties VALORES = cargar();

    private Configuracion() {
    }

    private static Properties cargar() {
        Properties p = new Properties();
        try (InputStream in = Configuracion.class.getClassLoader().getResourceAsStream("ticka.properties")) {
            if (in != null) {
                p.load(in);
            }
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "No se pudo leer ticka.properties; se usan los valores por defecto", ex);
        }
        return p;
    }

    /** Minutos de la reserva temporal de asientos (RF-18). */
    public static int minutosReserva() {
        return entero("reserva.minutos", 10, 1, 120);
    }

    /** Máximo de asientos por compra (RF-19). */
    public static int maximoPorCompra() {
        return entero("compra.maximo", 6, 1, 50);
    }

    /** Horas antes de la apertura de puertas hasta las que se puede anular (RN-12). */
    public static int horasAnulacion() {
        return entero("anulacion.horas", 24, 0, 720);
    }

    /** Re-entrada permitida (RF-31). */
    public static boolean reentrada() {
        return Boolean.parseBoolean(VALORES.getProperty("acceso.reentrada", "false").trim());
    }

    /** Registrar incidencia por cada rechazo en puerta (RF-35). */
    public static boolean incidenciaPorRechazo() {
        return Boolean.parseBoolean(VALORES.getProperty("acceso.incidenciaPorRechazo", "true").trim());
    }

    private static int entero(String clave, int defecto, int minimo, int maximo) {
        try {
            int v = Integer.parseInt(VALORES.getProperty(clave, String.valueOf(defecto)).trim());
            return v < minimo || v > maximo ? defecto : v;
        } catch (NumberFormatException ex) {
            return defecto;
        }
    }
}
