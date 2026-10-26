package game;

import model.Animal;
import model.AnimalBot;
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
            for (Animal bot : manadaBot.getVivos()) {
                if (bot == null || !bot.estaVivo() || !bot.esBot()) {
                    continue;
                }
                for (ManadaJugador manadaJugador : mundo.getManadasJugador()) {
                    if (manadaJugador == null || manadaJugador.estaEliminada()) {
                        continue;
                    }
                    for (Animal animalJugador : manadaJugador.getVivos()) {
                        if (animalJugador == null || !animalJugador.estaVivo()) {
                            continue;
                        }
                        if (bot.estaEnRadio(animalJugador, Constantes.RADIO_ENCUENTRO)) {
                            bot.atacar(animalJugador, ahora);
                        }
                    }
                }
            }
        }
    }
}
