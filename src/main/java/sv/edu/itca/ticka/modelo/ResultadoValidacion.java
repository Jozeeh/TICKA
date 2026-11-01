package sv.edu.itca.ticka.modelo;

import java.io.Serializable;

/**
 * Resultado de validar un boleto en puerta (RF-25 a RF-27): VÁLIDO con el
 * asiento, o RECHAZADO con el motivo. Se guarda en la sesión para mostrarlo
 * después de redirigir (patrón POST-Redirect-GET).
 */
public class ResultadoValidacion implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean valido;
    private final String motivo;
    private final String codigo;
    private final String zona;
    private final String asiento;
    private final String cliente;
    private final String hora;

    public ResultadoValidacion(boolean valido, String motivo, String codigo, BoletoPuerta b, String hora) {
        this.valido = valido;
        this.motivo = motivo;
        this.codigo = codigo;
        this.zona = b == null ? null : b.getZona();
        this.asiento = b == null ? null : b.getAsiento();
        this.cliente = b == null ? null : b.getCliente();
        this.hora = hora;
    }

    public boolean isValido() { return valido; }
    public String getMotivo() { return motivo; }
    public String getCodigo() { return codigo; }
    public String getZona() { return zona; }
    public String getAsiento() { return asiento; }
    public String getCliente() { return cliente; }
    public String getHora() { return hora; }
}
