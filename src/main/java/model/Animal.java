package model;

import mapa.Ruta;
import util.Constantes;

public abstract class Animal extends EntidadJuego {
    protected Especie especie;
    protected int energia;
    protected Manada manada;
    protected long ultimoAtaque;
    protected Ruta ruta;

    public boolean estaVivo() {
        return energia > 0;
    }

    public void recibirDanio(int valor) {
        if (valor <= 0 || !estaVivo()) {
            return;
        }
        energia = Math.max(0, energia - valor);
        if (!estaVivo()) {
            notifyObservers(this);
        }
    }

    public boolean puedeAtacar(long ahora) {
        return ahora - ultimoAtaque >= Constantes.COOLDOWN_ATAQUE_MS;
    }

    public boolean atacar(Animal objetivo, long ahora) {
        if (objetivo == null || !objetivo.estaVivo() || !puedeAtacar(ahora)) {
            return false;
        }
        int danio = this.manada != null ? this.manada.danio : 0;
        if (danio <= 0) {
            return false;
        }
        this.ultimoAtaque = ahora;
        objetivo.recibirDanio(danio);
        notifyObservers(this);
        return true;
    }

    public void setRuta(Ruta ruta) {
        this.ruta = ruta;
    }

    public void avanzar(double deltaSeg) {
        if (ruta == null || deltaSeg <= 0 || !estaVivo()) {
            return;
        }
        Punto destino = ruta.puntoActual();
        if (destino == null) {
            return;
        }
        double distancia = Constantes.VELOCIDAD_ANIMAL * deltaSeg;
        Punto nuevo = this.posicion.moverHacia(destino, distancia);
        this.posicion = nuevo;
        if (this.posicion.distancia(destino) <= 0.0001) {
            ruta.avanzar();
        }
        notifyObservers(this);
    }

    public void addEnergia(int valor) {
        if (valor <= 0) {
            return;
        }
        this.energia = Math.min(getEnergiaMax(), this.energia + valor);
    }

    public abstract int getEnergiaMax();

    public abstract boolean esBot();
}
