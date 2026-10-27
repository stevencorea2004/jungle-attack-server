package game;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import mapa.Mapa;
import model.Animal;
import model.Bandera;
import model.ManadaBot;
import model.ManadaJugador;
import model.TazaCafe;
import util.IObserver;
import util.Observable;

public class MundoJuego extends Observable implements IObserver {
    private Mapa mapa;
    private final List<ManadaJugador> manadasJugador;
    private final List<ManadaBot> manadasBot;
    private final List<TazaCafe> tazas;
    private final List<Bandera> banderas;
    private final AtomicBoolean terminado;
    private Jugador ganador;

    public MundoJuego() {
        this.manadasJugador = new ArrayList<>();
        this.manadasBot = new ArrayList<>();
        this.tazas = new ArrayList<>();
        this.banderas = new ArrayList<>();
        this.terminado = new AtomicBoolean(false);
    }

    public List<ManadaJugador> getManadasJugador() {
        return manadasJugador;
    }

    public List<ManadaBot> getManadasBot() {
        return manadasBot;
    }

    public Mapa getMapa() {
        return mapa;
    }

    public void setMapa(Mapa mapa) {
        if (mapa == null) {
            throw new IllegalArgumentException("El mapa es obligatorio");
        }
        this.mapa = mapa;
    }

    public List<TazaCafe> getTazas() {
        return tazas;
    }

    public List<Bandera> getBanderas() {
        return banderas;
    }

    public List<Animal> getAnimalesVivos() {
        List<Animal> vivos = new ArrayList<>();
        for (ManadaJugador manada : manadasJugador) {
            if (manada != null) {
                vivos.addAll(manada.getVivos());
            }
        }
        for (ManadaBot manada : manadasBot) {
            if (manada != null) {
                vivos.addAll(manada.getVivos());
            }
        }
        return vivos;
    }

    public Animal buscarAnimal(int id) {
        for (Animal animal : getAnimalesVivos()) {
            if (animal != null && animal.getId() == id) {
                return animal;
            }
        }
        return null;
    }

    public boolean hayGanador() {
        return terminado.get() && ganador != null;
    }

    public Jugador getGanador() {
        return ganador;
    }

    public void terminar(Jugador ganador) {
        this.ganador = ganador;
        this.terminado.set(true);
        notifyObservers(this);
    }

    @Override
    public void update(Object source, Observable ob) {
        notifyObservers(source);
    }
}
