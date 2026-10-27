package mapa;

import java.awt.geom.Rectangle2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.Punto;
import util.Constantes;

public class ConstructorMapa {
    private final List<Zona> zonas;
    private final List<Camino> caminos;

    public ConstructorMapa() {
        this.zonas = new ArrayList<>();
        this.caminos = new ArrayList<>();
    }

    public ConstructorMapa agregarZona(Zona zona) {
        if (zona == null) {
            throw new IllegalArgumentException("La zona es obligatoria");
        }
        Rectangle2D limites = zona.getLimites();
        if (limites == null || limites.isEmpty()
                || !Double.isFinite(limites.getMinX())
                || !Double.isFinite(limites.getMinY())
                || !Double.isFinite(limites.getMaxX())
                || !Double.isFinite(limites.getMaxY())) {
            throw new IllegalArgumentException("La zona debe tener límites válidos");
        }
        if (buscarZona(zona.getId()) != null) {
            throw new IllegalArgumentException("Ya existe una zona con el id " + zona.getId());
        }
        zonas.add(zona);
        return this;
    }

    public ConstructorMapa conectar(int zonaA, int zonaB, List<?> puntos) {
        Zona origen = buscarZona(zonaA);
        Zona destino = buscarZona(zonaB);
        if (origen == null || destino == null) {
            throw new IllegalArgumentException("Ambas zonas deben existir antes de conectarlas");
        }
        if (zonaA == zonaB) {
            throw new IllegalArgumentException("Un camino debe conectar dos zonas distintas");
        }
        if (puntos == null || puntos.isEmpty()) {
            throw new IllegalArgumentException("El camino debe contener al menos un punto");
        }
        if (buscarCamino(zonaA, zonaB) != null) {
            throw new IllegalArgumentException("Las zonas ya están conectadas");
        }

        List<Punto> puntosCamino = new ArrayList<>();
        for (Object punto : puntos) {
            if (!(punto instanceof Punto)) {
                throw new IllegalArgumentException("Todos los puntos del camino deben ser coordenadas");
            }
            Punto coordenada = (Punto) punto;
            if (!Double.isFinite(coordenada.getX()) || !Double.isFinite(coordenada.getY())) {
                throw new IllegalArgumentException("Las coordenadas del camino deben ser finitas");
            }
            puntosCamino.add(new Punto(coordenada.getX(), coordenada.getY()));
        }

        caminos.add(new Camino(zonaA, zonaB, puntosCamino));
        origen.agregarVecino(zonaB);
        destino.agregarVecino(zonaA);
        return this;
    }

    public Mapa construir() {
        if (zonas.size() != Constantes.CANTIDAD_ZONAS) {
            throw new IllegalStateException(
                    "El mapa debe contener exactamente " + Constantes.CANTIDAD_ZONAS + " zonas");
        }
        if (!estaConectado()) {
            throw new IllegalStateException("Todas las zonas deben estar conectadas");
        }
        Mapa mapa = new Mapa();
        for (Zona zona : zonas) {
            mapa.agregarZona(zona);
        }
        for (Camino camino : caminos) {
            mapa.agregarCamino(camino);
        }
        return mapa;
    }

    private boolean estaConectado() {
        Set<Integer> visitadas = new HashSet<>();
        ArrayDeque<Zona> pendientes = new ArrayDeque<>();
        pendientes.addLast(zonas.get(0));
        visitadas.add(zonas.get(0).getId());

        while (!pendientes.isEmpty()) {
            Zona actual = pendientes.removeFirst();
            for (int idVecino : actual.getVecinos()) {
                Zona vecino = buscarZona(idVecino);
                if (vecino != null && visitadas.add(idVecino)) {
                    pendientes.addLast(vecino);
                }
            }
        }
        return visitadas.size() == zonas.size();
    }

    private Zona buscarZona(int id) {
        for (Zona zona : zonas) {
            if (zona.getId() == id) {
                return zona;
            }
        }
        return null;
    }

    private Camino buscarCamino(int zonaA, int zonaB) {
        for (Camino camino : caminos) {
            if (camino.conecta(zonaA, zonaB)) {
                return camino;
            }
        }
        return null;
    }
}
