package mapa;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Vector;
import model.Punto;
import util.Constantes;

public final class MainMapa {
    private MainMapa() {
    }

    public static Mapa crearMapa() {
        return crearMapa(new Random());
    }

    public static Mapa crearMapa(Random random) {
        Objects.requireNonNull(random, "El generador aleatorio es obligatorio");
        int columnas = 4;
        int filas = Constantes.CANTIDAD_ZONAS / columnas;
        double anchoZona = 1280.0 / columnas;
        double altoZona = 720.0 / filas;
        ConstructorMapa constructor = new ConstructorMapa();
        List<Punto> centros = new ArrayList<>();

        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                int id = fila * columnas + columna + 1;
                Rectangle2D limites = new Rectangle2D.Double(
                        columna * anchoZona, fila * altoZona, anchoZona, altoZona);
                constructor.agregarZona(new Zona(id, limites));
                centros.add(new Punto(
                        limites.getMinX() + limites.getWidth() * (0.3 + random.nextDouble() * 0.4),
                        limites.getMinY() + limites.getHeight() * (0.3 + random.nextDouble() * 0.4)));
            }
        }

        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                int id = fila * columnas + columna + 1;
                if (columna + 1 < columnas) {
                    conectarHorizontal(constructor, id, id + 1, centros.get(id - 1),
                            centros.get(id), columna, fila, anchoZona, altoZona, random);
                }
                if (fila + 1 < filas) {
                    conectarVertical(constructor, id, id + columnas, centros.get(id - 1),
                            centros.get(id + columnas - 1), columna, fila, anchoZona, altoZona, random);
                }
            }
        }
        return constructor.construir();
    }

    private static void conectarHorizontal(ConstructorMapa constructor, int origen, int destino,
            Punto inicio, Punto fin, int columna, int fila, double anchoZona, double altoZona, Random random) {
        double xBorde = (columna + 1) * anchoZona;
        double yMin = fila * altoZona + altoZona * 0.12;
        double yBorde = yMin + random.nextDouble() * altoZona * 0.76;
        double curvatura = (random.nextDouble() - 0.5) * altoZona * 0.24;
        Vector<Punto> puntos = new Vector<>();
        puntos.add(inicio);
        puntos.add(new Punto(xBorde - anchoZona * 0.22, inicio.getY() + curvatura));
        puntos.add(new Punto(xBorde, yBorde));
        puntos.add(new Punto(xBorde + anchoZona * 0.22, fin.getY() - curvatura));
        puntos.add(fin);
        constructor.conectar(origen, destino, puntos);
    }

    private static void conectarVertical(ConstructorMapa constructor, int origen, int destino,
            Punto inicio, Punto fin, int columna, int fila, double anchoZona, double altoZona, Random random) {
        double yBorde = (fila + 1) * altoZona;
        double xMin = columna * anchoZona + anchoZona * 0.12;
        double xBorde = xMin + random.nextDouble() * anchoZona * 0.76;
        double curvatura = (random.nextDouble() - 0.5) * anchoZona * 0.24;
        Vector<Punto> puntos = new Vector<>();
        puntos.add(inicio);
        puntos.add(new Punto(inicio.getX() + curvatura, yBorde - altoZona * 0.22));
        puntos.add(new Punto(xBorde, yBorde));
        puntos.add(new Punto(fin.getX() - curvatura, yBorde + altoZona * 0.22));
        puntos.add(fin);
        constructor.conectar(origen, destino, puntos);
    }

    public static void main(String[] args) {
        crearMapa();
    }
}
