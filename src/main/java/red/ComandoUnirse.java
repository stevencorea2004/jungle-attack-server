package red;

import game.Jugador;
import java.util.Objects;
import partida.AdministradorPartidas;
import partida.Partida;

public class ComandoUnirse implements IComando {
    private final AdministradorPartidas administrador;

    public ComandoUnirse(AdministradorPartidas administrador) {
        this.administrador = Objects.requireNonNull(administrador, "El administrador de partidas es obligatorio");
    }

    @Override
    public void ejecutar(ManejadorCliente cliente, Mensaje mensaje) {
        if (cliente.getJugador() != null) {
            throw new IllegalStateException("Esta conexión ya está asociada a un jugador");
        }
        DatosUnirse datos = (DatosUnirse) mensaje.getPayload(DatosUnirse.class);
        if (datos == null || datos.nickname == null || datos.nickname.isBlank()) {
            throw new IllegalArgumentException("El nickname es obligatorio");
        }
        if (datos.codigo == null || datos.codigo.isBlank()) {
            throw new IllegalArgumentException("El código de partida es obligatorio");
        }

        Jugador invitado = new Jugador();
        invitado.setNickname(datos.nickname.trim());
        invitado.setConexion(cliente);
        Partida partida = administrador.unirse(datos.codigo, invitado);
        cliente.setJugador(invitado);

        DatosPartidaCreada respuesta = new DatosPartidaCreada();
        respuesta.codigo = partida.getCodigo();
        for (Jugador jugador : partida.getJugadores()) {
            if (jugador.getConexion() != null) {
                jugador.getConexion().enviar(FabricaMensajes.crear(TipoMensaje.MATCH_JOINED, respuesta));
            }
        }
        ComandoElegirAnimal.notificarSeleccion(partida);
    }
}
