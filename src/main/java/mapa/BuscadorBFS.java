package mapa;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class BuscadorBFS implements IBuscadorRuta {
    @Override
    public Vector<Zona> buscarRuta(Mapa mapa, int zonaOrigen, int zonaDestino) {
        if (mapa == null || zonaOrigen == zonaDestino) {
            Zona zona = mapa == null ? null : mapa.getZona(zonaOrigen);
            Vector<Zona> ruta = new Vector<>();
            if (zona != null) {
                ruta.add(zona);
            }
            return ruta;
        }

        Vector<Integer> cola = new Vector<>();
        Map<Integer, Integer> prev = new HashMap<>();
        cola.add(zonaOrigen);
        prev.put(zonaOrigen, -1);

        while (!cola.isEmpty()) {
            int actual = cola.remove(0);
            if (actual == zonaDestino) {
                break;
            }
            for (Zona vecino : mapa.getVecinos(actual)) {
                if (vecino == null) {
                    continue;
                }
                int idVecino = vecino.getId();
                if (!prev.containsKey(idVecino)) {
                    prev.put(idVecino, actual);
                    cola.add(idVecino);
                }
            }
        }

        Vector<Zona> ruta = new Vector<>();
        Integer paso = zonaDestino;
        while (paso != null && paso != -1) {
            Zona z = mapa.getZona(paso);
            if (z != null) {
                ruta.add(0, z);
            }
            Integer siguiente = prev.get(paso);
            paso = siguiente == null || siguiente < 0 ? null : siguiente;
        }
        return ruta;
    }
}
