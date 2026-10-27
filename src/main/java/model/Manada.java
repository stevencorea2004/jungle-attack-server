package model;

import java.util.Random;
import java.util.Vector;
import java.util.function.Supplier;
import mapa.Mapa;
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

    public Vector<Animal> getMiembros() {
        return new Vector<>(miembros);
    }

    public int getDanio() {
        return danio;
    }

    protected int getTamano() {
        return Constantes.TAM_MANADA_JUGADOR;
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
        crearMiembros(zona.getId(), () -> zona.puntoAleatorio(random));
    }

    public void crearMiembros(Zona zona, Random random, Mapa mapa) {
        if (zona == null || mapa == null) {
            throw new IllegalArgumentException("La zona y el mapa son obligatorios");
        }
        crearMiembros(zona.getId(), () -> mapa.puntoAleatorioEnZona(zona.getId(), random));
    }

    private void crearMiembros(int idZona, Supplier<Punto> elegirPosicion) {
        for (int i = 0; i < getTamano(); i++) {
            Punto pos = elegirPosicion.get();
            Animal animal = crearMiembro(i, pos);
            if (animal != null) {
                animal.id = especie.ordinal() * Constantes.TAM_MANADA_JUGADOR + i;
                animal.posicion = pos;
                animal.idZona = idZona;
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
