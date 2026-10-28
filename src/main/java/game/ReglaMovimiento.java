package game;

import java.util.Random;

import mapa.Mapa;
import mapa.Ruta;
import mapa.Zona;
import model.Animal;
import model.AnimalBot;
import model.ManadaBot;
import model.ManadaJugador;
import model.Punto;
import util.Constantes;

public class ReglaMovimiento implements IReglaJuego {
    private final Random random;

    public ReglaMovimiento() {
        this(new Random());
    }

    ReglaMovimiento(Random random) {
        if (random == null) {
            throw new IllegalArgumentException("El generador aleatorio es obligatorio");
        }
        this.random = random;
    }

    @Override
    public void aplicar(MundoJuego mundo, long ahora, double deltaSeg) {
        if (mundo == null) {
            return;
        }
        for (ManadaJugador manada : mundo.getManadasJugador()) {
            if (manada == null) {
                continue;
            }
            for (Animal animal : manada.getVivos()) {
                if (animal != null) {
                    animal.avanzar(deltaSeg);
                    actualizarZona(mundo, animal);
                }
            }
        }
        for (ManadaBot manada : mundo.getManadasBot()) {
            if (manada == null) {
                continue;
            }
            for (Animal animal : manada.getVivos()) {
                if (animal != null) {
                    if (animal instanceof AnimalBot) {
                        AnimalBot bot = (AnimalBot) animal;
                        ManadaBot manadaBot = (ManadaBot) bot.getManada();
                        Zona zonaInicial = mundo.getMapa() == null || manadaBot == null
                                ? null : mundo.getMapa().getZona(manadaBot.getIdZonaInicial());
                        if (zonaInicial == null || !zonaInicial.contiene(bot.getPosicion())) {
                            bot.setRuta(null);
                            bot.limpiarObjetivo();
                            continue;
                        }
                        if (bot.isAtacando()) {
                            bot.setRuta(null);
                            continue;
                        }
                        Animal objetivo = bot.getObjetivo();
                        if (objetivo != null && objetivo.estaVivo()
                                && bot.estaEnRadio(objetivo, Constantes.RADIO_ENCUENTRO)) {
                            bot.setRuta(null);
                            continue;
                        }
                        if (objetivo != null && objetivo.estaVivo()) {
                            animal.avanzar(deltaSeg);
                            actualizarZona(mundo, animal);
                            continue;
                        }
                    }
                    if (!prepararPatrulla(mundo.getMapa(), animal)) {
                        animal.setRuta(null);
                        continue;
                    }
                    animal.avanzar(deltaSeg);
                }
            }
        }
    }

    private boolean prepararPatrulla(Mapa mapa, Animal animal) {
        if (mapa == null) {
            return false;
        }
        int idZona = animal.getIdZona();
        if (animal instanceof AnimalBot && animal.getManada() instanceof ManadaBot) {
            idZona = ((ManadaBot) animal.getManada()).getIdZonaInicial();
        }
        Zona zona = mapa.getZona(idZona);
        if (zona == null || !zona.contiene(animal.getPosicion())) {
            return false;
        }
        Ruta ruta = animal.getRuta();
        if (ruta != null && !ruta.terminada()) {
            return true;
        }
        Punto destino = mapa.puntoAleatorioEnZona(zona.getId(), random);
        Ruta patrulla = animal instanceof AnimalBot
                ? Ruta.crearEnZona(mapa, idZona, animal.getPosicion(), destino)
                : Ruta.crear(mapa, null, animal.getPosicion(), destino);
        if (!patrulla.esValida()) {
            throw new IllegalStateException("No se pudo crear una ruta de patrulla para el bot");
        }
        animal.setRuta(patrulla);
        return true;
    }

    private static void actualizarZona(MundoJuego mundo, Animal animal) {
        Zona zona = mundo.getMapa() == null ? null : mundo.getMapa().zonaEn(animal.getPosicion());
        if (zona != null) {
            int zonaAnterior = animal.getIdZona();
            animal.setIdZona(zona.getId());
            if (zonaAnterior != zona.getId() && animal.getManada() instanceof ManadaJugador) {
                game.Jugador dueno = ((ManadaJugador) animal.getManada()).getDueno();
                if (dueno != null && dueno.getZonaVista() == zonaAnterior) {
                    dueno.setZonaVista(zona.getId());
                }
            }
        }
    }
}
