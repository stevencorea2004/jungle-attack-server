package model;

import util.Constantes;

public class TazaCafe extends EntidadJuego {
    private boolean consumida;

    public TazaCafe() {
        this.consumida = false;
    }

    public boolean consumir(ManadaJugador manada) {
        if (consumida || manada == null) {
            return false;
        }
        manada.addDanio(Constantes.BONO_DANIO_TAZA);
        consumida = true;
        notifyObservers(this);
        return true;
    }

    public boolean isConsumida() {
        return consumida;
    }
}
