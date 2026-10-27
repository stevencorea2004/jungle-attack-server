package red;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.Animal;
import model.AnimalJugador;
import model.Especie;
import model.Punto;
import partida.AdministradorPartidas;
import partida.EstadoPartida;
import partida.Partida;

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
        enrutador.registrar(TipoMensaje.MOVE_ANIMALS, new ComandoMover());
        enrutador.registrar(TipoMensaje.ATTACK, new ComandoAtacar());
        enrutador.registrar(TipoMensaje.VIEW_ZONE, new ComandoVerZona());
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
        Mensaje inicioAnfitrion = anfitrion.leer();
        assertEquals(2, servidor.getCantidadClientes());
        Mensaje inicioInvitado = invitado.leer();
        assertEquals(TipoMensaje.GAME_STARTED, inicioAnfitrion.getTipo());
        assertEquals(TipoMensaje.GAME_STARTED, inicioInvitado.getTipo());
        DatosInicio datosInicio = (DatosInicio) inicioAnfitrion.getPayload(DatosInicio.class);
        assertEquals(10, datosInicio.manada.size());
        assertEquals(12, datosInicio.mapa.zonas.size());
        Mensaje estadoInicial = anfitrion.leer();
        assertEquals(TipoMensaje.STATE_UPDATE, estadoInicial.getTipo());
        EstadoVisible snapshot = (EstadoVisible) estadoInicial.getPayload(EstadoVisible.class);
        assertTrue(snapshot.zonasVisibles.contains(snapshot.idZona));
        assertEquals(TipoMensaje.STATE_UPDATE, invitado.leer().getTipo());

        Partida partida = servidor.getAdministrador().buscar(partidaCreada.codigo);
        assertEquals(EstadoPartida.EN_JUEGO, partida.getEstado());
        assertEquals(2, partida.getMundo().getManadasJugador().size());
        assertEquals(5, partida.getMundo().getManadasBot().size());

        AnimalJugador atacante = (AnimalJugador) partida.getJugadores().get(0)
                .getManada().getVivos().firstElement();
        Punto posicionInicial = new Punto(atacante.getPosicion().getX(), atacante.getPosicion().getY());
        DatosMover mover = new DatosMover();
        mover.idsAnimales = List.of(atacante.getId());
        mover.destinoX = 1_100;
        mover.destinoY = 600;
        anfitrion.enviar(TipoMensaje.MOVE_ANIMALS, mover);
        Thread.sleep(150L);
        assertNotNull(atacante.getRuta());
        assertTrue(posicionInicial.distancia(atacante.getPosicion()) > 0);

        Animal objetivo = partida.getMundo().getManadasBot().get(0).getVivos().firstElement();
        objetivo.setUbicacion(objetivo.getId(), atacante.getPosicion(), atacante.getIdZona());
        DatosAtacar atacar = new DatosAtacar();
        atacar.idsAnimales = List.of(atacante.getId());
        atacar.idObjetivo = objetivo.getId();
        anfitrion.enviar(TipoMensaje.ATTACK, atacar);
        Thread.sleep(100L);
        assertSame(objetivo, atacante.getObjetivo());
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

    @Test
    void partidaCompletaPorTcpTerminaAlTocarLaBanderaYRindeResumen() throws Exception {
        ClientePrueba anfitrion = conectar(45_000);
        anfitrion.enviar(TipoMensaje.CREATE_MATCH, crear("Ana"));
        DatosPartidaCreada creada = (DatosPartidaCreada) anfitrion.leer()
                .getPayload(DatosPartidaCreada.class);

        ClientePrueba invitado = conectar(45_000);
        DatosUnirse unirse = new DatosUnirse();
        unirse.nickname = "Luis";
        unirse.codigo = creada.codigo;
        invitado.enviar(TipoMensaje.JOIN_MATCH, unirse);
        assertEquals(TipoMensaje.MATCH_JOINED, anfitrion.leer().getTipo());
        assertEquals(TipoMensaje.MATCH_JOINED, invitado.leer().getTipo());
        seleccionar(anfitrion.leer());
        seleccionar(invitado.leer());

        anfitrion.enviar(TipoMensaje.SELECT_ANIMAL, elegir(Especie.JAGUAR));
        seleccionar(anfitrion.leer());
        seleccionar(invitado.leer());
        invitado.enviar(TipoMensaje.SELECT_ANIMAL, elegir(Especie.DANTA));
        seleccionar(anfitrion.leer());
        seleccionar(invitado.leer());
        assertEquals(TipoMensaje.GAME_STARTED, anfitrion.leer().getTipo());
        assertEquals(TipoMensaje.GAME_STARTED, invitado.leer().getTipo());
        assertEquals(TipoMensaje.STATE_UPDATE, anfitrion.leer().getTipo());
        assertEquals(TipoMensaje.STATE_UPDATE, invitado.leer().getTipo());

        Partida partida = servidor.getAdministrador().buscar(creada.codigo);
        assertNotNull(partida);
        assertEquals(EstadoPartida.EN_JUEGO, partida.getEstado());
        var mundo = partida.getMundo();
        var jugadorGanador = partida.getJugadores().get(0);
        var jugadorDefensor = partida.getJugadores().get(1);
        var banderaDefensora = jugadorDefensor.getBandera();
        Punto destino = mundo.getMapa().puntoAleatorioEnZona(
                banderaDefensora.getIdZona(), new Random(73));
        banderaDefensora.setUbicacion(
                banderaDefensora.getId(), destino, banderaDefensora.getIdZona());
        for (var bots : mundo.getManadasBot()) {
            for (Animal bot : bots.getMiembros()) {
                bot.setUbicacion(bot.getId(), new Punto(2_000, 2_000), 0);
            }
        }

        DatosMover mover = new DatosMover();
        mover.idsAnimales = jugadorGanador.getManada().getVivos().stream()
                .map(Animal::getId)
                .toList();
        mover.destinoX = destino.getX();
        mover.destinoY = destino.getY();

        ExecutorService lectores = Executors.newFixedThreadPool(2);
        try {
            Future<Mensaje> finAnfitrion = lectores.submit(
                    () -> anfitrion.leerHasta(TipoMensaje.GAME_OVER));
            Future<Mensaje> finInvitado = lectores.submit(
                    () -> invitado.leerHasta(TipoMensaje.GAME_OVER));
            anfitrion.enviar(TipoMensaje.MOVE_ANIMALS, mover);

            ResumenPartida resumenAnfitrion = (ResumenPartida) finAnfitrion
                    .get(50, TimeUnit.SECONDS).getPayload(ResumenPartida.class);
            ResumenPartida resumenInvitado = (ResumenPartida) finInvitado
                    .get(50, TimeUnit.SECONDS).getPayload(ResumenPartida.class);

            assertEquals("Ana", resumenAnfitrion.ganador);
            assertEquals(resumenAnfitrion.ganador, resumenInvitado.ganador);
            assertEquals(0, resumenAnfitrion.animalesPerdidos.get("Ana"));
            assertTrue(resumenAnfitrion.animalesPerdidos.containsKey("Luis"));
            assertTrue(resumenAnfitrion.botsEliminados.containsKey("Ana"));
            assertTrue(resumenAnfitrion.tazas.containsKey("Luis"));
            assertEquals(EstadoPartida.TERMINADA, partida.getEstado());
            assertEquals(0, servidor.getAdministrador().getCantidadActivas());
        } finally {
            lectores.shutdownNow();
        }
    }

    private ClientePrueba conectar() throws IOException {
        return conectar(2_000);
    }

    private ClientePrueba conectar(int timeoutMs) throws IOException {
        ClientePrueba cliente = new ClientePrueba(
                new Socket("localhost", servidor.getPuerto()), timeoutMs);
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

        private ClientePrueba(Socket socket, int timeoutMs) throws IOException {
            this.socket = socket;
            socket.setSoTimeout(timeoutMs);
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

        private Mensaje leerHasta(TipoMensaje tipoEsperado) throws IOException {
            Mensaje mensaje;
            do {
                mensaje = leer();
            } while (mensaje.getTipo() != tipoEsperado);
            return mensaje;
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }
    }
}
