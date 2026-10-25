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

    public Vector<Animal> getAnimalesVivos() {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public Animal buscarAnimal(int id) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public boolean hayGanador() {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public Jugador getGanador() {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public void terminar(Jugador ganador) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    @Override
    public void update(Object source, Observable ob) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }
}
