package mapa;

import java.util.Random;
import java.util.Vector;
import model.Punto;

public class Camino {
    private int idZonaA;
    private int idZonaB;
    private Vector<Punto> puntos;
    private double largo;

    public Camino(int idZonaA, int idZonaB, Vector<Punto> puntos) {
        this.idZonaA = idZonaA;
        this.idZonaB = idZonaB;
        this.puntos = puntos == null ? new Vector<>() : puntos;
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

    private double calcularLargo() {
        double total = 0;
        for (int i = 1; i < puntos.size(); i++) {
            total += puntos.get(i - 1).distancia(puntos.get(i));
        }
        return total;
    }
}
