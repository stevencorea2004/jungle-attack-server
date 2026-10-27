package red;

import game.Jugador;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import mapa.Ruta;
import model.Animal;
import model.AnimalJugador;
import model.Punto;
import partida.EstadoPartida;
import partida.Partida;

public class ComandoMover implements IComando {
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
        if (partida.getMundo().getMapa().zonaEn(destino) == null) {
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
