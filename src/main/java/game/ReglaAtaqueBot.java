package game;

import mapa.Mapa;
import mapa.Ruta;
import mapa.Zona;
import model.Animal;
import model.AnimalBot;
import model.AnimalJugador;
import model.ManadaBot;
import model.ManadaJugador;
import util.Constantes;

public class ReglaAtaqueBot implements IReglaJuego {
    @Override
    public void aplicar(MundoJuego mundo, long ahora, double deltaSeg) {
        if (mundo == null) {
            return;
        }
        for (ManadaBot manadaBot : mundo.getManadasBot()) {
            if (manadaBot == null || manadaBot.estaEliminada()) {
                continue;
            }
            Mapa mapa = mundo.getMapa();
            Zona zonaInicial = mapa == null ? null : mapa.getZona(manadaBot.getIdZonaInicial());
            if (zonaInicial == null) {
                continue;
            }
            for (Animal bot : manadaBot.getVivos()) {
                if (!(bot instanceof AnimalBot) || !bot.estaVivo()) {
                    continue;
                }
                AnimalBot animalBot = (AnimalBot) bot;
                Animal objetivo = null;
                double distanciaObjetivo = Double.POSITIVE_INFINITY;
                for (ManadaJugador manadaJugador : mundo.getManadasJugador()) {
                    if (manadaJugador == null || manadaJugador.estaEliminada()) {
                        continue;
                    }
                    for (Animal animalJugador : manadaJugador.getVivos()) {
                        if (!(animalJugador instanceof AnimalJugador) || !animalJugador.estaVivo()) {
                            continue;
                        }
                        if (!zonaInicial.contiene(animalJugador.getPosicion())) {
                            continue;
                        }
                        double distancia = bot.getPosicion().distancia(animalJugador.getPosicion());
                        if (distancia < distanciaObjetivo) {
                            objetivo = animalJugador;
                            distanciaObjetivo = distancia;
                        }
                    }
                }
                if (objetivo == null) {
                    animalBot.limpiarObjetivo();
                    animalBot.setRuta(null);
                    continue;
                }
                if (animalBot.getObjetivo() != objetivo) {
                    animalBot.setRuta(null);
                }
                animalBot.setObjetivo(objetivo);
                if (!bot.estaEnRadio(objetivo, Constantes.RADIO_ENCUENTRO)) {
                    animalBot.setAtacando(false);
                    Ruta ruta = animalBot.getRuta();
                    if (ruta == null || ruta.terminada()) {
                        ruta = Ruta.crearEnZona(mapa, manadaBot.getIdZonaInicial(),
                                animalBot.getPosicion(), objetivo.getPosicion());
                        if (!ruta.esValida()) {
                            animalBot.limpiarObjetivo();
                            animalBot.setRuta(null);
                            continue;
                        }
                        animalBot.setRuta(ruta);
                    }
                    continue;
                }
                animalBot.setRuta(null);
                animalBot.setAtacando(true);
                animalBot.atacar(objetivo, ahora);
            }
        }
    }
}
