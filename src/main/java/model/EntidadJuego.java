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

    public int getIdZona() {
        return idZona;
    }

    public void setUbicacion(int id, Punto posicion, int idZona) {
        if (posicion == null || !Double.isFinite(posicion.getX()) || !Double.isFinite(posicion.getY())) {
            throw new IllegalArgumentException("La posición debe tener coordenadas finitas");
        }
        this.id = id;
        this.posicion = new Punto(posicion.getX(), posicion.getY());
        this.idZona = idZona;
    }

    public boolean estaEnRadio(EntidadJuego otra, double radio) {
        if (otra == null || this.posicion == null || otra.posicion == null) {
            return false;
        }
        return this.posicion.distancia(otra.posicion) <= radio;
    }
}
