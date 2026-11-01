package sv.edu.itca.ticka.servicio;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import sv.edu.itca.ticka.modelo.Asiento;
import sv.edu.itca.ticka.modelo.FilaAsientos;
import sv.edu.itca.ticka.modelo.PrecioZona;
import sv.edu.itca.ticka.modelo.ZonaMapa;

/**
 * Arma el mapa de asientos de un evento (zonas → filas → asientos) a partir
 * de los asientos ya ordenados. Lo usan el Organizador (bloqueos, RF-08), el
 * cliente (elegir asientos, RF-16) y la venta en mostrador (RF-24).
 */
public final class MapaAsientos {

    private MapaAsientos() {
    }

    /** Agrupa los asientos por zona y fila, con el precio de cada zona. */
    public static List<ZonaMapa> armarZonas(List<PrecioZona> precios, List<Asiento> asientos) {
        Map<Long, ZonaMapa> zonas = new LinkedHashMap<>();
        for (PrecioZona p : precios) {
            ZonaMapa z = new ZonaMapa(p.getIdZona(), p.getZona());
            z.setPrecio(p.getPrecio());
            zonas.put(p.getIdZona(), z);
        }
        for (Asiento a : asientos) {
            ZonaMapa z = zonas.get(a.getIdZona());
            if (z == null) {
                continue;
            }
            List<FilaAsientos> filas = z.getFilas();
            if (filas.isEmpty() || !filas.get(filas.size() - 1).getLetra().equals(a.getFila())) {
                filas.add(new FilaAsientos(a.getFila()));
            }
            filas.get(filas.size() - 1).getAsientos().add(a);
        }
        // Solo se muestran las zonas que tienen asientos en este evento.
        zonas.values().removeIf(z -> z.getFilas().isEmpty());
        return List.copyOf(zonas.values());
    }

    /** Cantidad de asientos por estado (Disponible, Reservado, Vendido, Bloqueado). */
    public static Map<String, Integer> contarPorEstado(List<Asiento> asientos) {
        Map<String, Integer> conteo = new TreeMap<>();
        for (String estado : new String[]{"Disponible", "Reservado", "Vendido", "Bloqueado"}) {
            conteo.put(estado, 0);
        }
        for (Asiento a : asientos) {
            // Los asientos que el propio cliente tiene reservados cuentan como reservados.
            String estado = "Seleccionado".equals(a.getEstado()) ? "Reservado" : a.getEstado();
            conteo.merge(estado, 1, Integer::sum);
        }
        return conteo;
    }
}
