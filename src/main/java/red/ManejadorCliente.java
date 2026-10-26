package red;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

import game.Jugador;

public class ManejadorCliente implements Runnable {
    private Socket socket;
    private BufferedReader lector;
    private PrintWriter escritor;
    private EnrutadorMensajes enrutador;
    private Jugador jugador;

    public ManejadorCliente(Socket socket2, EnrutadorMensajes enrutador2) {
        //TODO Auto-generated constructor stub
    }

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
