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
    private final String codigo;
    private volatile EstadoPartida estado;
    private final Vector<Jugador> jugadores;
    private final SelectorEspecies selector;
    private MundoJuego mundo;
    private CicloJuego ciclo;
    private long inicio;

    public Partida(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código de partida es obligatorio");
        }
        this.codigo = GeneradorCodigo.normalizar(codigo);
        this.estado = EstadoPartida.ESPERANDO_JUGADOR;
        this.jugadores = new Vector<>();
        this.selector = new SelectorEspecies();
    }

    public synchronized void agregarJugador(Jugador jugador) {
        if (jugador == null) {
            throw new IllegalArgumentException("El jugador es obligatorio");
        }
        if (estado != EstadoPartida.ESPERANDO_JUGADOR || estaLlena()) {
            throw new IllegalStateException("La partida ya está llena o no acepta jugadores");
        }
        if (jugadores.contains(jugador)) {
            throw new IllegalArgumentException("El jugador ya pertenece a esta partida");
        }
        if (jugador.getPartida() != null && jugador.getPartida() != this) {
            throw new IllegalArgumentException("El jugador ya pertenece a otra partida");
        }
        jugadores.add(jugador);
        jugador.setPartida(this);
        if (estaLlena()) {
            estado = EstadoPartida.ELIGIENDO_ANIMALES;
        }
    }

    public synchronized boolean estaLlena() {
        return jugadores.size() >= 2;
    }

    public synchronized void elegirEspecie(Jugador jugador, Especie especie) {
        if (estado != EstadoPartida.ELIGIENDO_ANIMALES) {
            throw new IllegalStateException("La partida no está en fase de selección");
        }
        if (!jugadores.contains(jugador)) {
            throw new IllegalArgumentException("El jugador no pertenece a esta partida");
        }
        if (!selector.elegir(jugador, especie)) {
            throw new IllegalArgumentException("La especie no está disponible para este jugador");
        }
        jugador.setEspecie(especie);
    }

    public synchronized Jugador getRival(Jugador jugador) {
        if (!jugadores.contains(jugador)) {
            return null;
        }
        for (Jugador participante : jugadores) {
            if (participante != jugador) {
                return participante;
            }
        }
        return null;
    }

    public synchronized Vector<Jugador> getJugadores() {
        return new Vector<>(jugadores);
    }

    public synchronized Vector<Especie> getEspeciesDisponibles() {
        return selector.getDisponibles();
    }

    public synchronized boolean todosEligieron() {
        return selector.todosEligieron();
    }

    public synchronized Especie getEleccion(Jugador jugador) {
        return selector.getEleccion(jugador);
    }

    public String getCodigo() {
        return codigo;
    }

    public EstadoPartida getEstado() {
        return estado;
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
