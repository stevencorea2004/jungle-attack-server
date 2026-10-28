package red;

import game.Jugador;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import mapa.Ruta;
import model.Animal;
import model.AnimalJugador;
import partida.EstadoPartida;
import partida.Partida;
import util.Constantes;

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
        if (objetivo == null) {
            return;
        }
        if (objetivo.getManada() == jugador.getManada()) {
            throw new IllegalArgumentException("El objetivo no existe o pertenece al jugador");
        }
        if (!objetivo.estaVivo()) {
            return;
        }

        Set<Integer> idsUnicos = new HashSet<>();
        ArrayList<AnimalJugador> atacantes = new ArrayList<>();
        ArrayList<Ruta> rutas = new ArrayList<>();
        for (Integer id : datos.idsAnimales) {
            if (id == null || !idsUnicos.add(id)) {
                throw new IllegalArgumentException("La lista de animales contiene ids inválidos o repetidos");
            }
            Animal propio = ComandoMover.animalPropio(jugador, id);
            if (!(propio instanceof AnimalJugador)) {
                throw new IllegalArgumentException("Solo los animales del jugador pueden atacar");
            }
            AnimalJugador atacante = (AnimalJugador) propio;
            Ruta ruta = null;
            if (!atacante.estaEnRadio(objetivo, Constantes.RADIO_ENCUENTRO)) {
                ruta = Ruta.crear(partida.getMundo().getMapa(), null,
                        atacante.getPosicion(), objetivo.getPosicion());
                if (!ruta.esValida()) {
                    throw new IllegalArgumentException("No existe un camino transitable hasta el objetivo");
                }
            }
            atacantes.add(atacante);
            rutas.add(ruta);
        }
        for (int i = 0; i < atacantes.size(); i++) {
            AnimalJugador atacante = atacantes.get(i);
            atacante.setRuta(rutas.get(i));
            atacante.setObjetivo(objetivo);
        }
    }
}
