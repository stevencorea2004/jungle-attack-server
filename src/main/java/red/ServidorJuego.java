package red;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import partida.AdministradorPartidas;

/** Acepta conexiones y crea un ManejadorCliente por cada una. */
public class ServidorJuego {
    private int puerto;
    private ServerSocket serverSocket;
    private ExecutorService hilos;
    private AdministradorPartidas administrador;
    private EnrutadorMensajes enrutador;
    private volatile boolean activo;
    private final Set<ManejadorCliente> clientes = ConcurrentHashMap.newKeySet();

    /** Con puerto 0 el sistema escoge uno libre (útil en las pruebas). */
    public ServidorJuego(int puerto, AdministradorPartidas administrador, EnrutadorMensajes enrutador) {
        this.puerto = puerto;
        this.administrador = administrador;
        this.enrutador = enrutador;
    }

    /** Abre el puerto y acepta conexiones en otro hilo; no bloquea. */
    public void iniciar() throws IOException {
        serverSocket = new ServerSocket(puerto);
        hilos = Executors.newCachedThreadPool();
        activo = true;
        hilos.submit(this::aceptarConexiones);
    }

    private void aceptarConexiones() {
        while (activo) {
            try {
                Socket socket = serverSocket.accept();
                ManejadorCliente cliente = new ManejadorCliente(socket, enrutador);
                clientes.add(cliente);
                cliente.setAlDesconectar(() -> clientes.remove(cliente));
                hilos.submit(cliente);
            } catch (IOException e) {
                if (activo) {
                    System.err.println("Error aceptando conexión: " + e.getMessage());
                }
            }
        }
    }

    public void detener() {
        activo = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            // Ya estaba cerrado.
        }
        clientes.forEach(ManejadorCliente::cerrar);
        if (hilos != null) {
            hilos.shutdownNow();
        }
    }

    /** El puerto real, aunque se haya pedido el 0. */
    public int getPuerto() {
        return serverSocket.getLocalPort();
    }

    public int getCantidadClientes() {
        return clientes.size();
    }

    public AdministradorPartidas getAdministrador() {
        return administrador;
    }
}
