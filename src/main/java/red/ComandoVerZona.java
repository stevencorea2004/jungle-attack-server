package red;

import game.Jugador;
import partida.EstadoPartida;

public class ComandoVerZona implements IComando {
    @Override
    public void ejecutar(ManejadorCliente cliente, Mensaje mensaje) {
        Jugador jugador = ComandoMover.jugadorEnPartida(cliente);
        if (jugador.getPartida().getEstado() != EstadoPartida.EN_JUEGO) {
            throw new IllegalStateException("La partida no está en juego");
        }
        DatosVerZona datos = (DatosVerZona) mensaje.getPayload(DatosVerZona.class);
        if (datos == null || jugador.getPartida().getMundo().getMapa().getZona(datos.idZona) == null) {
            throw new IllegalArgumentException("La zona solicitada no existe");
        }
        jugador.setZonaVista(datos.idZona);
        jugador.getPartida().enviarEstado(jugador);
    }
}
