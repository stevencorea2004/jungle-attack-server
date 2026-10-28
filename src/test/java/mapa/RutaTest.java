package mapa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import model.Punto;
import org.junit.jupiter.api.Test;

class RutaTest {
    @Test
    void routesMovementWithinAndBetweenZonesAlongRoads() {
        Mapa mapa = MainMapa.crearMapa();
        Ruta mismaZona = Ruta.crear(mapa, null, new Punto(100, 40), new Punto(300, 180));
        Ruta otrasZonas = Ruta.crear(mapa, null, new Punto(100, 40), new Punto(1_100, 600));

        assertTrue(mismaZona.esValida());
        assertTrue(otrasZonas.esValida());
        assertRouteUsesOnlyRoads(mapa, mismaZona);
        assertRouteUsesOnlyRoads(mapa, otrasZonas);
        List<Punto> puntos = otrasZonas.getPuntos();
        assertEquals(12, mapa.zonaEn(puntos.get(puntos.size() - 1)).getId());
    }

    @Test
    void movesAtConstantSpeedAcrossWaypointsWithoutLeavingTheRoad() {
        Mapa mapa = MainMapa.crearMapa();
        Ruta ruta = Ruta.crear(mapa, null, new Punto(100, 40), new Punto(1_100, 600));
        Punto posicion = ruta.getPuntos().get(0);

        for (int i = 0; i < 10; i++) {
            Punto anterior = posicion;
            posicion = ruta.mover(posicion, 37.0);
            assertTrue(anterior.distancia(posicion) <= 37.000001);
            assertTrue(anterior.distancia(posicion) > 0.0);
            assertTrue(estaSobreCamino(mapa, posicion));
        }
    }

    @Test
    void startingAnimalsArePlacedOnTheRoadNetworkWithinTheirZone() {
        Mapa mapa = MainMapa.crearMapa();
        Random random = new Random(42);

        for (int idZona = 1; idZona <= 12; idZona++) {
            Zona zona = mapa.getZona(idZona);
            for (int i = 0; i < 10; i++) {
                Punto posicion = mapa.puntoAleatorioEnZona(idZona, random);
                assertTrue(zona.contiene(posicion));
                assertTrue(estaSobreCamino(mapa, posicion));
            }
        }
    }

    private static void assertRouteUsesOnlyRoads(Mapa mapa, Ruta ruta) {
        List<Punto> puntos = ruta.getPuntos();
        for (int i = 1; i < puntos.size(); i++) {
            assertTrue(tramoSobreCamino(mapa, puntos.get(i - 1), puntos.get(i)));
        }
    }

    private static boolean estaSobreCamino(Mapa mapa, Punto punto) {
        for (Camino camino : mapa.getCaminos()) {
            for (int i = 1; i < camino.getPuntos().size(); i++) {
                if (tramoSobreSegmento(punto, punto,
                        camino.getPuntos().get(i - 1), camino.getPuntos().get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean tramoSobreCamino(Mapa mapa, Punto inicio, Punto fin) {
        for (Camino camino : mapa.getCaminos()) {
            for (int i = 1; i < camino.getPuntos().size(); i++) {
                if (tramoSobreSegmento(inicio, fin,
                        camino.getPuntos().get(i - 1), camino.getPuntos().get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean tramoSobreSegmento(Punto inicio, Punto fin, Punto a, Punto b) {
        return puntoEnSegmento(inicio, a, b) && puntoEnSegmento(fin, a, b);
    }

    private static boolean puntoEnSegmento(Punto punto, Punto a, Punto b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        double largoCuadrado = dx * dx + dy * dy;
        if (largoCuadrado == 0.0) {
            return punto.distancia(a) < 0.000001;
        }
        double proporcion = ((punto.getX() - a.getX()) * dx + (punto.getY() - a.getY()) * dy)
                / largoCuadrado;
        if (proporcion < -0.000001 || proporcion > 1.000001) {
            return false;
        }
        Punto proyeccion = new Punto(a.getX() + proporcion * dx, a.getY() + proporcion * dy);
        return punto.distancia(proyeccion) < 0.000001;
    }
}
