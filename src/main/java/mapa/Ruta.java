package mapa;

import java.util.Vector;
import model.Punto;

public class Ruta {
    private final Vector<Punto> puntos;
    private int indice;

    public Ruta(Vector<Punto> puntos) {
        this.puntos = puntos == null ? new Vector<>() : puntos;
        this.indice = 0;
    }

    public static Ruta crear(Mapa mapa, Vector<Zona> zonas, Punto inicio, Punto destino) {
        Vector<Punto> lista = new Vector<>();
        if (inicio != null) {
            lista.add(new Punto(inicio.getX(), inicio.getY()));
        }
        if (destino != null) {
            lista.add(new Punto(destino.getX(), destino.getY()));
        }
        return new Ruta(lista);
    }

    public Punto puntoActual() {
        if (puntos == null || puntos.isEmpty()) {
            return null;
        }
        return puntos.get(Math.min(indice, puntos.size() - 1));
    }

    public void avanzar() {
        if (puntos.size() > 1 && indice < puntos.size() - 1) {
            indice++;
        }
    }

    public boolean terminada() {
        return puntos == null || puntos.size() <= 1 || indice >= puntos.size() - 1;
    }
}
