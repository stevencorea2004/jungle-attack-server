package red;

import game.Jugador;
import java.util.Objects;
import partida.AdministradorPartidas;
import partida.Partida;

public class ComandoCrearPartida implements IComando {
    private final AdministradorPartidas administrador;

    public ComandoCrearPartida(AdministradorPartidas administrador) {
        this.administrador = Objects.requireNonNull(administrador, "El administrador de partidas es obligatorio");
    }

    @Override
    public void ejecutar(ManejadorCliente cliente, Mensaje mensaje) {
        if (cliente.getJugador() != null) {
            throw new IllegalStateException("Esta conexión ya está asociada a un jugador");
        }
        DatosCrearPartida datos = (DatosCrearPartida) mensaje.getPayload(DatosCrearPartida.class);
        if (datos == null || datos.nickname == null || datos.nickname.isBlank()) {
            throw new IllegalArgumentException("El nickname es obligatorio");
        }

        Jugador anfitrion = new Jugador();
        anfitrion.setNickname(datos.nickname.trim());
        anfitrion.setConexion(cliente);
        Partida partida = administrador.crearPartida(anfitrion);
        cliente.setJugador(anfitrion);
        cliente.setAlDesconectar(() -> partida.manejarDesconexion(anfitrion));

        DatosPartidaCreada respuesta = new DatosPartidaCreada();
        respuesta.codigo = partida.getCodigo();
        cliente.enviar(FabricaMensajes.crear(TipoMensaje.MATCH_CREATED, respuesta));
    }
}
