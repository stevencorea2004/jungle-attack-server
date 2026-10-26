package game;

import java.util.Vector;
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
    private Vector<ManadaJugador> manadasJugador;
    private Vector<ManadaBot> manadasBot;
    private Vector<TazaCafe> tazas;
    private Vector<Bandera> banderas;
    private AtomicBoolean terminado;
    private Jugador ganador;

    public MundoJuego() {
        this.manadasJugador = new Vector<>();
        this.manadasBot = new Vector<>();
        this.tazas = new Vector<>();
        this.banderas = new Vector<>();
        this.terminado = new AtomicBoolean(false);
    }

    public Vector<ManadaJugador> getManadasJugador() {
        return manadasJugador;
    }

    public Vector<ManadaBot> getManadasBot() {
        return manadasBot;
    }

    public Vector<Animal> getAnimalesVivos() {
        Vector<Animal> vivos = new Vector<>();
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
