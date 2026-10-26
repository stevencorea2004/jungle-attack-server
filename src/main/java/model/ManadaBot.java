package model;

import util.Constantes;

public class ManadaBot extends Manada {
    private int idZonaInicial;

    public ManadaBot(Especie especie, int idZonaInicial) {
        super(especie, Constantes.DANIO_BOT);
        this.idZonaInicial = idZonaInicial;
    }

    @Override
    public Animal crearMiembro(int id, Punto pos) {
        AnimalBot animal = new AnimalBot();
        animal.id = id;
        animal.posicion = pos;
        animal.energia = Constantes.ENERGIA_BOT;
        animal.especie = especie;
        animal.manada = this;
        return animal;
    }
}
