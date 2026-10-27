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
                    animal.avanzar(deltaSeg);
                    actualizarZona(mundo, animal);
                }
            }
        }
    }

    private static void actualizarZona(MundoJuego mundo, Animal animal) {
        mapa.Zona zona = mundo.getMapa() == null ? null : mundo.getMapa().zonaEn(animal.getPosicion());
        if (zona != null) {
            animal.setIdZona(zona.getId());
        }
    }
}
