package red;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import model.Especie;
import model.Punto;
import org.junit.jupiter.api.Test;

class CodificadorJsonTest {
    private final CodificadorJson codificador = CodificadorJson.getInstancia();

    @Test
    void roundTripsGameStartPayloadWithNestedMapAndAnimals() {
        VistaZona zona = new VistaZona();
        zona.id = 3;
        zona.x = 100.5;
        zona.y = 200.25;
        zona.ancho = 300;
        zona.alto = 150;

        VistaCamino camino = new VistaCamino();
        camino.zonaA = 3;
        camino.zonaB = 4;
        camino.puntos = new Vector<>(List.of(new Punto(110, 220), new Punto(340, 250)));

        VistaMapa mapa = new VistaMapa();
        mapa.zonas = new Vector<>(List.of(zona));
        mapa.caminos = new Vector<>(List.of(camino));

        VistaAnimal animal = new VistaAnimal();
        animal.id = 42;
        animal.especie = Especie.JAGUAR;
        animal.esMio = true;
        animal.x = 125.5;
        animal.y = 240.25;
        animal.energia = 18;

        DatosInicio inicio = new DatosInicio();
        inicio.mapa = mapa;
        inicio.zonaPropia = 3;
        inicio.manada = new Vector<>(List.of(animal));

        Mensaje recibido = roundTrip(TipoMensaje.GAME_STARTED, inicio);
        DatosInicio payload = (DatosInicio) recibido.getPayload(DatosInicio.class);

        assertEquals(TipoMensaje.GAME_STARTED, recibido.getTipo());
        assertEquals(3, payload.zonaPropia);
        assertEquals(3, payload.mapa.zonas.get(0).id);
        assertEquals(150, payload.mapa.zonas.get(0).alto);
        assertEquals(new Punto(340, 250), payload.mapa.caminos.get(0).puntos.get(1));
        assertEquals(42, payload.manada.get(0).id);
        assertEquals(Especie.JAGUAR, payload.manada.get(0).especie);
        assertEquals(18, payload.manada.get(0).energia);
    }

    @Test
    void roundTripsStatePayloadWithCoordinatesCollectionsAndCounters() {
        VistaAnimal animal = new VistaAnimal();
        animal.id = 7;
        animal.especie = Especie.DANTA;
        animal.esBot = true;
        animal.x = 75.5;
        animal.y = 90.25;
        animal.energia = 6;

        EstadoVisible estado = new EstadoVisible();
        estado.idZona = 5;
        estado.zonasVisibles = new Vector<>(List.of(5, 6));
        estado.animales = new Vector<>(List.of(animal));
        estado.tazas = new Vector<>(List.of(new Punto(80, 95)));
        estado.bandera = new Punto(100, 120);
        estado.colorBandera = "azul";
        estado.eventos = new Vector<>(List.of("BOT_ELIMINADO"));
        estado.animalesVivos = 9;
        estado.danio = 3;
        estado.tazasConsumidas = 2;
        estado.botsEliminados = 1;

        Mensaje recibido = roundTrip(TipoMensaje.STATE_UPDATE, estado);
        EstadoVisible payload = (EstadoVisible) recibido.getPayload(EstadoVisible.class);

        assertEquals(TipoMensaje.STATE_UPDATE, recibido.getTipo());
        assertEquals(List.of(5, 6), payload.zonasVisibles);
        assertEquals(new Punto(80, 95), payload.tazas.get(0));
        assertEquals(new Punto(100, 120), payload.bandera);
        assertEquals("BOT_ELIMINADO", payload.eventos.get(0));
        assertEquals(Especie.DANTA, payload.animales.get(0).especie);
        assertEquals(6, payload.animales.get(0).energia);
        assertEquals(9, payload.animalesVivos);
        assertEquals(1, payload.botsEliminados);
    }

    @Test
    void roundTripsGameOverSummaryAndClientCommandPayloads() {
        ResumenPartida resumen = new ResumenPartida();
        resumen.ganador = "Ana";
        resumen.botsEliminados = Map.of("Ana", 4, "Luis", 1);
        resumen.animalesPerdidos = Map.of("Ana", 2, "Luis", 3);
        resumen.tazas = Map.of("Ana", 1, "Luis", 5);
        resumen.duracionSeg = 125;

        Mensaje fin = roundTrip(TipoMensaje.GAME_OVER, resumen);
        ResumenPartida payload = (ResumenPartida) fin.getPayload(ResumenPartida.class);
        assertEquals(TipoMensaje.GAME_OVER, fin.getTipo());
        assertEquals("Ana", payload.ganador);
        assertEquals(4, payload.botsEliminados.get("Ana"));
        assertEquals(3, payload.animalesPerdidos.get("Luis"));
        assertEquals(5, payload.tazas.get("Luis"));
        assertEquals(125, payload.duracionSeg);

        DatosMover mover = new DatosMover();
        mover.idsAnimales = List.of(10, 11);
        mover.destinoX = 350.5;
        mover.destinoY = 480.25;
        DatosMover movimientoRecibido = (DatosMover) roundTrip(
                TipoMensaje.MOVE_ANIMALS, mover).getPayload(DatosMover.class);
        assertEquals(List.of(10, 11), movimientoRecibido.idsAnimales);
        assertEquals(350.5, movimientoRecibido.destinoX);
        assertEquals(480.25, movimientoRecibido.destinoY);

        DatosAtacar atacar = new DatosAtacar();
        atacar.idsAnimales = List.of(10);
        atacar.idObjetivo = 22;
        DatosAtacar ataqueRecibido = (DatosAtacar) roundTrip(
                TipoMensaje.ATTACK, atacar).getPayload(DatosAtacar.class);
        assertEquals(List.of(10), ataqueRecibido.idsAnimales);
        assertEquals(22, ataqueRecibido.idObjetivo);
    }

    private Mensaje roundTrip(TipoMensaje tipo, Object payload) {
        Mensaje mensaje = FabricaMensajes.crear(tipo, payload);
        return codificador.decodificar(codificador.codificar(mensaje));
    }
}
