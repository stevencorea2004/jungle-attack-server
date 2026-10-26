package game;

import model.Animal;
import model.ManadaBot;
import model.ManadaJugador;

public class ReglaMovimiento implements IReglaJuego {
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
                }
            }
        }
        for (ManadaBot manada : mundo.getManadasBot()) {
            if (manada == null) {
                continue;
            }
            for (Animal animal : manada.getVivos()) {
                if (animal != null) {
                    animal.avanzar(deltaSeg);
                }
            }
        }
    }
}
