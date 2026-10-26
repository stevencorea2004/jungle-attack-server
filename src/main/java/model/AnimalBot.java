package model;

import util.Constantes;

public class AnimalBot extends Animal {
    @Override
    public int getEnergiaMax() {
        return Constantes.ENERGIA_BOT;
    }

    @Override
    public boolean esBot() {
        return true;
    }
}
