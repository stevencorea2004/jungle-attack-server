package mapa;

import java.util.Vector;

public interface IBuscadorRuta {
    Vector<Zona> buscarRuta(Mapa mapa, int zonaOrigen, int zonaDestino);
}
