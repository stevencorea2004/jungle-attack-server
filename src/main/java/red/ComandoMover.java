package red;

import game.Jugador;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import mapa.Mapa;
import mapa.Ruta;
import mapa.Zona;
import model.Animal;
import model.AnimalJugador;
import model.Punto;
import partida.EstadoPartida;
import partida.Partida;

public class ComandoMover implements IComando {
    private static final double DISPERSION_MINIMA = 18.0;
    private static final double DISPERSION_MAXIMA = 42.0;

    @Override
    public void ejecutar(ManejadorCliente cliente, Mensaje mensaje) {
        Jugador jugador = jugadorEnPartida(cliente);
        Partida partida = jugador.getPartida();
        if (partida.getEstado() != EstadoPartida.EN_JUEGO) {
            throw new IllegalStateException("La partida no está en juego");
        }
        DatosMover datos = (DatosMover) mensaje.getPayload(DatosMover.class);
        if (datos == null || datos.idsAnimales == null || datos.idsAnimales.isEmpty()
                || !Double.isFinite(datos.destinoX) || !Double.isFinite(datos.destinoY)) {
            throw new IllegalArgumentException("Se requieren animales y un destino válido");
        }

        Punto destino = new Punto(datos.destinoX, datos.destinoY);
        Mapa mapa = partida.getMundo().getMapa();
        Zona zonaDestino = mapa.zonaEn(destino);
        if (zonaDestino == null) {
            throw new IllegalArgumentException("El destino debe estar dentro del mapa");
        }
        Set<Integer> idsUnicos = new HashSet<>();
        ArrayList<Animal> animales = new ArrayList<>();
        for (Integer id : datos.idsAnimales) {
            if (id == null || !idsUnicos.add(id)) {
                throw new IllegalArgumentException("La lista de animales contiene ids inválidos o repetidos");
            }
            animales.add(animalPropio(jugador, id));
        }
        ArrayList<Ruta> rutas = new ArrayList<>();
        for (Animal animal : animales) {
            Ruta ruta = Ruta.crear(partida.getMundo().getMapa(), null, animal.getPosicion(), destino);
            if (!ruta.esValida()) {
                throw new IllegalArgumentException("No existe un camino transitable hasta el destino");
            }
            if (animales.size() > 1) {
                double dispersion = ThreadLocalRandom.current()
                        .nextDouble(DISPERSION_MINIMA, DISPERSION_MAXIMA);
                ruta = recortarFinalRuta(ruta, dispersion, mapa, zonaDestino.getId());
            }
            rutas.add(ruta);
        }
        for (int i = 0; i < animales.size(); i++) {
            Animal animal = animales.get(i);
            animal.setRuta(rutas.get(i));
            if (animal instanceof AnimalJugador) {
                ((AnimalJugador) animal).limpiarObjetivo();
            }
        }
    }

    static Ruta recortarFinalRuta(Ruta ruta, double distancia) {
        if (ruta == null || !ruta.esValida() || !Double.isFinite(distancia) || distancia <= 0.0) {
            return ruta;
        }

        List<Punto> puntos = ruta.getPuntos();
        double largoTotal = 0.0;
        for (int i = 1; i < puntos.size(); i++) {
            largoTotal += puntos.get(i - 1).distancia(puntos.get(i));
        }
        if (largoTotal <= 0.0) {
            return ruta;
        }

        double restante = Math.min(distancia, largoTotal * 0.25);
        for (int i = puntos.size() - 1; i > 0; i--) {
            Punto anterior = puntos.get(i - 1);
            Punto actual = puntos.get(i);
            double largoSegmento = anterior.distancia(actual);
            if (restante <= largoSegmento) {
                List<Punto> rutaDispersa = new ArrayList<>(puntos.subList(0, i));
                rutaDispersa.add(actual.moverHacia(anterior, restante));
                return new Ruta(rutaDispersa);
            }
            restante -= largoSegmento;
        }
        return ruta;
    }

    static Ruta recortarFinalRuta(Ruta ruta, double distancia, Mapa mapa, int idZonaDestino) {
        Ruta rutaDispersa = recortarFinalRuta(ruta, distancia);
        List<Punto> puntos = rutaDispersa.getPuntos();
        if (!puntos.isEmpty()) {
            Zona zonaFinal = mapa.zonaEn(puntos.get(puntos.size() - 1));
            if (zonaFinal != null && zonaFinal.getId() == idZonaDestino) {
                return rutaDispersa;
            }
        }
        return ruta;
    }

    static Jugador jugadorEnPartida(ManejadorCliente cliente) {
        Jugador jugador = cliente == null ? null : cliente.getJugador();
        if (jugador == null || jugador.getPartida() == null) {
            throw new IllegalStateException("La conexión no pertenece a una partida");
        }
        return jugador;
    }

    static Animal animalPropio(Jugador jugador, int id) {
        if (jugador.getManada() == null) {
            throw new IllegalStateException("La manada del jugador no está inicializada");
        }
        for (Animal animal : jugador.getManada().getVivos()) {
            if (animal.getId() == id) {
                return animal;
            }
        }
        throw new IllegalArgumentException("El animal no existe o no pertenece al jugador");
    }
}
