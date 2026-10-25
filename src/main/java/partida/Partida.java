package partida;

import game.CicloJuego;
import game.Jugador;
import game.MundoJuego;
import java.util.Vector;
import model.Especie;
import red.ResumenPartida;
import util.IObserver;
import util.Observable;

public class Partida implements IObserver {
    private String codigo;
    private EstadoPartida estado;
    private Vector<Jugador> jugadores;
    private SelectorEspecies selector;
    private MundoJuego mundo;
    private CicloJuego ciclo;
    private long inicio;

    public void agregarJugador(Jugador jugador) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public boolean estaLlena() {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public void elegirEspecie(Jugador jugador, Especie especie) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public void iniciar() {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public void enviarEstado() {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public void terminar(Jugador ganador) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public void manejarDesconexion(Jugador jugador) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    @Override
    public void update(Object source, Observable ob) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }
}
