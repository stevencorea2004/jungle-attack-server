package model;

import game.Jugador;

public class Bandera extends EntidadJuego {
    private Jugador dueno;
    private String color;

    public Bandera(Jugador dueno, String color) {
        this.dueno = dueno;
        this.color = color;
    }

    public boolean esTocadaPor(Animal animal) {
        if (animal == null || !animal.estaVivo() || animal.posicion == null || this.posicion == null) {
            return false;
        }
        if (animal.manada instanceof ManadaJugador) {
            ManadaJugador manadaJugador = (ManadaJugador) animal.manada;
            if (manadaJugador != null && this.dueno != null && this.dueno.equals(manadaJugador.getDueno())) {
                return false;
            }
        }
        return animal.posicion.distancia(this.posicion) <= util.Constantes.RADIO_BANDERA;
    }

    public Jugador getDueno() {
        return dueno;
    }

    public String getColor() {
        return color;
    }
}
