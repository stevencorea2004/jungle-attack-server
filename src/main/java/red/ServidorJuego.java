package red;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import partida.AdministradorPartidas;

public class ServidorJuego {
    private int puerto;
    private ServerSocket serverSocket;
    private ExecutorService hilos;
    private AdministradorPartidas administrador;
    private EnrutadorMensajes enrutador;
    private boolean activo;

    public void iniciar() {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public void detener() {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }
}
