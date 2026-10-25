package mapa;

import java.util.Vector;
import model.Punto;

public class Ruta {
    private Vector<Punto> puntos;
    private int indice;

    public static Ruta crear(Mapa mapa, Vector<Zona> zonas, Punto inicio, Punto destino) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public Punto puntoActual() {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public void avanzar() {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public boolean terminada() {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }
}
