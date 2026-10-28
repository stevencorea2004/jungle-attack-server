package game;

import java.util.Random;
import mapa.Mapa;
import mapa.Ruta;
import mapa.Zona;
import model.Animal;
import model.ManadaBot;
import model.ManadaJugador;
import model.Punto;

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
        Zona zona = mapa.getZona(animal.getIdZona());
        if (zona == null || !zona.contiene(animal.getPosicion())) {
            return false;
        }
        Ruta ruta = animal.getRuta();
        if (ruta != null && !ruta.terminada()) {
            return true;
        }
        Punto destino = mapa.puntoAleatorioEnZona(zona.getId(), random);
        Ruta patrulla = Ruta.crear(mapa, null, animal.getPosicion(), destino);
        if (!patrulla.esValida()) {
            throw new IllegalStateException("No se pudo crear una ruta de patrulla para el bot");
        }
        animal.setRuta(patrulla);
        return true;
    }

    private static void actualizarZona(MundoJuego mundo, Animal animal) {
        mapa.Zona zona = mundo.getMapa() == null ? null : mundo.getMapa().zonaEn(animal.getPosicion());
        if (zona != null) {
            animal.setIdZona(zona.getId());
        }
    }
}
