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
        if (mapa == null || inicio == null || destino == null) {
            return new Ruta(lista);
        }

        Zona origen = mapa.zonaEn(inicio);
        Zona fin = mapa.zonaEn(destino);
        if (origen != null && fin != null && origen.getId() != fin.getId()) {
            Vector<Zona> rutaZonas = new BuscadorBFS().buscarRuta(mapa, origen.getId(), fin.getId());
            if (rutaZonas.isEmpty() || rutaZonas.get(rutaZonas.size() - 1).getId() != fin.getId()) {
                return new Ruta(new Vector<>());
            }
            for (int i = 1; i < rutaZonas.size(); i++) {
                Camino camino = mapa.getCamino(rutaZonas.get(i - 1).getId(), rutaZonas.get(i).getId());
                if (camino == null) {
                    return new Ruta(new Vector<>());
                }
                Vector<Punto> puntosCamino = camino.getPuntos();
                if (camino.getIdZonaA() == rutaZonas.get(i - 1).getId()) {
                    agregarPuntos(lista, puntosCamino, false);
                } else {
                    agregarPuntos(lista, puntosCamino, true);
                }
            }
        }
        if (!destino.equals(lista.isEmpty() ? null : lista.lastElement())) {
            lista.add(new Punto(destino.getX(), destino.getY()));
        }
        return new Ruta(lista);
    }

    private static void agregarPuntos(Vector<Punto> destino, Vector<Punto> origen, boolean inverso) {
        for (int i = 0; i < origen.size(); i++) {
            Punto punto = origen.get(inverso ? origen.size() - 1 - i : i);
            if (destino.isEmpty() || !destino.lastElement().equals(punto)) {
                destino.add(new Punto(punto.getX(), punto.getY()));
            }
        }
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
