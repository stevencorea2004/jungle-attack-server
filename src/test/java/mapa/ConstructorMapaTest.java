package mapa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Rectangle2D;
import java.util.Random;
import java.util.Vector;
import model.Punto;
import org.junit.jupiter.api.Test;

class ConstructorMapaTest {
    @Test
    void buildsBidirectionalConnectionsAndSupportsRouteSearch() {
        Mapa mapa = mapaConectado().construir();

        assertEquals(2, mapa.getVecinos(1).get(0).getId());
        assertEquals(1, mapa.getVecinos(2).get(0).getId());
        assertEquals(3, mapa.getVecinos(2).get(1).getId());
        assertEquals(10.0, mapa.getCamino(2, 1).getLargo());
        assertEquals(12, new BuscadorBFS().buscarRuta(mapa, 1, 12).size());
    }

    @Test
    void rejectsInvalidZonesAndConnections() {
        ConstructorMapa constructor = new ConstructorMapa();
        Zona zona = zona(1, 0);
        constructor.agregarZona(zona).agregarZona(zona(2, 10));
        Vector<Object> puntosInvalidos = new Vector<>();
        puntosInvalidos.add("not a coordinate");

        assertThrows(IllegalArgumentException.class, () -> constructor.agregarZona(null));
        assertThrows(IllegalArgumentException.class, () -> constructor.agregarZona(zona(1, 10)));
        assertThrows(IllegalArgumentException.class, () -> constructor.conectar(1, 2, puntos()));
        assertThrows(IllegalArgumentException.class, () -> constructor.conectar(1, 1, puntos(new Punto(1, 1))));
        assertThrows(IllegalArgumentException.class, () -> constructor.conectar(1, 2, puntos(new Punto(Double.NaN, 1))));
        assertThrows(IllegalArgumentException.class, () -> constructor.conectar(1, 2, puntosInvalidos));
        assertThrows(IllegalStateException.class, () -> new ConstructorMapa().construir());
        assertThrows(IllegalStateException.class, () -> mapaConectadoSinUltimaZona().construir());
        assertThrows(IllegalStateException.class, () -> mapaDesconectado().construir());
    }

    @Test
    void copiesPathPointsAndRejectsDuplicateConnections() {
        ConstructorMapa constructor = mapaConectado();
        Vector<Punto> puntos = puntos(new Punto(5, 5), new Punto(115, 5));
        constructor.conectar(1, 12, puntos);
        puntos.get(0).setX(100);
        puntos.clear();

        Mapa mapa = constructor.construir();
        assertNotSame(puntos, mapa.getCamino(1, 12).getPuntos());
        assertEquals(5.0, mapa.getCamino(1, 12).getPuntos().get(0).getX());
        assertThrows(IllegalArgumentException.class,
                () -> constructor.conectar(2, 1, puntos(new Punto(0, 0))));
    }

    @Test
    void generatedMapsVaryAndRoadsReachRegionBorders() {
        Mapa primero = MainMapa.crearMapa(new Random(1));
        Mapa segundo = MainMapa.crearMapa(new Random(2));

        assertEquals(17, primero.getCaminos().size());
        assertNotEquals(primero.getCaminos().get(0).getPuntos(),
                segundo.getCaminos().get(0).getPuntos());
        for (Camino camino : primero.getCaminos()) {
            boolean tocaBorde = false;
            for (Punto punto : camino.getPuntos()) {
                if (punto.getX() % 320.0 == 0.0 || punto.getY() % 240.0 == 0.0) {
                    tocaBorde = true;
                    break;
                }
            }
            assertTrue(tocaBorde, "Cada camino entre regiones debe alcanzar su borde compartido");
        }
    }

    private static ConstructorMapa mapaConectado() {
        ConstructorMapa constructor = new ConstructorMapa();
        for (int id = 1; id <= 12; id++) {
            constructor.agregarZona(zona(id, (id - 1) * 10));
        }
        for (int id = 1; id < 12; id++) {
            double x = id * 10;
            constructor.conectar(id, id + 1, puntos(new Punto(x - 5, 5), new Punto(x + 5, 5)));
        }
        return constructor;
    }

    private static ConstructorMapa mapaConectadoSinUltimaZona() {
        ConstructorMapa constructor = new ConstructorMapa();
        for (int id = 1; id < 12; id++) {
            constructor.agregarZona(zona(id, (id - 1) * 10));
        }
        for (int id = 1; id < 11; id++) {
            double x = id * 10;
            constructor.conectar(id, id + 1, puntos(new Punto(x - 5, 5), new Punto(x + 5, 5)));
        }
        return constructor;
    }

    private static ConstructorMapa mapaDesconectado() {
        ConstructorMapa constructor = new ConstructorMapa();
        for (int id = 1; id <= 12; id++) {
            constructor.agregarZona(zona(id, (id - 1) * 10));
        }
        return constructor;
    }

    private static Zona zona(int id, double x) {
        return new Zona(id, new Rectangle2D.Double(x, 0, 10, 10));
    }

    private static Vector<Punto> puntos(Punto... coordenadas) {
        Vector<Punto> puntos = new Vector<>();
        for (Punto coordenada : coordenadas) {
            puntos.add(coordenada);
        }
        return puntos;
    }
}
