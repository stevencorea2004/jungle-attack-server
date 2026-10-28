package game;

import model.Animal;
import model.ManadaBot;
import model.ManadaJugador;
import mapa.Zona;

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
