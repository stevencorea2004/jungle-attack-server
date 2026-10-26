package model;

import util.Observable;

public abstract class EntidadJuego extends Observable {
    protected int id;
    protected Punto posicion;
    protected int idZona;

    public int getId() {
        return id;
    }

    public Punto getPos() {
        return posicion;
    }

    public boolean estaEnRadio(EntidadJuego otra, double radio) {
        if (otra == null || this.posicion == null || otra.posicion == null) {
            return false;
        }
        return this.posicion.distancia(otra.posicion) <= radio;
    }
}
