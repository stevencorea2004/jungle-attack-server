package game;

import model.Animal;
import model.AnimalJugador;
import model.ManadaJugador;
import util.Constantes;

public class ReglaAtaqueJugador implements IReglaJuego {
    @Override
    public void aplicar(MundoJuego mundo, long ahora, double deltaSeg) {
        if (mundo == null) {
            return;
        }
        for (ManadaJugador manada : mundo.getManadasJugador()) {
            if (manada == null) {
                continue;
            }
            for (Animal atacante : manada.getVivos()) {
                if (!(atacante instanceof AnimalJugador)) {
                    continue;
                }
                AnimalJugador jugador = (AnimalJugador) atacante;
                Animal objetivo = jugador.getObjetivo();
                if (objetivo == null) {
                    continue;
                }
                if (!objetivo.estaVivo() || objetivo.getManada() == manada
                        || !jugador.estaEnRadio(objetivo, Constantes.RADIO_ENCUENTRO)) {
                    jugador.limpiarObjetivo();
                    continue;
                }
                jugador.atacar(objetivo, ahora);
            }
        }
    }
}
