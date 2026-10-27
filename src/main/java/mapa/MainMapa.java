package mapa;

import java.awt.geom.Rectangle2D;
import java.util.Vector;
import model.Punto;
import util.Constantes;

public final class MainMapa {
    private MainMapa() {
    }

    public static Mapa crearMapa() {
        int columnas = 4;
        int filas = Constantes.CANTIDAD_ZONAS / columnas;
        double anchoZona = 1280.0 / columnas;
        double altoZona = 720.0 / filas;
        ConstructorMapa constructor = new ConstructorMapa();

        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                int id = fila * columnas + columna + 1;
                constructor.agregarZona(new Zona(id, new Rectangle2D.Double(
                        columna * anchoZona, fila * altoZona, anchoZona, altoZona)));
            }
        }

        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                int id = fila * columnas + columna + 1;
                if (columna + 1 < columnas) {
                    conectarHorizontal(constructor, id, id + 1, columna, fila, anchoZona, altoZona);
                }
                if (fila + 1 < filas) {
                    conectarVertical(constructor, id, id + columnas, columna, fila, anchoZona, altoZona);
                }
            }
        }
        return constructor.construir();
    }

    private static void conectarHorizontal(ConstructorMapa constructor, int origen, int destino,
            int columna, int fila, double anchoZona, double altoZona) {
        double y = (fila + 0.5) * altoZona;
        double x = (columna + 0.5) * anchoZona;
        Vector<Punto> puntos = new Vector<>();
        puntos.add(new Punto(x, y));
        puntos.add(new Punto((columna + 1) * anchoZona, y));
        puntos.add(new Punto((columna + 1.5) * anchoZona, y));
        constructor.conectar(origen, destino, puntos);
    }

    private static void conectarVertical(ConstructorMapa constructor, int origen, int destino,
            int columna, int fila, double anchoZona, double altoZona) {
        double x = (columna + 0.5) * anchoZona;
        double y = (fila + 0.5) * altoZona;
        Vector<Punto> puntos = new Vector<>();
        puntos.add(new Punto(x, y));
        puntos.add(new Punto(x, (fila + 1) * altoZona));
        puntos.add(new Punto(x, (fila + 1.5) * altoZona));
        constructor.conectar(origen, destino, puntos);
    }

    public static void main(String[] args) {
        crearMapa();
    }
}
