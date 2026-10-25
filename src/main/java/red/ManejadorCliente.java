package red;

import game.Jugador;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ManejadorCliente implements Runnable {
    private Socket socket;
    private BufferedReader lector;
    private PrintWriter escritor;
    private EnrutadorMensajes enrutador;
    private Jugador jugador;

    @Override
    public void run() {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public void enviar(Mensaje mensaje) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public void cerrar() {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }
}
