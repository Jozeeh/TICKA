package sv.edu.itca.ticka.controlador;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.ticka.persistencia.ConexionBD;

/**
 * Servlet de diagnóstico del entorno: muestra la versión de Java, el
 * servidor, la ruta del archivo de configuración y el resultado de una
 * conexión de prueba a MySQL con el conteo de tablas de la base ticka.
 */
@WebServlet("/diagnostico")
public class DiagnosticoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Map<String, String> entorno = new LinkedHashMap<>();
        entorno.put("Aplicación", "TICKA - Sistema de Eventos en Vivo");
        entorno.put("Java", System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
        entorno.put("Servidor", getServletContext().getServerInfo());
        entorno.put("API Servlet", getServletContext().getMajorVersion() + "." + getServletContext().getMinorVersion());
        entorno.put("Ruta de contexto", req.getContextPath());
        entorno.put("Archivo de configuración", ConexionBD.rutaConfiguracion().toString());
        req.setAttribute("entorno", entorno);

        Map<String, String> baseDatos = new LinkedHashMap<>();
        try (Connection cn = ConexionBD.obtenerConexion()) {
            DatabaseMetaData meta = cn.getMetaData();
            baseDatos.put("Motor", meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
            baseDatos.put("Base de datos", cn.getCatalog());
            baseDatos.put("Usuario", meta.getUserName());
            baseDatos.put("Tablas", String.valueOf(contarTablas(cn)));
            req.setAttribute("conexionOk", true);
        } catch (SQLException | IllegalStateException ex) {
            log("Fallo en la conexión de diagnóstico", ex);
            req.setAttribute("conexionOk", false);
            req.setAttribute("errorConexion", ex.getMessage());
        }
        req.setAttribute("baseDatos", baseDatos);
        req.getRequestDispatcher("/WEB-INF/vistas/diagnostico.jsp").forward(req, resp);
    }

    private int contarTablas(Connection cn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.tables "
                + "WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'";
        try (PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
