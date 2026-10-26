package partida;

import game.Jugador;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import model.Especie;

public class SelectorEspecies {
    private final Set<Especie> disponibles = EnumSet.allOf(Especie.class);
    private final Map<Jugador, Especie> elecciones = new java.util.HashMap<>();

    public synchronized boolean elegir(Jugador jugador, Especie especie) {
        if (jugador == null || especie == null) {
            return false;
        }
        Especie eleccionAnterior = elecciones.get(jugador);
        if (eleccionAnterior != null) {
            return eleccionAnterior == especie;
        }
        if (!disponibles.remove(especie)) {
            return false;
        }
        elecciones.put(jugador, especie);
        return true;
    }

    public synchronized boolean todosEligieron() {
        return elecciones.size() >= 2;
    }

    public synchronized Vector<Especie> especiesBot() {
        return new Vector<>(disponibles);
    }

    public synchronized Vector<Especie> getDisponibles() {
        return new Vector<>(disponibles);
    }

    public synchronized Especie getEleccion(Jugador jugador) {
        return elecciones.get(jugador);
    }
}
