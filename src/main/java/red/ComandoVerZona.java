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
        if (datos == null || !jugador.puedeVer(datos.idZona)) {
            throw new IllegalArgumentException("El jugador no tiene animales vivos en esa zona");
        }
        jugador.setZonaVista(datos.idZona);
        jugador.getPartida().enviarEstado(jugador);
    }
}
