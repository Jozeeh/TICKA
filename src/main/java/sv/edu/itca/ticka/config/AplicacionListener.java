package sv.edu.itca.ticka.config;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Ciclo de vida de la aplicación. Al detenerla o redesplegarla (por ejemplo
 * desde NetBeans), libera el driver de MySQL y su hilo de limpieza. Si no se
 * hace, Tomcat muestra "this web application instance has been stopped
 * already" y la memoria de la versión anterior no se libera.
 */
@WebListener
public class AplicacionListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sce.getServletContext().log("TICKA iniciada en " + sce.getServletContext().getContextPath());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ClassLoader cargadorApp = Thread.currentThread().getContextClassLoader();
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == cargadorApp) {
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException ex) {
                    sce.getServletContext().log("No se pudo liberar el driver JDBC", ex);
                }
            }
        }
        AbandonedConnectionCleanupThread.checkedShutdown();
    }
}
