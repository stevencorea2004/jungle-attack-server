package model;

import util.Constantes;

public class AnimalJugador extends Animal {
    private Animal objetivo;

    public void setObjetivo(Animal objetivo) {
        this.objetivo = objetivo;
    }

    public void limpiarObjetivo() {
        this.objetivo = null;
    }

    public Animal getObjetivo() {
        return objetivo;
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
