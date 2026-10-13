package sv.edu.itca.ticka.servicio;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/** Pruebas de las reglas de formato de usuario y contraseña. */
public class ValidadorUsuarioTest {

    @Test
    public void usernameValido() {
        assertNull(ValidadorUsuario.validarUsername("organizador.uno"));
    }

    @Test
    public void usernameConMayusculasOEspaciosEsInvalido() {
        assertNotNull(ValidadorUsuario.validarUsername("Juan Perez"));
        assertNotNull(ValidadorUsuario.validarUsername("abc"));
        assertNotNull(ValidadorUsuario.validarUsername(""));
    }

    @Test
    public void claveDebeTenerOchoCaracteresLetrasYNumeros() {
        assertNotNull(ValidadorUsuario.validarClave("abc12", "abc12"));
        assertNotNull(ValidadorUsuario.validarClave("solotexto", "solotexto"));
        assertNotNull(ValidadorUsuario.validarClave("12345678", "12345678"));
        assertNull(ValidadorUsuario.validarClave("Ticka2026", "Ticka2026"));
    }

    @Test
    public void confirmacionDistintaEsInvalida() {
        assertNotNull(ValidadorUsuario.validarClave("Ticka2026", "Ticka2027"));
    }
}
