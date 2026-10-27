package model;

import mapa.Ruta;
import util.Constantes;

public abstract class Animal extends EntidadJuego {
    protected Especie especie;
    protected int energia;
    protected Manada manada;
    protected long ultimoAtaque;
    protected Ruta ruta;
    protected Animal ultimoAtacante;

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
        objetivo.ultimoAtacante = this;
        objetivo.recibirDanio(danio);
        notifyObservers(this);
        return true;
    }

    public void setRuta(Ruta ruta) {
        this.ruta = ruta;
    }

    public Ruta getRuta() {
        return ruta;
    }

    public Animal getUltimoAtacante() {
        return ultimoAtacante;
    }

    public int getEnergia() {
        return energia;
    }

    public Especie getEspecie() {
        return especie;
    }

    public Manada getManada() {
        return manada;
    }

    public void setIdZona(int idZona) {
        this.idZona = idZona;
    }

    public Punto getPosicion() {
        return posicion;
    }

    public void avanzar(double deltaSeg) {
        if (ruta == null || deltaSeg <= 0 || !estaVivo()) {
            return;
        }
        double distancia = Constantes.VELOCIDAD_ANIMAL * deltaSeg;
        this.posicion = ruta.mover(this.posicion, distancia);
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
