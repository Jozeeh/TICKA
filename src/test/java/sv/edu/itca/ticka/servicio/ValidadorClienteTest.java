package sv.edu.itca.ticka.servicio;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import sv.edu.itca.ticka.modelo.Cliente;

/** Datos del registro de clientes (RF-13). */
public class ValidadorClienteTest {

    private static Cliente valido() {
        Cliente c = new Cliente();
        c.setNombres("Ana");
        c.setApellidos("López");
        c.setDocumento("04512378-1");
        c.setCorreo("ana@correo.test");
        c.setTelefono("7012-3456");
        return c;
    }

    @Test
    public void aceptaUnClienteValido() {
        assertNull(ValidadorCliente.validar(valido()));
        Cliente sinTelefono = valido();
        sinTelefono.setTelefono(null);
        assertNull(ValidadorCliente.validar(sinTelefono));
    }

    @Test
    public void rechazaDocumentoYCorreoInvalidos() {
        Cliente c = valido();
        c.setDocumento("12");
        assertNotNull(ValidadorCliente.validar(c));
        c = valido();
        c.setCorreo("sin-arroba");
        assertNotNull(ValidadorCliente.validar(c));
        c = valido();
        c.setNombres(" ");
        assertNotNull(ValidadorCliente.validar(c));
    }
}
