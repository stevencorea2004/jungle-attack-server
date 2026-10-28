package red;

import game.Jugador;
import partida.EstadoPartida;
import partida.Partida;

public class ComandoDeseleccionarAnimal implements IComando {
    @Override
    public void ejecutar(ManejadorCliente cliente, Mensaje mensaje) {
        Jugador jugador = ComandoMover.jugadorEnPartida(cliente);
        Partida partida = jugador.getPartida();
        if (partida.getEstado() == EstadoPartida.EN_JUEGO) {
            return;
        }
        partida.deseleccionarEspecie(jugador);
        ComandoElegirAnimal.notificarSeleccion(partida);
    }
}
