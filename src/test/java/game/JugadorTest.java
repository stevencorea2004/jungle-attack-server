package game;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Rectangle2D;
import java.util.Random;
import mapa.Zona;
import model.Animal;
import model.Especie;
import model.ManadaJugador;
import org.junit.jupiter.api.Test;

class JugadorTest {
    @Test
    void botEliminationAwardsEnergyBonusEveryTwoKills() {
        Jugador jugador = new Jugador();

        assertFalse(jugador.registrarBotEliminado());
        assertTrue(jugador.registrarBotEliminado());
        assertFalse(jugador.registrarBotEliminado());
        assertTrue(jugador.registrarBotEliminado());
    }

    @Test
    void visibilityRequiresALivingPackAnimalInTheRequestedZone() {
        Jugador jugador = new Jugador();
        ManadaJugador manada = new ManadaJugador(Especie.JAGUAR, jugador);
        jugador.setManada(manada);
        manada.crearMiembros(zona(1), new Random(1));

        assertTrue(jugador.puedeVer(1));
        assertFalse(jugador.puedeVer(2));
        assertFalse(jugador.puedeVer(0));

        for (Animal animal : manada.getVivos()) {
            animal.recibirDanio(animal.getEnergiaMax());
        }
        assertFalse(jugador.puedeVer(1));
    }

    @Test
    void visibilityIsFalseBeforeThePackIsAssigned() {
        assertFalse(new Jugador().puedeVer(0));
        assertFalse(new Jugador().puedeVer(1));
    }

    private static Zona zona(int id) {
        return new Zona(id, new Rectangle2D.Double(0, 0, 100, 100));
    }
}
