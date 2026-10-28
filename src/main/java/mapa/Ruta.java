package mapa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Punto;

public class Ruta {
    private static final double TOLERANCIA = 0.000001;
    private final List<Punto> puntos;
    private int indice;

    public Ruta(List<Punto> puntos) {
        this.puntos = puntos == null ? new ArrayList<>() : new ArrayList<>(puntos);
        this.indice = 0;
    }

    public static Ruta crear(Mapa mapa, List<Zona> zonas, Punto inicio, Punto destino) {
        return crear(mapa, zonas, inicio, destino, null);
    }

    public static Ruta crearEnZona(Mapa mapa, int idZona, Punto inicio, Punto destino) {
        if (mapa == null || mapa.getZona(idZona) == null) {
            return new Ruta(new ArrayList<>());
        }
        return crear(mapa, null, inicio, destino, mapa.getZona(idZona));
    }

    private static Ruta crear(Mapa mapa, List<Zona> zonas, Punto inicio, Punto destino,
            Zona zonaPermitida) {
        if (mapa == null || inicio == null || destino == null) {
            return new Ruta(new ArrayList<>());
        }

        RedVial red = construirRed(mapa, zonaPermitida);
        if (red.segmentos.isEmpty()) {
            return new Ruta(new ArrayList<>());
        }

        Zona zonaInicio = zonaPermitida == null ? mapa.zonaEn(inicio) : zonaPermitida;
        Zona zonaDestino = zonaPermitida == null ? mapa.zonaEn(destino) : zonaPermitida;
        Proyeccion origen = proyectar(red.segmentos, zonaInicio, inicio);
        Proyeccion fin = proyectar(red.segmentos, zonaDestino, destino);
        if (origen == null || fin == null) {
            return new Ruta(new ArrayList<>());
        }

        int idOrigen = agregarNodo(red, origen.punto);
        int idDestino = agregarNodo(red, fin.punto);
        conectar(red, idOrigen, origen.segmento.idInicio,
                origen.punto.distancia(origen.segmento.inicio));
        conectar(red, idOrigen, origen.segmento.idFin,
                origen.punto.distancia(origen.segmento.fin));
        conectar(red, idDestino, fin.segmento.idInicio,
                fin.punto.distancia(fin.segmento.inicio));
        conectar(red, idDestino, fin.segmento.idFin,
                fin.punto.distancia(fin.segmento.fin));
        if (origen.segmento == fin.segmento) {
            conectar(red, idOrigen, idDestino, origen.punto.distancia(fin.punto));
        }

        int[] anteriores = new int[red.nodos.size()];
        Arrays.fill(anteriores, -1);
        double[] distancias = new double[red.nodos.size()];
        Arrays.fill(distancias, Double.POSITIVE_INFINITY);
        boolean[] visitados = new boolean[red.nodos.size()];
        distancias[idOrigen] = 0.0;

        for (int i = 0; i < red.nodos.size(); i++) {
            int actual = -1;
            for (int nodo = 0; nodo < distancias.length; nodo++) {
                if (!visitados[nodo] && (actual == -1 || distancias[nodo] < distancias[actual])) {
                    actual = nodo;
                }
            }
            if (actual == -1 || !Double.isFinite(distancias[actual])) {
                break;
            }
            if (actual == idDestino) {
                break;
            }
            visitados[actual] = true;
            for (Arista arista : red.aristas.get(actual)) {
                double nuevaDistancia = distancias[actual] + arista.largo;
                if (nuevaDistancia < distancias[arista.destino]) {
                    distancias[arista.destino] = nuevaDistancia;
                    anteriores[arista.destino] = actual;
                }
            }
        }
        if (!Double.isFinite(distancias[idDestino])) {
            return new Ruta(new ArrayList<>());
        }

        List<Punto> puntosRuta = new ArrayList<>();
        for (int paso = idDestino; paso != -1; paso = anteriores[paso]) {
            agregarPunto(puntosRuta, red.nodos.get(paso));
            if (paso == idOrigen) {
                break;
            }
        }
        if (puntosRuta.isEmpty() || !puntosRuta.get(puntosRuta.size() - 1).equals(origen.punto)) {
            return new Ruta(new ArrayList<>());
        }
        java.util.Collections.reverse(puntosRuta);
        return new Ruta(puntosRuta);
    }

    private static RedVial construirRed(Mapa mapa, Zona zonaPermitida) {
        RedVial red = new RedVial();
        for (Camino camino : mapa.getCaminos()) {
            List<Punto> puntos = camino.getPuntos();
            for (int i = 1; i < puntos.size(); i++) {
                Punto inicio = puntos.get(i - 1);
                Punto fin = puntos.get(i);
                if (zonaPermitida != null) {
                    double[] intervalo = Camino.intervaloEnZona(zonaPermitida, inicio, fin);
                    if (intervalo == null || intervalo[1] - intervalo[0] <= TOLERANCIA) {
                        continue;
                    }
                    if (intervalo[0] > 0.0 || intervalo[1] < 1.0) {
                        double dx = fin.getX() - inicio.getX();
                        double dy = fin.getY() - inicio.getY();
                        fin = new Punto(inicio.getX() + intervalo[1] * dx,
                                inicio.getY() + intervalo[1] * dy);
                        inicio = new Punto(inicio.getX() + intervalo[0] * dx,
                                inicio.getY() + intervalo[0] * dy);
                    }
                }
                if (inicio.distancia(fin) <= TOLERANCIA) {
                    continue;
                }
                int idInicio = agregarNodo(red, inicio);
                int idFin = agregarNodo(red, fin);
                Segmento segmento = new Segmento(inicio, fin, idInicio, idFin);
                red.segmentos.add(segmento);
                conectar(red, idInicio, idFin, inicio.distancia(fin));
            }
        }
        return red;
    }

    private static Proyeccion proyectar(List<Segmento> segmentos, Zona zona, Punto punto) {
        Proyeccion mejor = null;
        double menorDistancia = Double.POSITIVE_INFINITY;
        for (Segmento segmento : segmentos) {
            double[] intervalo = Camino.intervaloEnZona(zona, segmento.inicio, segmento.fin);
            if (intervalo == null) {
                continue;
            }
            double dx = segmento.fin.getX() - segmento.inicio.getX();
            double dy = segmento.fin.getY() - segmento.inicio.getY();
            double largoCuadrado = dx * dx + dy * dy;
            double t = ((punto.getX() - segmento.inicio.getX()) * dx
                    + (punto.getY() - segmento.inicio.getY()) * dy) / largoCuadrado;
            t = Math.max(intervalo[0], Math.min(intervalo[1], t));
            Punto proyeccion = new Punto(
                    segmento.inicio.getX() + t * dx,
                    segmento.inicio.getY() + t * dy);
            double distancia = punto.distancia(proyeccion);
            if (distancia < menorDistancia) {
                menorDistancia = distancia;
                mejor = new Proyeccion(segmento, proyeccion);
            }
        }
        return mejor;
    }

    private static int agregarNodo(RedVial red, Punto punto) {
        Punto clave = new Punto(punto.getX(), punto.getY());
        Integer existente = red.indices.get(clave);
        if (existente != null) {
            return existente;
        }
        int id = red.nodos.size();
        red.indices.put(clave, id);
        red.nodos.add(clave);
        red.aristas.add(new ArrayList<>());
        return id;
    }

    private static void conectar(RedVial red, int origen, int destino, double largo) {
        red.aristas.get(origen).add(new Arista(destino, largo));
        red.aristas.get(destino).add(new Arista(origen, largo));
    }

    private static void agregarPunto(List<Punto> destino, Punto punto) {
        if (destino.isEmpty() || !destino.get(destino.size() - 1).equals(punto)) {
            destino.add(new Punto(punto.getX(), punto.getY()));
        }
    }

    public Punto puntoActual() {
        if (puntos.isEmpty() || indice >= puntos.size()) {
            return null;
        }
        return puntos.get(indice);
    }

    public List<Punto> getPuntos() {
        List<Punto> copia = new ArrayList<>();
        for (Punto punto : puntos) {
            copia.add(new Punto(punto.getX(), punto.getY()));
        }
        return copia;
    }

    public void avanzar() {
        if (indice < puntos.size()) {
            indice++;
        }
    }

    public Punto mover(Punto posicion, double distanciaMaxima) {
        if (posicion == null) {
            throw new IllegalArgumentException("La posición actual es obligatoria");
        }
        if (!Double.isFinite(distanciaMaxima) || distanciaMaxima < 0.0) {
            throw new IllegalArgumentException("La distancia de movimiento debe ser finita y no negativa");
        }

        Punto actual = posicion;
        double distanciaRestante = distanciaMaxima;
        while (indice < puntos.size()) {
            Punto siguiente = puntos.get(indice);
            double distancia = actual.distancia(siguiente);
            if (distancia <= TOLERANCIA) {
                actual = siguiente;
                indice++;
            } else if (distanciaRestante < distancia) {
                return actual.moverHacia(siguiente, distanciaRestante);
            } else {
                actual = siguiente;
                distanciaRestante -= distancia;
                indice++;
            }
        }
        return actual;
    }

    public boolean esValida() {
        return !puntos.isEmpty();
    }

    public boolean terminada() {
        return indice >= puntos.size();
    }

    private static final class RedVial {
        private final List<Punto> nodos = new ArrayList<>();
        private final Map<Punto, Integer> indices = new HashMap<>();
        private final List<List<Arista>> aristas = new ArrayList<>();
        private final List<Segmento> segmentos = new ArrayList<>();
    }

    private static final class Segmento {
        private final Punto inicio;
        private final Punto fin;
        private final int idInicio;
        private final int idFin;

        private Segmento(Punto inicio, Punto fin, int idInicio, int idFin) {
            this.inicio = inicio;
            this.fin = fin;
            this.idInicio = idInicio;
            this.idFin = idFin;
        }
    }

    private static final class Proyeccion {
        private final Segmento segmento;
        private final Punto punto;

        private Proyeccion(Segmento segmento, Punto punto) {
            this.segmento = segmento;
            this.punto = punto;
        }
    }

    private static final class Arista {
        private final int destino;
        private final double largo;

        private Arista(int destino, double largo) {
            this.destino = destino;
            this.largo = largo;
        }
    }
}
