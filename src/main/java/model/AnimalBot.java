package model;

import util.Constantes;

public class AnimalBot extends Animal {
    private Animal objetivo;
    private boolean atacando;

    public Animal getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(Animal objetivo) {
        this.objetivo = objetivo;
    }

    public boolean isAtacando() {
        return atacando;
    }

    public void setAtacando(boolean atacando) {
        this.atacando = atacando;
    }

    public void limpiarObjetivo() {
        objetivo = null;
        atacando = false;
    }

    @Override
    public int getEnergiaMax() {
        return Constantes.ENERGIA_BOT;
    }

    @Override
    public boolean esBot() {
        return true;
    }
}
