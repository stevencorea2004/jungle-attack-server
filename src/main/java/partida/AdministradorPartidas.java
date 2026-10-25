package partida;

import game.Jugador;
import java.util.concurrent.ConcurrentHashMap;

public class AdministradorPartidas {
    private ConcurrentHashMap<String, Partida> partidas;
    private GeneradorCodigo generador;

    public Partida crearPartida(Jugador anfitrion) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public Partida unirse(String codigo, Jugador invitado) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public Partida buscar(String codigo) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public void eliminar(String codigo) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public int getCantidadActivas() {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }
}
