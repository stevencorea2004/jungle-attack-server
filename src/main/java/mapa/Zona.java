package mapa;

import java.awt.geom.Rectangle2D;
import java.util.Random;
import java.util.Vector;
import model.Punto;

public class Zona {
    private int id;
    private Rectangle2D limites;
    private Vector<Integer> vecinos;

    public Zona(int id, Rectangle2D limites) {
        this.id = id;
        this.limites = limites;
        this.vecinos = new Vector<>();
    }

    public int getId() {
        return id;
    }

    public Rectangle2D getLimites() {
        return limites;
    }

    public Vector<Integer> getVecinos() {
        return vecinos;
    }

    public void agregarVecino(int idVecino) {
        if (!vecinos.contains(idVecino)) {
            vecinos.add(idVecino);
        }
    }

    public boolean contiene(Punto p) {
        return p != null && limites != null && limites.contains(p.getX(), p.getY());
    }

    public Punto puntoAleatorio(Random random) {
        if (limites == null) {
            return new Punto();
        }
        double x = limites.getMinX() + (limites.getWidth() * (random == null ? Math.random() : random.nextDouble()));
        double y = limites.getMinY() + (limites.getHeight() * (random == null ? Math.random() : random.nextDouble()));
        return new Punto(x, y);
    }
}
