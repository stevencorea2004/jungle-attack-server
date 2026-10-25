package mapa;

import java.awt.geom.Rectangle2D;
import java.util.Random;
import java.util.Vector;
import model.Punto;

public class Zona {
    private int id;
    private Rectangle2D limites;
    private Vector<Integer> vecinos;

    public boolean contiene(Punto p) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public Punto puntoAleatorio(Random random) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }
}
