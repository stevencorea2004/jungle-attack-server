package model;

import mapa.Ruta;

public abstract class Animal extends EntidadJuego {
    protected Especie especie;
    protected int energia;
    protected Manada manada;
    protected long ultimoAtaque;
    protected Ruta ruta;

    public boolean estaVivo() {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public void recibirDanio(int valor) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public boolean puedeAtacar(long ahora) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public boolean atacar(Animal objetivo, long ahora) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public void setRuta(Ruta ruta) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public void avanzar(double deltaSeg) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public void addEnergia(int valor) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public abstract int getEnergiaMax();

    public abstract boolean esBot();
}
