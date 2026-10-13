package sv.edu.itca.ticka.filtro;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Fuerza UTF-8 en todas las peticiones y respuestas (RNF-13) para que las
 * tildes y la ñ lleguen bien desde los formularios hasta MySQL. Se ejecuta
 * primero en la cadena (ver orden en web.xml).
 */
public class CodificacionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        if (req.getCharacterEncoding() == null) {
            req.setCharacterEncoding("UTF-8");
        }
        res.setCharacterEncoding("UTF-8");
        chain.doFilter(req, res);
    }
}
