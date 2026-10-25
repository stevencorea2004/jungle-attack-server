package mapa;

import java.util.Vector;
import model.Punto;

public class Mapa {
    private Vector<Zona> zonas;
    private Vector<Camino> caminos;

    public Zona getZona(int id) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public Zona zonaEn(Punto p) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public Vector<Zona> getVecinos(int idZona) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public Camino getCamino(int zonaA, int zonaB) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }
}
