package model;

import java.util.Random;
import java.util.Vector;
import mapa.Zona;

public abstract class Manada {
    protected Especie especie;
    protected Vector<Animal> miembros;
    protected int danio;

    public Vector<Animal> getVivos() {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public boolean estaEliminada() {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public void addDanio(int valor) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public void crearMiembros(Zona zona, Random random) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public abstract Animal crearMiembro(int id, Punto pos);
}
