package model;

import game.Jugador;
import util.Constantes;

public class ManadaJugador extends Manada {
    private final Jugador dueno;

    public ManadaJugador(Especie especie, Jugador dueno) {
        super(especie, Constantes.DANIO_JUGADOR);
        this.dueno = dueno;
    }

    @Override
    public Animal crearMiembro(int id, Punto pos) {
        AnimalJugador animal = new AnimalJugador();
        animal.id = id;
        animal.posicion = pos;
        animal.energia = Constantes.ENERGIA_MAX_JUGADOR;
        animal.especie = especie;
        animal.manada = this;
        return animal;
    }

    public void aplicarBonoEnergia(int valor) {
        for (Animal animal : getVivos()) {
            if (animal != null) {
                animal.addEnergia(valor);
            }
        }
    }

    public Jugador getDueno() {
        return dueno;
    }
}
