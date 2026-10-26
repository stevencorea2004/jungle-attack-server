package mapa;

import java.util.Vector;
import model.Punto;

public class Mapa {
    private Vector<Zona> zonas;
    private Vector<Camino> caminos;

    public Mapa() {
        this.zonas = new Vector<>();
        this.caminos = new Vector<>();
    }

    public void agregarZona(Zona zona) {
        if (zona != null && !zonas.contains(zona)) {
            zonas.add(zona);
        }
    }

    public void agregarCamino(Camino camino) {
        if (camino != null && !caminos.contains(camino)) {
            caminos.add(camino);
        }
    }

    public Zona getZona(int id) {
        for (Zona zona : zonas) {
            if (zona != null && zona.getId() == id) {
                return zona;
            }
        }
        return null;
    }

    public Vector<Zona> getZonas() {
        return new Vector<>(zonas);
    }

    public Vector<Camino> getCaminos() {
        return new Vector<>(caminos);
    }

    public Zona zonaEn(Punto p) {
        for (Zona zona : zonas) {
            if (zona != null && zona.contiene(p)) {
                return zona;
            }
        }
        return null;
    }

    public Vector<Zona> getVecinos(int idZona) {
        Vector<Zona> vecinos = new Vector<>();
        for (Camino camino : caminos) {
            if (camino == null) {
                continue;
            }
            if (camino.getIdZonaA() == idZona) {
                Zona z = getZona(camino.getIdZonaB());
                if (z != null && !vecinos.contains(z)) {
                    vecinos.add(z);
                }
            } else if (camino.getIdZonaB() == idZona) {
                Zona z = getZona(camino.getIdZonaA());
                if (z != null && !vecinos.contains(z)) {
                    vecinos.add(z);
                }
            }
        }
        return vecinos;
    }

    public Camino getCamino(int zonaA, int zonaB) {
        for (Camino camino : caminos) {
            if (camino != null && camino.conecta(zonaA, zonaB)) {
                return camino;
            }
        }
        return null;
    }
}
