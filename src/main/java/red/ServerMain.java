package red;

import java.io.IOException;

import partida.AdministradorPartidas;
import util.Constantes;

public final class ServerMain {
    private ServerMain() {
    }

    public static void main(String[] args) throws IOException {
        AdministradorPartidas administrador = new AdministradorPartidas();

        EnrutadorMensajes enrutador = new EnrutadorMensajes();
        enrutador.registrar(TipoMensaje.CREATE_MATCH, new ComandoCrearPartida(administrador));
        enrutador.registrar(TipoMensaje.JOIN_MATCH, new ComandoUnirse(administrador));
        enrutador.registrar(TipoMensaje.SELECT_ANIMAL, new ComandoElegirAnimal());
        enrutador.registrar(TipoMensaje.MOVE_ANIMALS, new ComandoMover());
        enrutador.registrar(TipoMensaje.ATTACK, new ComandoAtacar());
        enrutador.registrar(TipoMensaje.VIEW_ZONE, new ComandoVerZona());
        enrutador.registrar(TipoMensaje.LEAVE_MATCH, new ComandoSalirPartida());

        ServidorJuego servidor = new ServidorJuego(Constantes.PUERTO, administrador, enrutador);
        servidor.iniciar();
        Runtime.getRuntime().addShutdownHook(new Thread(servidor::detener));
        System.out.println("Servidor escuchando en el puerto " + servidor.getPuerto());
    }
}
