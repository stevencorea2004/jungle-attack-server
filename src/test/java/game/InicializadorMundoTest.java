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
import mapa.Mapa;
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
