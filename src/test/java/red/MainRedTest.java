package red;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import partida.AdministradorPartidas;
import util.Constantes;

class MainRedTest {

    private ServidorJuego servidor;
    private Socket socket;
    private PrintWriter escritor;
    private BufferedReader lector;
    private AdministradorPartidas administrador;

    @BeforeEach
    void iniciarServidor() throws Exception {
        administrador = new AdministradorPartidas();
        EnrutadorMensajes enrutador = new EnrutadorMensajes();
        // Comando de prueba: responde MATCH_CREATED con un código fijo.
        enrutador.registrar(TipoMensaje.CREATE_MATCH, (cliente, mensaje) -> {
            DatosPartidaCreada datos = new DatosPartidaCreada();
            datos.codigo = "A7K2";
            cliente.enviar(FabricaMensajes.crear(TipoMensaje.MATCH_CREATED, datos));
        });
        enrutador.registrar(TipoMensaje.JOIN_MATCH, new ComandoUnirse(administrador));

        servidor = new ServidorJuego(0, administrador, enrutador);
        servidor.iniciar();

        socket = new Socket("localhost", servidor.getPuerto());
        socket.setSoTimeout(2000);
        escritor = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), Constantes.CHARSET), true);
        lector = new BufferedReader(new InputStreamReader(socket.getInputStream(), Constantes.CHARSET));
    }

    @AfterEach
    void detenerServidor() throws Exception {
        socket.close();
        servidor.detener();
    }

    private Mensaje leer() throws Exception {
        return CodificadorJson.getInstancia().decodificar(lector.readLine());
    }

    @Test
    void elServidorEnrutaElMensajeYResponde() throws Exception {
        escritor.println("{\"tipo\":\"CREATE_MATCH\",\"payload\":{\"nickname\":\"Steven\"}}");

        Mensaje respuesta = leer();

        assertEquals(TipoMensaje.MATCH_CREATED, respuesta.getTipo());
        assertEquals("A7K2", ((DatosPartidaCreada) respuesta.getPayload(DatosPartidaCreada.class)).codigo);
    }

    @Test
    void tipoSinComandoRespondeError() throws Exception {
        escritor.println("{\"tipo\":\"ATTACK\",\"payload\":{}}");

        Mensaje respuesta = leer();

        assertEquals(TipoMensaje.ERROR, respuesta.getTipo());
        assertTrue(respuesta.getPayloadJson().get("error").getAsString().contains("ATTACK"));
    }

    @Test
    void comandoQueFallaRespondeErrorYLaConexionSigue() throws Exception {
        escritor.println("{\"tipo\":\"JOIN_MATCH\",\"payload\":{\"nickname\":\"Martin\",\"codigo\":\"A7K2\"}}");
        assertEquals(TipoMensaje.ERROR, leer().getTipo());

        escritor.println("{\"tipo\":\"CREATE_MATCH\",\"payload\":{\"nickname\":\"Martin\"}}");
        assertEquals(TipoMensaje.MATCH_CREATED, leer().getTipo());
    }

    @Test
    void jsonInvalidoRespondeErrorSinCerrarLaConexion() throws Exception {
        escritor.println("hola servidor");
        assertEquals(TipoMensaje.ERROR, leer().getTipo());

        escritor.println("{\"tipo\":\"NO_EXISTE\"}");
        assertEquals(TipoMensaje.ERROR, leer().getTipo());

        escritor.println("{\"tipo\":\"CREATE_MATCH\",\"payload\":{\"nickname\":\"Steven\"}}");
        assertEquals(TipoMensaje.MATCH_CREATED, leer().getTipo());
    }

    @Test
    void alDesconectarseElClienteSaleDeLaLista() throws Exception {
        escritor.println("{\"tipo\":\"CREATE_MATCH\",\"payload\":{\"nickname\":\"Steven\"}}");
        leer();
        assertEquals(1, servidor.getCantidadClientes());

        socket.close();
        for (int i = 0; i < 50 && servidor.getCantidadClientes() > 0; i++) {
            Thread.sleep(20);
        }
        assertEquals(0, servidor.getCantidadClientes());
    }

    @Test
    void elServidorAtiendeVariosClientesALaVez() throws Exception {
        try (Socket otro = new Socket("localhost", servidor.getPuerto())) {
            otro.setSoTimeout(2000);
            PrintWriter escritorOtro = new PrintWriter(new OutputStreamWriter(otro.getOutputStream(), Constantes.CHARSET), true);
            BufferedReader lectorOtro = new BufferedReader(new InputStreamReader(otro.getInputStream(), Constantes.CHARSET));

            escritorOtro.println("{\"tipo\":\"CREATE_MATCH\",\"payload\":{\"nickname\":\"Martin\"}}");
            escritor.println("{\"tipo\":\"CREATE_MATCH\",\"payload\":{\"nickname\":\"Steven\"}}");

            assertEquals(TipoMensaje.MATCH_CREATED, CodificadorJson.getInstancia().decodificar(lectorOtro.readLine()).getTipo());
            assertEquals(TipoMensaje.MATCH_CREATED, leer().getTipo());
            assertEquals(2, servidor.getCantidadClientes());
        }
    }
}
        