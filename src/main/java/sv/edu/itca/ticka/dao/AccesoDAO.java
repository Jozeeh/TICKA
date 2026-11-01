package sv.edu.itca.ticka.dao;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import sv.edu.itca.ticka.modelo.BoletoPuerta;
import sv.edu.itca.ticka.modelo.Evento;
import sv.edu.itca.ticka.modelo.OcupacionPuerta;
import sv.edu.itca.ticka.modelo.RegistroAcceso;

/**
 * DAO del control de acceso en puerta (M4). Los métodos con Connection se
 * usan dentro de la transacción de AccesoServicio.
 */
public interface AccesoDAO {

    /** Eventos que se pueden controlar en puerta: a la venta, agotados o en curso, que no han terminado hace más de un día. */
    List<Evento> eventosParaPuerta();

    /** Boleto por su código, bloqueando la fila (dos puertas no lo validan a la vez, RN-07). */
    Optional<BoletoPuerta> buscarPorCodigo(Connection cn, String codigo);

    /** Boletos del evento que pertenecen al cliente con ese documento (RF-25). */
    List<BoletoPuerta> porDocumento(long idEvento, String documento);

    long registrar(Connection cn, long idEvento, Long idEntrada, String codigo, String puerta, long idUsuario,
            boolean valido, String motivo);

    void cambiarEstadoEntrada(Connection cn, long idEntrada, String estado);

    /** Últimos accesos registrados en el evento (bitácora de la puerta). */
    List<RegistroAcceso> recientes(long idEvento, int limite);

    /** Acceso bloqueado para anularlo (RF-29). */
    Optional<RegistroAcceso> buscarParaAnular(Connection cn, long idAcceso);

    void anular(Connection cn, long idAcceso, String motivo);

    /** Ingresos válidos y no anulados de un boleto. */
    int contarIngresosValidos(Connection cn, long idEntrada);

    /** Vendidos frente a ingresados por zona (RF-28). */
    List<OcupacionPuerta> ocupacion(long idEvento);
}
