package model;

import game.Jugador;

public class Bandera extends EntidadJuego {
    private Jugador dueno;
    private String color;

    public boolean esTocadaPor(Animal animal) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }
}
