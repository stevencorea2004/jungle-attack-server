package red;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import game.Jugador;
import util.Constantes;

/** Atiende a un cliente conectado en su propio hilo. Un mensaje = una línea de JSON. */
public class ManejadorCliente implements Runnable {
    private Socket socket;
    private BufferedReader lector;
    private PrintWriter escritor;
    private EnrutadorMensajes enrutador;
    private Jugador jugador;
    private final AtomicBoolean cerrado = new AtomicBoolean(false);
    private Runnable alDesconectar = () -> { };

    public ManejadorCliente(Socket socket, EnrutadorMensajes enrutador) throws IOException {
        this.socket = socket;
        this.enrutador = enrutador;
        this.lector = new BufferedReader(new InputStreamReader(socket.getInputStream(), Constantes.CHARSET));
        this.escritor = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), Constantes.CHARSET), true);
    }

    @Override
    public void run() {
        try {
            String linea;
            while ((linea = lector.readLine()) != null) {
                procesar(linea);
            }
        } catch (IOException e) {
            if (!cerrado.get()) {
                System.err.println("Error leyendo mensajes del cliente: " + e.getMessage());
            }
        } catch (Error e) {
            System.err.println("Error fatal atendiendo al cliente: " + e);
            throw e;
        } finally {
            cerrar();
        }
    }

    private void procesar(String linea) {
        try {
            Mensaje mensaje = CodificadorJson.getInstancia().decodificar(linea);
            if (mensaje == null) {
                return; // línea vacía
            }
            enrutador.enrutar(this, mensaje);
        } catch (RuntimeException e) {
            System.err.println("No se pudo procesar el mensaje del cliente: " + e.getMessage());
            enviar(FabricaMensajes.error("No se pudo procesar el mensaje: " + e.getMessage()));
        }
    }

    /** synchronized: el hilo de este cliente y el ciclo del juego pueden enviar a la vez. */
    public synchronized void enviar(Mensaje mensaje) {
        escritor.println(CodificadorJson.getInstancia().codificar(mensaje));
    }

    public void cerrar() {
        if (!cerrado.compareAndSet(false, true)) {
            return;
        }
        try {
            socket.close();
        } catch (IOException e) {
            // Ya estaba cerrado.
        }
        alDesconectar.run();
    }

    /** Registra acciones que deben ejecutarse al desconectar al cliente. */
    public void setAlDesconectar(Runnable alDesconectar) {
        Runnable anterior = this.alDesconectar;
        this.alDesconectar = () -> {
            anterior.run();
            if (alDesconectar != null) {
                alDesconectar.run();
            }
        };
    }

    public Jugador getJugador() {
        return jugador;
    }

    public void setJugador(Jugador jugador) {
        this.jugador = jugador;
    }

    public boolean estaCerrado() {
        return cerrado.get();
    }
}