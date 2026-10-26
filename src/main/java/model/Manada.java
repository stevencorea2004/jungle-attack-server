package model;

import java.util.Random;
import java.util.Vector;
import mapa.Zona;
import util.Constantes;

public abstract class Manada {
    protected Especie especie;
    protected Vector<Animal> miembros;
    protected int danio;

    public Manada(Especie especie, int danio) {
        this.especie = especie;
        this.miembros = new Vector<>();
        this.danio = danio;
    }

    public Vector<Animal> getVivos() {
        Vector<Animal> vivos = new Vector<>();
        for (Animal animal : miembros) {
            if (animal != null && animal.estaVivo()) {
                vivos.add(animal);
            }
        }
        return vivos;
    }

    public boolean estaEliminada() {
        return getVivos().isEmpty();
    }

    public void addDanio(int valor) {
        if (valor != 0) {
            this.danio += valor;
        }
    }

    public void crearMiembros(Zona zona, Random random) {
        if (zona == null) {
            return;
        }
        for (int i = 0; i < Constantes.TAM_MANADA_JUGADOR; i++) {
            Punto pos = zona.puntoAleatorio(random);
            Animal animal = crearMiembro(i, pos);
            if (animal != null) {
                animal.id = i;
                animal.posicion = pos;
                animal.idZona = zona.getId();
                animal.manada = this;
                animal.especie = this.especie;
                if (animal.energia <= 0) {
                    animal.energia = animal.getEnergiaMax();
                }
                miembros.add(animal);
            }
        }
    }

    public abstract Animal crearMiembro(int id, Punto pos);
}
