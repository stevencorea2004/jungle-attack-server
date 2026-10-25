package model;

import util.Observable;

public abstract class EntidadJuego extends Observable {
    protected int id;
    protected Punto posicion;
    protected int idZona;

    public int getId() {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public Punto getPos() {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public boolean estaEnRadio(EntidadJuego otra, double radio) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }
}
