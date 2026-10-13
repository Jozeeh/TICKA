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
 * Las credenciales funcionan como un archivo .env: viven en un archivo local
 * que Git ignora y nunca se escriben en el código. Se buscan en este orden:
 *
 * 1. Variables de entorno TICKA_DB_URL, TICKA_DB_USER y TICKA_DB_PASSWORD
 *    (útil en un servidor de producción).
 * 2. src/main/resources/db.properties dentro del proyecto. Maven lo copia a
 *    WEB-INF/classes al compilar, por eso se lee desde el classpath.
 * 3. %USERPROFILE%\.ticka\db.properties (respaldo, como en las guías).
 *
 * No se mantiene una Connection estática compartida: cada operación abre y
 * cierra la suya con try-with-resources.
 *
 * Se registra el driver con Class.forName porque en un Tomcat con varias
 * aplicaciones desplegadas el registro automático puede no quedar visible
 * para el classloader de esta aplicación ("No suitable driver found").
 */
public final class ConexionBD {

    private static final String RECURSO = "db.properties";
    private static final Path RUTA_PERSONAL = Path.of(
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
        if (config != null) {
            return config;
        }
        Properties p = new Properties();

        // 1. Variables de entorno
        String url = System.getenv("TICKA_DB_URL");
        if (url != null && !url.isBlank()) {
            p.setProperty("db.url", url);
            p.setProperty("db.user", valorOVacio(System.getenv("TICKA_DB_USER")));
            p.setProperty("db.password", valorOVacio(System.getenv("TICKA_DB_PASSWORD")));
            return guardar(p);
        }

        // 2. Archivo dentro del proyecto (src/main/resources/db.properties)
        try (InputStream in = ConexionBD.class.getClassLoader().getResourceAsStream(RECURSO)) {
            if (in != null) {
                p.load(in);
                return guardar(p);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo leer db.properties del proyecto.", ex);
        }

        // 3. Carpeta personal del usuario
        if (Files.exists(RUTA_PERSONAL)) {
            try (InputStream in = Files.newInputStream(RUTA_PERSONAL)) {
                p.load(in);
                return guardar(p);
            } catch (IOException ex) {
                throw new IllegalStateException("No se pudo leer " + RUTA_PERSONAL, ex);
            }
        }

        throw new IllegalStateException("No hay configuración de base de datos. Copie "
                + "src/main/resources/db.properties.example como db.properties en esa misma carpeta, "
                + "complete sus credenciales y vuelva a compilar.");
    }

    private static Properties guardar(Properties p) {
        config = p;
        return p;
    }

    private static String valorOVacio(String valor) {
        return valor == null ? "" : valor;
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
}
