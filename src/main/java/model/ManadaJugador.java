package model;

import game.Jugador;

public class ManadaJugador extends Manada {
    private Jugador dueno;

    @Override
    public Animal crearMiembro(int id, Punto pos) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public void aplicarBonoEnergia(int valor) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }
}
