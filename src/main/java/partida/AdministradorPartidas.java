package partida;

import game.Jugador;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AdministradorPartidas {
    private final ConcurrentHashMap<String, Partida> partidas = new ConcurrentHashMap<>();
    private final GeneradorCodigo generador;

    public AdministradorPartidas() {
        this(new GeneradorCodigo());
    }

    public AdministradorPartidas(GeneradorCodigo generador) {
        if (generador == null) {
            throw new IllegalArgumentException("El generador de códigos es obligatorio");
        }
        this.generador = generador;
    }

    public synchronized Partida crearPartida(Jugador anfitrion) {
        validarJugadorNuevo(anfitrion);
        Set<String> existentes = partidas.keySet();
        String codigo = generador.generar(existentes);
        Partida partida = new Partida(codigo);
        partida.setAlTerminar(() -> partidas.remove(codigo, partida));
        partida.agregarJugador(anfitrion);
        partidas.put(codigo, partida);
        return partida;
    }

    public synchronized Partida unirse(String codigo, Jugador invitado) {
        validarJugadorNuevo(invitado);
        String normalizado = GeneradorCodigo.normalizar(codigo);
        Partida partida = normalizado == null ? null : partidas.get(normalizado);
        if (partida == null) {
            throw new IllegalArgumentException("No existe una partida con ese código");
        }
        partida.agregarJugador(invitado);
        return partida;
    }

    public Partida buscar(String codigo) {
        String normalizado = GeneradorCodigo.normalizar(codigo);
        return normalizado == null ? null : partidas.get(normalizado);
    }

    public void eliminar(String codigo) {
        String normalizado = GeneradorCodigo.normalizar(codigo);
        if (normalizado != null) {
            partidas.remove(normalizado);
        }
    }

    public int getCantidadActivas() {
        return partidas.size();
    }

    private static void validarJugadorNuevo(Jugador jugador) {
        if (jugador == null) {
            throw new IllegalArgumentException("El jugador es obligatorio");
        }
        if (jugador.getPartida() != null) {
            throw new IllegalArgumentException("El jugador ya pertenece a una partida");
        }
    }
}
