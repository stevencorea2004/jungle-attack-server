package red;

import game.Jugador;

public class ComandoSalirPartida implements IComando {
    @Override
    public void ejecutar(ManejadorCliente cliente, Mensaje mensaje) {
        Jugador jugador = ComandoMover.jugadorEnPartida(cliente);
        jugador.getPartida().abandonar(jugador);
    }
}
