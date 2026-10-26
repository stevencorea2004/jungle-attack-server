package mapa;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class BuscadorDijkstra implements IBuscadorRuta {
    @Override
    public Vector<Zona> buscarRuta(Mapa mapa, int zonaOrigen, int zonaDestino) {
        if (mapa == null) {
            return new Vector<>();
        }
        if (zonaOrigen == zonaDestino) {
            Zona z = mapa.getZona(zonaOrigen);
            Vector<Zona> ruta = new Vector<>();
            if (z != null) {
                ruta.add(z);
            }
            return ruta;
        }

        Map<Integer, Double> dist = new HashMap<>();
        Map<Integer, Integer> prev = new HashMap<>();
        Vector<Integer> visitados = new Vector<>();
        dist.put(zonaOrigen, 0.0);

        while (true) {
            Integer actual = null;
            double mejor = Double.MAX_VALUE;
            for (Integer id : dist.keySet()) {
                if (!visitados.contains(id) && dist.get(id) < mejor) {
                    mejor = dist.get(id);
                    actual = id;
                }
            }
            if (actual == null) {
                break;
            }
            visitados.add(actual);
            if (actual == zonaDestino) {
                break;
            }
            for (Zona vecino : mapa.getVecinos(actual)) {
                int idVecino = vecino.getId();
                Camino camino = mapa.getCamino(actual, idVecino);
                double peso = camino == null ? 1.0 : camino.getLargo();
                double nuevo = dist.get(actual) + peso;
                if (!dist.containsKey(idVecino) || nuevo < dist.get(idVecino)) {
                    dist.put(idVecino, nuevo);
                    prev.put(idVecino, actual);
                }
            }
        }

        Vector<Zona> ruta = new Vector<>();
        Integer paso = zonaDestino;
        while (paso != null) {
            Zona z = mapa.getZona(paso);
            if (z != null) {
                ruta.add(0, z);
            }
            if (paso == zonaOrigen) {
                break;
            }
            paso = prev.get(paso);
        }
        return ruta;
    }
}
