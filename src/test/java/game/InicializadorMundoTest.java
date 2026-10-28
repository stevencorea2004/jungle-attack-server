package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import mapa.ConstructorMapa;
import mapa.MainMapa;
import mapa.Mapa;
import mapa.Ruta;
import mapa.Zona;
import model.Animal;
import model.Especie;
import model.ManadaJugador;
import model.Punto;
import org.junit.jupiter.api.Test;
import util.Constantes;

class InicializadorMundoTest {
    @Test
    void selectsTheRequestedNumberOfDistinctZonesAndCanBeSeeded() {
        Mapa mapa = crearMapa();
        InicializadorMundo primero = new InicializadorMundo(new Random(42));
        InicializadorMundo segundo = new InicializadorMundo(new Random(42));

        Vector<Integer> seleccion = primero.elegirZonasIniciales(mapa, 7);

        assertEquals(7, seleccion.size());
        assertEquals(7, new HashSet<>(seleccion).size());
        assertEquals(seleccion, segundo.elegirZonasIniciales(mapa, 7));
        assertThrows(IllegalArgumentException.class, () -> primero.elegirZonasIniciales(mapa, 13));
    }

    @Test
    void placesConfiguredNumberOfCupsOnPathCoordinates() {
        MundoJuego mundo = mundoConMapa();
        InicializadorMundo inicializador = new InicializadorMundo(new Random(12));

        inicializador.colocarTazas(mundo);

        assertEquals(Constantes.CANTIDAD_TAZAS, mundo.getTazas().size());
        Set<Punto> puntosDeCamino = new HashSet<>();
        mundo.getMapa().getCaminos().forEach(camino -> puntosDeCamino.addAll(camino.getPuntos()));
        for (var taza : mundo.getTazas()) {
            assertTrue(puntosDeCamino.contains(taza.getPos()));
        }
        assertThrows(IllegalStateException.class, () -> inicializador.colocarTazas(mundo));
    }

    @Test
    void placesOneFlagInEachDistinctPlayerStartingZone() {
        MundoJuego mundo = mundoConMapa();
        Jugador jugadorAzul = agregarManada(mundo, Especie.JAGUAR, 1);
        Jugador jugadorRojo = agregarManada(mundo, Especie.DANTA, 2);

        new InicializadorMundo(new Random(5)).colocarBanderas(mundo);

        assertEquals(2, mundo.getBanderas().size());
        assertNotNull(jugadorAzul.getBandera());
        assertNotNull(jugadorRojo.getBandera());
        assertEquals(jugadorAzul, jugadorAzul.getBandera().getDueno());
        assertEquals(jugadorRojo, jugadorRojo.getBandera().getDueno());
        assertEquals(1, jugadorAzul.getBandera().getIdZona());
        assertEquals(2, jugadorRojo.getBandera().getIdZona());
        assertTrue(mundo.getMapa().getZona(1).contiene(jugadorAzul.getBandera().getPos()));
        assertTrue(mundo.getMapa().getZona(2).contiene(jugadorRojo.getBandera().getPos()));
        EstadoVisible estadoAzul = new ServicioVisibilidad().crearSnapshot(mundo, jugadorAzul);
        EstadoVisible estadoRojo = new ServicioVisibilidad().crearSnapshot(mundo, jugadorRojo);
        assertEquals(jugadorAzul.getBandera().getPos(), estadoAzul.bandera);
        assertEquals("azul", estadoAzul.colorBandera);
        assertEquals(jugadorRojo.getBandera().getPos(), estadoRojo.bandera);
        assertEquals("rojo", estadoRojo.colorBandera);
        assertEquals(13, mundo.getMapa().getCaminos().size());
        assertTrue(mundo.getMapa().getVecinos(1).stream().noneMatch(vecino -> vecino.getId() == 1));
        assertTrue(mundo.getMapa().getVecinos(2).stream().noneMatch(vecino -> vecino.getId() == 2));
        for (var bandera : mundo.getBanderas()) {
            var caminoBandera = mundo.getMapa().getCaminos().stream()
                    .filter(camino -> camino.getIdZonaA() == bandera.getIdZona()
                            && camino.getIdZonaB() == bandera.getIdZona())
                    .findFirst()
                    .orElseThrow();
            assertEquals(bandera.getPos(), caminoBandera.getPuntos().lastElement());
            var manada = mundo.getManadasJugador().stream()
                    .filter(actual -> actual.getDueno() == bandera.getDueno())
                    .findFirst()
                    .orElseThrow();
            Ruta ruta = Ruta.crear(mundo.getMapa(), null,
                    manada.getVivos().firstElement().getPosicion(), bandera.getPos());
            assertTrue(ruta.esValida());
            assertTrue(ruta.getPuntos().get(ruta.getPuntos().size() - 1)
                    .distancia(bandera.getPos()) < 0.000001);
        }
    }

    @Test
    void flagPathsDoNotInterruptMovementBetweenZones() {
        Mapa mapa = MainMapa.crearMapa(new Random(25));
        MundoJuego mundo = new MundoJuego();
        mundo.setMapa(mapa);
        agregarManada(mundo, Especie.JAGUAR, 1);
        agregarManada(mundo, Especie.DANTA, 2);
        new InicializadorMundo(new Random(26)).colocarBanderas(mundo);

        Punto inicio = mapa.puntoAleatorioEnZona(1, new Random(27));
        Punto destino = mapa.puntoAleatorioEnZona(12, new Random(28));
        Animal animal = mundo.getManadasJugador().get(0).getVivos().firstElement();
        animal.setUbicacion(animal.getId(), inicio, 1);
        Ruta ruta = Ruta.crear(mapa, null, inicio, destino);
        assertTrue(ruta.esValida());
        animal.setRuta(ruta);
        Set<Integer> zonasVisitadas = new HashSet<>();
        ReglaMovimiento reglaMovimiento = new ReglaMovimiento();
        for (int tick = 0; tick < 10_000 && !ruta.terminada(); tick++) {
            reglaMovimiento.aplicar(mundo, tick * 200L, 0.2);
            zonasVisitadas.add(animal.getIdZona());
        }

        assertTrue(ruta.terminada());
        assertEquals(12, animal.getIdZona());
        assertTrue(zonasVisitadas.size() > 1);
    }

    private static MundoJuego mundoConMapa() {
        MundoJuego mundo = new MundoJuego();
        mundo.setMapa(crearMapa());
        return mundo;
    }

    private static Mapa crearMapa() {
        ConstructorMapa constructor = new ConstructorMapa();
        for (int id = 1; id <= Constantes.CANTIDAD_ZONAS; id++) {
            constructor.agregarZona(zona(id));
        }
        for (int id = 1; id < Constantes.CANTIDAD_ZONAS; id++) {
            Vector<Punto> puntos = new Vector<>();
            puntos.add(new Punto(id * 100 + 50, 50));
            puntos.add(new Punto(id * 100 + 60, 50));
            constructor.conectar(id, id + 1, puntos);
        }
        return constructor.construir();
    }

    private static Zona zona(int id) {
        return new Zona(id, new Rectangle2D.Double(id * 100, 0, 100, 100));
    }

    private static Jugador agregarManada(MundoJuego mundo, Especie especie, int idZona) {
        Jugador jugador = new Jugador();
        ManadaJugador manada = new ManadaJugador(especie, jugador);
        manada.crearMiembros(mundo.getMapa().getZona(idZona), new Random(idZona));
        jugador.setManada(manada);
        mundo.getManadasJugador().add(manada);
        return jugador;
    }
}
