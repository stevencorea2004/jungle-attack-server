package red;

import game.Jugador;
import java.util.Objects;
import partida.Partida;

public class ComandoElegirAnimal implements IComando {
    @Override
    public void ejecutar(ManejadorCliente cliente, Mensaje mensaje) {
        Jugador jugador = cliente.getJugador();
        if (jugador == null || jugador.getPartida() == null) {
            throw new IllegalStateException("La conexión no pertenece a una partida");
        }
        DatosElegirAnimal datos = (DatosElegirAnimal) mensaje.getPayload(DatosElegirAnimal.class);
        if (datos == null || datos.especie == null) {
            throw new IllegalArgumentException("La especie es obligatoria");
        }

        Partida partida = jugador.getPartida();
        partida.elegirEspecie(jugador, datos.especie);
        notificarSeleccion(partida);
    }

    static void notificarSeleccion(Partida partida) {
        Objects.requireNonNull(partida, "La partida es obligatoria");
        for (Jugador jugador : partida.getJugadores()) {
            if (jugador.getConexion() == null) {
                continue;
            }
            DatosSeleccion datos = new DatosSeleccion();
            datos.disponibles = partida.getEspeciesDisponibles();
            Jugador rival = partida.getRival(jugador);
            datos.eleccionRival = rival == null ? null : partida.getEleccion(rival);
            jugador.getConexion().enviar(FabricaMensajes.crear(TipoMensaje.ANIMAL_SELECTION, datos));
        }
    }
}
