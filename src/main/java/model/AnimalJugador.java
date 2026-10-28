package model;

import util.Constantes;

public class AnimalJugador extends Animal {
    private Animal objetivo;
    private boolean atacando;

    public void setObjetivo(Animal objetivo) {
        this.objetivo = objetivo;
        this.atacando = false;
    }

    public void limpiarObjetivo() {
        this.objetivo = null;
        this.atacando = false;
    }

    public Animal getObjetivo() {
        return objetivo;
    }

    public boolean isAtacando() {
        return atacando;
    }

    public void setAtacando(boolean atacando) {
        this.atacando = atacando;
    }

    @Override
    public int getEnergiaMax() {
        return Constantes.ENERGIA_MAX_JUGADOR;
    }

    @Override
    public boolean esBot() {
        return false;
    }
}
