package mapa;

import java.util.Random;
import java.util.List;
import java.util.Vector;
import java.awt.geom.Rectangle2D;
import model.Punto;

public class Camino {
    private int idZonaA;
    private int idZonaB;
    private Vector<Punto> puntos;
    private double largo;

    public Camino(int idZonaA, int idZonaB, List<Punto> puntos) {
        this.idZonaA = idZonaA;
        this.idZonaB = idZonaB;
        this.puntos = puntos == null ? new Vector<>() : new Vector<>(puntos);
        this.largo = calcularLargo();
    }

    public boolean conecta(int zonaA, int zonaB) {
        return (zonaA == idZonaA && zonaB == idZonaB) || (zonaA == idZonaB && zonaB == idZonaA);
    }

    public Punto puntoAleatorio(Random random) {
        if (puntos == null || puntos.isEmpty()) {
            return new Punto();
        }
        int idx = random == null ? 0 : random.nextInt(puntos.size());
        return puntos.get(idx);
    }

    public double getLargo() {
        return largo;
    }

    public int getIdZonaA() {
        return idZonaA;
    }

    public int getIdZonaB() {
        return idZonaB;
    }

    public Vector<Punto> getPuntos() {
        return puntos;
    }

    static double[] intervaloEnZona(Zona zona, Punto inicio, Punto fin) {
        if (zona == null || zona.getLimites() == null) {
            return new double[] {0.0, 1.0};
        }

        Rectangle2D limites = zona.getLimites();
        double dx = fin.getX() - inicio.getX();
        double dy = fin.getY() - inicio.getY();
        double[] intervalo = {0.0, 1.0};
        if (!recortar(-dx, inicio.getX() - limites.getMinX(), intervalo)
                || !recortar(dx, limites.getMaxX() - inicio.getX(), intervalo)
                || !recortar(-dy, inicio.getY() - limites.getMinY(), intervalo)
                || !recortar(dy, limites.getMaxY() - inicio.getY(), intervalo)) {
            return null;
        }
        return intervalo;
    }

    private static boolean recortar(double p, double q, double[] intervalo) {
        if (p == 0.0) {
            return q >= 0.0;
        }
        double r = q / p;
        if (p < 0.0) {
            if (r > intervalo[1]) {
                return false;
            }
            intervalo[0] = Math.max(intervalo[0], r);
        } else {
            if (r < intervalo[0]) {
                return false;
            }
            intervalo[1] = Math.min(intervalo[1], r);
        }
        return true;
    }

    private double calcularLargo() {
        double total = 0;
        for (int i = 1; i < puntos.size(); i++) {
            total += puntos.get(i - 1).distancia(puntos.get(i));
        }
        return total;
    }
}
