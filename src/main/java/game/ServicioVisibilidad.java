package game;

import java.util.Vector;
import java.util.HashMap;
import java.util.Map;
import model.Animal;
import model.AnimalBot;
import model.AnimalJugador;
import model.Bandera;
import model.ManadaBot;
import model.ManadaJugador;
import model.TazaCafe;
import red.EstadoVisible;
import red.VistaAnimal;

public class ServicioVisibilidad {
    public Vector<Integer> zonasVisibles(Jugador jugador) {
        Vector<Integer> zonas = new Vector<>();
        if (jugador == null || jugador.getManada() == null) {
            return zonas;
        }
        for (Animal animal : jugador.getManada().getVivos()) {
            if (!zonas.contains(animal.getIdZona())) {
                zonas.add(animal.getIdZona());
            }
        }
        return zonas;
    }

    public EstadoVisible crearSnapshot(MundoJuego mundo, Jugador jugador) {
        if (mundo == null || jugador == null || jugador.getManada() == null) {
            throw new IllegalArgumentException("El mundo y el jugador inicializado son obligatorios");
        }
        Vector<Integer> visibles = zonasVisibles(jugador);
        EstadoVisible estado = new EstadoVisible();
        estado.zonasVisibles = visibles;
        estado.animales = new Vector<>();
        estado.animalesPorZona = new HashMap<>();
        for (Animal animal : jugador.getManada().getVivos()) {
            estado.animalesPorZona.merge(animal.getIdZona(), 1, Integer::sum);
        }
        estado.tazas = new Vector<>();
        estado.eventos = new Vector<>();
        estado.animalesVivos = jugador.getManada().getVivos().size();
        estado.danio = jugador.getManada().getDanio();
        estado.tazasConsumidas = jugador.getTazasConsumidas();
        estado.botsEliminados = jugador.getBotsEliminados();
        int zona = jugador.getZonaVista();
        if (mundo.getMapa() != null && mundo.getMapa().getZona(zona) == null && !visibles.isEmpty()) {
            zona = visibles.firstElement();
        }
        jugador.setZonaVista(zona);
        estado.idZona = zona;
        if (!visibles.contains(zona)) {
            return estado;
        }

        for (ManadaJugador manada : mundo.getManadasJugador()) {
            agregarAnimales(estado.animales, manada, jugador, zona);
        }
        for (ManadaBot manada : mundo.getManadasBot()) {
            agregarAnimales(estado.animales, manada, jugador, zona);
        }
        for (TazaCafe taza : mundo.getTazas()) {
            if (taza != null && !taza.isConsumida() && taza.getIdZona() == zona) {
                estado.tazas.add(taza.getPos());
            }
        }
        for (Bandera bandera : mundo.getBanderas()) {
            if (bandera != null && bandera.getIdZona() == zona) {
                estado.bandera = bandera.getPos();
                estado.colorBandera = bandera.getColor();
                break;
            }
        }
        return estado;
    }

    private static void agregarAnimales(Vector<VistaAnimal> destino, model.Manada manada,
            Jugador jugador, int zona) {
        if (manada == null) {
            return;
        }
        for (Animal animal : manada.getVivos()) {
            if (animal.getIdZona() != zona) {
                continue;
            }
            VistaAnimal vista = new VistaAnimal();
            vista.id = animal.getId();
            vista.especie = animal.getEspecie();
            vista.esBot = animal.esBot();
            vista.esMio = manada == jugador.getManada();
            vista.x = animal.getPosicion().getX();
            vista.y = animal.getPosicion().getY();
            vista.energia = animal.getEnergia();
            if (animal instanceof AnimalJugador) {
                AnimalJugador animalJugador = (AnimalJugador) animal;
                Animal objetivo = animalJugador.getObjetivo();
                vista.idObjetivo = objetivo == null ? null : objetivo.getId();
                vista.objetivoX = objetivo == null ? null : objetivo.getPosicion().getX();
                vista.objetivoY = objetivo == null ? null : objetivo.getPosicion().getY();
                vista.atacando = animalJugador.isAtacando();
            } else if (animal instanceof AnimalBot) {
                AnimalBot animalBot = (AnimalBot) animal;
                Animal objetivo = animalBot.getObjetivo();
                vista.idObjetivo = objetivo == null ? null : objetivo.getId();
                vista.objetivoX = objetivo == null ? null : objetivo.getPosicion().getX();
                vista.objetivoY = objetivo == null ? null : objetivo.getPosicion().getY();
                vista.atacando = animalBot.isAtacando();
            }
            destino.add(vista);
        }
    }
}
