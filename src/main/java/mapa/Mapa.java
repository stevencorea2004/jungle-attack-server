package mapa;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

    public void agregarCaminoLocal(int idZona, List<Punto> puntos) {
        if (getZona(idZona) == null) {
            throw new IllegalArgumentException("La zona debe pertenecer al mapa");
        }
        if (puntos == null || puntos.size() < 2) {
            throw new IllegalArgumentException("El camino local debe contener al menos dos puntos");
        }
        for (Punto punto : puntos) {
            if (punto == null || !Double.isFinite(punto.getX()) || !Double.isFinite(punto.getY())) {
                throw new IllegalArgumentException("Las coordenadas del camino deben ser finitas");
            }
        }
        Punto conexion = puntos.get(0);
        boolean conectado = false;
        for (Camino camino : caminos) {
            Vector<Punto> puntosExistentes = camino.getPuntos();
            for (int i = 1; i < puntosExistentes.size(); i++) {
                Punto inicio = puntosExistentes.get(i - 1);
                Punto fin = puntosExistentes.get(i);
                if (!puntoEnSegmento(conexion, inicio, fin)) {
                    continue;
                }
                if (!conexion.equals(inicio) && !conexion.equals(fin)) {
                    puntosExistentes.add(i, new Punto(conexion.getX(), conexion.getY()));
                }
                conectado = true;
                break;
            }
            if (conectado) {
                break;
            }
        }
        if (!conectado) {
            throw new IllegalArgumentException("El camino local debe iniciar en la red de caminos");
        }
        caminos.add(new Camino(idZona, idZona, puntos));
    }

    private static boolean puntoEnSegmento(Punto punto, Punto inicio, Punto fin) {
        double dx = fin.getX() - inicio.getX();
        double dy = fin.getY() - inicio.getY();
        double largoCuadrado = dx * dx + dy * dy;
        if (largoCuadrado == 0.0) {
            return punto.distancia(inicio) <= 0.000001;
        }
        double proporcion = ((punto.getX() - inicio.getX()) * dx
                + (punto.getY() - inicio.getY()) * dy) / largoCuadrado;
        if (proporcion < 0.0 || proporcion > 1.0) {
            return false;
        }
        Punto proyeccion = new Punto(inicio.getX() + proporcion * dx, inicio.getY() + proporcion * dy);
        return punto.distancia(proyeccion) <= 0.000001;
    }

    public Punto puntoMasCercanoEnZona(int idZona, Punto objetivo) {
        Zona zona = getZona(idZona);
        if (zona == null || objetivo == null) {
            throw new IllegalArgumentException("La zona y el objetivo deben pertenecer al mapa");
        }

        Punto masCercano = null;
        double menorDistancia = Double.POSITIVE_INFINITY;
        for (Camino camino : caminos) {
            Vector<Punto> puntos = camino.getPuntos();
            for (int i = 1; i < puntos.size(); i++) {
                Punto inicio = puntos.get(i - 1);
                Punto fin = puntos.get(i);
                double[] intervalo = Camino.intervaloEnZona(zona, inicio, fin);
                if (intervalo == null) {
                    continue;
                }
                double dx = fin.getX() - inicio.getX();
                double dy = fin.getY() - inicio.getY();
                double largoCuadrado = dx * dx + dy * dy;
                if (largoCuadrado == 0.0) {
                    continue;
                }
                double proporcion = ((objetivo.getX() - inicio.getX()) * dx
                        + (objetivo.getY() - inicio.getY()) * dy) / largoCuadrado;
                proporcion = Math.max(intervalo[0], Math.min(intervalo[1], proporcion));
                Punto proyeccion = new Punto(
                        inicio.getX() + proporcion * dx,
                        inicio.getY() + proporcion * dy);
                double distancia = objetivo.distancia(proyeccion);
                if (distancia < menorDistancia) {
                    menorDistancia = distancia;
                    masCercano = proyeccion;
                }
            }
        }
        if (masCercano == null) {
            throw new IllegalStateException("La zona no contiene caminos transitables");
        }
        return masCercano;
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
            if (camino.getIdZonaA() == idZona && camino.getIdZonaA() != camino.getIdZonaB()) {
                Zona z = getZona(camino.getIdZonaB());
                if (z != null && !vecinos.contains(z)) {
                    vecinos.add(z);
                }
            } else if (camino.getIdZonaB() == idZona && camino.getIdZonaA() != camino.getIdZonaB()) {
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

    public Punto puntoAleatorioEnZona(int idZona, Random random) {
        Zona zona = getZona(idZona);
        if (zona == null) {
            throw new IllegalArgumentException("La zona no pertenece al mapa");
        }

        List<SegmentoZona> segmentos = new ArrayList<>();
        double largoTotal = 0.0;
        for (Camino camino : caminos) {
            Vector<Punto> puntos = camino.getPuntos();
            for (int i = 1; i < puntos.size(); i++) {
                Punto inicio = puntos.get(i - 1);
                Punto fin = puntos.get(i);
                double[] intervalo = Camino.intervaloEnZona(zona, inicio, fin);
                if (intervalo == null || intervalo[1] <= intervalo[0]) {
                    continue;
                }
                double largo = inicio.distancia(fin) * (intervalo[1] - intervalo[0]);
                if (largo > 0.0) {
                    segmentos.add(new SegmentoZona(inicio, fin, intervalo[0], intervalo[1], largo));
                    largoTotal += largo;
                }
            }
        }
        if (segmentos.isEmpty()) {
            throw new IllegalStateException("La zona no contiene caminos transitables");
        }

        double seleccionado = (random == null ? Math.random() : random.nextDouble()) * largoTotal;
        for (SegmentoZona segmento : segmentos) {
            seleccionado -= segmento.largo;
            if (seleccionado <= 0.0) {
                double proporcion = segmento.inicioT
                        + (segmento.finT - segmento.inicioT)
                                * (random == null ? Math.random() : random.nextDouble());
                return interpolar(segmento.inicio, segmento.fin, proporcion);
            }
        }
        SegmentoZona ultimo = segmentos.get(segmentos.size() - 1);
        return interpolar(ultimo.inicio, ultimo.fin, ultimo.finT);
    }

    private static Punto interpolar(Punto inicio, Punto fin, double proporcion) {
        return new Punto(
                inicio.getX() + (fin.getX() - inicio.getX()) * proporcion,
                inicio.getY() + (fin.getY() - inicio.getY()) * proporcion);
    }

    private static final class SegmentoZona {
        private final Punto inicio;
        private final Punto fin;
        private final double inicioT;
        private final double finT;
        private final double largo;

        private SegmentoZona(Punto inicio, Punto fin, double inicioT, double finT, double largo) {
            this.inicio = inicio;
            this.fin = fin;
            this.inicioT = inicioT;
            this.finT = finT;
            this.largo = largo;
        }
    }
}
