package red;

import game.Jugador;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import model.Animal;
import model.AnimalJugador;
import partida.EstadoPartida;
import partida.Partida;

public class ComandoAtacar implements IComando {
    @Override
    public void ejecutar(ManejadorCliente cliente, Mensaje mensaje) {
        Jugador jugador = ComandoMover.jugadorEnPartida(cliente);
        Partida partida = jugador.getPartida();
        if (partida.getEstado() != EstadoPartida.EN_JUEGO) {
            throw new IllegalStateException("La partida no está en juego");
        }
        DatosAtacar datos = (DatosAtacar) mensaje.getPayload(DatosAtacar.class);
        if (datos == null || datos.idsAnimales == null || datos.idsAnimales.isEmpty()) {
            throw new IllegalArgumentException("Se requieren animales para atacar");
        }
        Animal objetivo = partida.getMundo().buscarAnimal(datos.idObjetivo);
        if (objetivo == null || objetivo.getManada() == jugador.getManada()) {
            throw new IllegalArgumentException("El objetivo no existe o pertenece al jugador");
        }

        Set<Integer> idsUnicos = new HashSet<>();
        ArrayList<AnimalJugador> atacantes = new ArrayList<>();
        for (Integer id : datos.idsAnimales) {
            if (id == null || !idsUnicos.add(id)) {
                throw new IllegalArgumentException("La lista de animales contiene ids inválidos o repetidos");
            }
            Animal propio = ComandoMover.animalPropio(jugador, id);
            if (!(propio instanceof AnimalJugador)) {
                throw new IllegalArgumentException("Solo los animales del jugador pueden atacar");
            }
            atacantes.add((AnimalJugador) propio);
        }
        for (AnimalJugador atacante : atacantes) {
            atacante.setRuta(null);
            atacante.setObjetivo(objetivo);
        }
    }
}
