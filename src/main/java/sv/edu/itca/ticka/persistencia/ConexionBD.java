package sv.edu.itca.ticka.persistencia;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Clase de conexión JDBC reutilizable para TICKA.
 *
 * La URL, el usuario y la contraseña se leen de un archivo de propiedades
 * ubicado fuera del proyecto (%USERPROFILE%\.ticka\db.properties), nunca
 * incrustados en el código ni subidos al repositorio (ver
 * db.properties.example). No se mantiene una Connection estática compartida:
 * cada operación abre y cierra la suya con try-with-resources.
 *
 * Se registra el driver con Class.forName porque en un Tomcat con varias
 * aplicaciones desplegadas el registro automático puede no quedar visible
 * para el classloader de esta aplicación ("No suitable driver found").
 */
public final class ConexionBD {

    private static final Path RUTA_CONFIG = Path.of(
            System.getProperty("user.home"), ".ticka", "db.properties");

    private static Properties config;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(
                    "No se encontró el driver com.mysql.cj.jdbc.Driver en el classpath.", ex);
        }
    }

    private ConexionBD() {
    }

    /** Carga la configuración la primera vez que se necesita. */
    private static synchronized Properties obtenerConfiguracion() {
        if (config == null) {
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(RUTA_CONFIG)) {
                p.load(in);
            } catch (IOException ex) {
                throw new IllegalStateException(
                        "No se pudo cargar la configuración JDBC en " + RUTA_CONFIG
                        + ". Copie db.properties.example a esa ruta y complete sus "
                        + "credenciales locales.", ex);
            }
            config = p;
        }
        return config;
    }

    /**
     * Abre una conexión nueva. El llamador es responsable de cerrarla
     * (idealmente con try-with-resources).
     */
    public static Connection obtenerConexion() throws SQLException {
        Properties p = obtenerConfiguracion();
        return DriverManager.getConnection(
                p.getProperty("db.url"),
                p.getProperty("db.user"),
                p.getProperty("db.password"));
    }

    /** Ruta donde se espera el archivo de configuración (para el diagnóstico). */
    public static Path rutaConfiguracion() {
        return RUTA_CONFIG;
    }
}
