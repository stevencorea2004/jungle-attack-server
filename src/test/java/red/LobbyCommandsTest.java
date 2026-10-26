package red;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import model.Especie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import partida.AdministradorPartidas;

class LobbyCommandsTest {
    private ServidorJuego servidor;
    private final List<ClientePrueba> clientes = new ArrayList<>();

    @BeforeEach
    void iniciarServidor() throws Exception {
        AdministradorPartidas administrador = new AdministradorPartidas();
        EnrutadorMensajes enrutador = new EnrutadorMensajes();
        enrutador.registrar(TipoMensaje.CREATE_MATCH, new ComandoCrearPartida(administrador));
        enrutador.registrar(TipoMensaje.JOIN_MATCH, new ComandoUnirse(administrador));
        enrutador.registrar(TipoMensaje.SELECT_ANIMAL, new ComandoElegirAnimal());
        servidor = new ServidorJuego(0, administrador, enrutador);
        servidor.iniciar();
    }

    @AfterEach
    void detenerServidor() throws Exception {
        for (ClientePrueba cliente : clientes) {
            cliente.close();
        }
        servidor.detener();
    }

    @Test
    void creaUneYPermiteElegirEspeciesPorTcp() throws Exception {
        ClientePrueba anfitrion = conectar();
        anfitrion.enviar(TipoMensaje.CREATE_MATCH, crear("Ana"));

        Mensaje creada = anfitrion.leer();
        assertEquals(TipoMensaje.MATCH_CREATED, creada.getTipo());
        DatosPartidaCreada partidaCreada =
                (DatosPartidaCreada) creada.getPayload(DatosPartidaCreada.class);
        assertNotNull(partidaCreada.codigo);
        assertTrue(partidaCreada.codigo.matches("[A-Z0-9]{4}"));

        ClientePrueba invitado = conectar();
        DatosUnirse unirse = new DatosUnirse();
        unirse.nickname = "Luis";
        unirse.codigo = partidaCreada.codigo.toLowerCase();
        invitado.enviar(TipoMensaje.JOIN_MATCH, unirse);

        assertEquals(TipoMensaje.MATCH_JOINED, anfitrion.leer().getTipo());
        assertEquals(TipoMensaje.MATCH_JOINED, invitado.leer().getTipo());
        DatosSeleccion disponibilidadAnfitrion = seleccionar(anfitrion.leer());
        DatosSeleccion disponibilidadInvitado = seleccionar(invitado.leer());
        assertEquals(7, disponibilidadAnfitrion.disponibles.size());
        assertEquals(7, disponibilidadInvitado.disponibles.size());

        anfitrion.enviar(TipoMensaje.SELECT_ANIMAL, elegir(Especie.JAGUAR));
        DatosSeleccion seleccionAnfitrion = seleccionar(anfitrion.leer());
        DatosSeleccion seleccionInvitado = seleccionar(invitado.leer());
        assertEquals(6, seleccionAnfitrion.disponibles.size());
        assertEquals(Especie.JAGUAR, seleccionInvitado.eleccionRival);

        invitado.enviar(TipoMensaje.SELECT_ANIMAL, elegir(Especie.DANTA));
        seleccionAnfitrion = seleccionar(anfitrion.leer());
        seleccionInvitado = seleccionar(invitado.leer());
        assertEquals(5, seleccionAnfitrion.disponibles.size());
        assertEquals(Especie.DANTA, seleccionAnfitrion.eleccionRival);
        assertEquals(Especie.JAGUAR, seleccionInvitado.eleccionRival);
        assertFalse(seleccionInvitado.disponibles.contains(Especie.JAGUAR));
        assertFalse(seleccionInvitado.disponibles.contains(Especie.DANTA));
    }

    @Test
    void codigoInvalidoYEspecieYaElegidaDevuelvenError() throws Exception {
        ClientePrueba anfitrion = conectar();
        anfitrion.enviar(TipoMensaje.CREATE_MATCH, crear("Ana"));
        DatosPartidaCreada creada = (DatosPartidaCreada) anfitrion.leer()
                .getPayload(DatosPartidaCreada.class);

        ClientePrueba invalido = conectar();
        DatosUnirse solicitudInvalida = new DatosUnirse();
        solicitudInvalida.nickname = "Luis";
        solicitudInvalida.codigo = "ZZZZ";
        invalido.enviar(TipoMensaje.JOIN_MATCH, solicitudInvalida);
        assertEquals(TipoMensaje.ERROR, invalido.leer().getTipo());

        ClientePrueba invitado = conectar();
        DatosUnirse solicitud = new DatosUnirse();
        solicitud.nickname = "Luis";
        solicitud.codigo = creada.codigo;
        invitado.enviar(TipoMensaje.JOIN_MATCH, solicitud);
        anfitrion.leer();
        invitado.leer();
        anfitrion.leer();
        invitado.leer();

        anfitrion.enviar(TipoMensaje.SELECT_ANIMAL, elegir(Especie.JAGUAR));
        anfitrion.leer();
        invitado.leer();
        invitado.enviar(TipoMensaje.SELECT_ANIMAL, elegir(Especie.JAGUAR));
        Mensaje error = invitado.leer();
        assertEquals(TipoMensaje.ERROR, error.getTipo());
    }

    private ClientePrueba conectar() throws IOException {
        ClientePrueba cliente = new ClientePrueba(new Socket("localhost", servidor.getPuerto()));
        clientes.add(cliente);
        return cliente;
    }

    private static DatosElegirAnimal elegir(Especie especie) {
        DatosElegirAnimal datos = new DatosElegirAnimal();
        datos.especie = especie;
        return datos;
    }

    private static DatosCrearPartida crear(String nickname) {
        DatosCrearPartida datos = new DatosCrearPartida();
        datos.nickname = nickname;
        return datos;
    }

    private static DatosSeleccion seleccionar(Mensaje mensaje) {
        assertEquals(TipoMensaje.ANIMAL_SELECTION, mensaje.getTipo());
        return (DatosSeleccion) mensaje.getPayload(DatosSeleccion.class);
    }

    private static final class ClientePrueba implements AutoCloseable {
        private final Socket socket;
        private final BufferedReader lector;
        private final PrintWriter escritor;

        private ClientePrueba(Socket socket) throws IOException {
            this.socket = socket;
            socket.setSoTimeout(2000);
            lector = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            escritor = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        }

        private void enviar(TipoMensaje tipo, Object datos) {
            escritor.println(CodificadorJson.getInstancia().codificar(FabricaMensajes.crear(tipo, datos)));
        }

        private Mensaje leer() throws IOException {
            String linea = lector.readLine();
            assertNotNull(linea, "El servidor cerró la conexión sin respuesta");
            return CodificadorJson.getInstancia().decodificar(linea);
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }
    }
}
