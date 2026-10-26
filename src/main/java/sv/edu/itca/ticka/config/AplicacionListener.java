package sv.edu.itca.ticka.config;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import sv.edu.itca.ticka.dao.impl.EventoDAOImpl;
import sv.edu.itca.ticka.servicio.TareaEstadosEventos;

/**
 * Ciclo de vida de la aplicación. Al detenerla o redesplegarla (por ejemplo
 * desde NetBeans), libera el driver de MySQL y su hilo de limpieza. Si no se
 * hace, Tomcat muestra "this web application instance has been stopped
 * already" y la memoria de la versión anterior no se libera.
 *
 * También programa la finalización automática de eventos (RF-51): cada
 * minuto se revisan los horarios y las reservas vencidas.
 */
@WebListener
public class AplicacionListener implements ServletContextListener {

    private static final long MINUTOS_ENTRE_REVISIONES = 1;

    private ScheduledExecutorService tareas;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sce.getServletContext().log("TICKA iniciada en " + sce.getServletContext().getContextPath());
        tareas = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread hilo = new Thread(r, "ticka-estados-eventos");
            hilo.setDaemon(true);
            return hilo;
        });
        tareas.scheduleWithFixedDelay(new TareaEstadosEventos(new EventoDAOImpl()),
                0, MINUTOS_ENTRE_REVISIONES, TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Primero se detiene la tarea programada, para que no use el driver ya liberado.
        if (tareas != null) {
            tareas.shutdownNow();
            try {
                tareas.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
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
