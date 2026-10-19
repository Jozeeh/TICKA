package sv.edu.itca.ticka.filtro;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Protección CSRF (RNF-04): cada sesión recibe un token aleatorio que las
 * vistas incluyen como campo oculto "csrf" en todos los formularios POST.
 * Si un POST llega sin el token o con uno distinto, se rechaza. Así otro
 * sitio no puede enviar formularios en nombre del usuario.
 */
public class CsrfFilter implements Filter {

    public static final String ATRIBUTO = "csrf";
    /** Debe coincidir con maxRequestSize de @MultipartConfig en EventoServlet (12 MB). */
    public static final long LIMITE_ADJUNTOS = 12L * 1024 * 1024;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Los recursos estáticos no necesitan sesión ni token.
        if (request.getServletPath().startsWith("/resources/")) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession sesion = request.getSession();
        String token = (String) sesion.getAttribute(ATRIBUTO);
        if (token == null) {
            token = generarToken();
            sesion.setAttribute(ATRIBUTO, token);
        }

        // Si un formulario con archivo supera el límite de @MultipartConfig, Tomcat no puede
        // leer ningún campo (ni el token). Se avisa al usuario en lugar de responder 403.
        String tipoContenido = request.getContentType();
        if (tipoContenido != null && tipoContenido.startsWith("multipart/")
                && request.getContentLengthLong() > LIMITE_ADJUNTOS) {
            sesion.setAttribute("flashError", "El archivo adjunto es demasiado grande (máximo 2 MB).");
            response.sendRedirect(request.getContextPath() + "/organizador/eventos");
            return;
        }
        String recibido = request.getParameter(ATRIBUTO);
        if ("POST".equalsIgnoreCase(request.getMethod()) && !coinciden(token, recibido)) {
            if ("/acceso".equals(request.getServletPath())) {
                // Formulario de acceso abierto antes de que expirara la sesión.
                response.sendRedirect(request.getContextPath() + "/acceso?estado=expirada");
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Formulario vencido o no válido. Recargue la página e intente de nuevo.");
            }
            return;
        }
        chain.doFilter(req, res);
    }

    private static String generarToken() {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Comparación en tiempo constante para no filtrar información del token. */
    private static boolean coinciden(String esperado, String recibido) {
        if (recibido == null) {
            return false;
        }
        return MessageDigest.isEqual(esperado.getBytes(StandardCharsets.UTF_8),
                recibido.getBytes(StandardCharsets.UTF_8));
    }
}
