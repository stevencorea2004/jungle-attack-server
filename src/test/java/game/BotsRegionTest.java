package game;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Random;
import mapa.Camino;
import mapa.Mapa;
import mapa.Zona;
import model.Animal;
import model.Especie;
import model.ManadaBot;
import model.ManadaJugador;
import model.Punto;
import org.junit.jupiter.api.Test;

class BotsRegionTest {
    @Test
    void botDoesNotChaseOrPatrolOutsideItsStartingZone() {
        Mapa mapa = new Mapa();
        Zona zonaBot = new Zona(1, new Rectangle2D.Double(0, 0, 100, 100));
        Zona zonaJugador = new Zona(2, new Rectangle2D.Double(100, 0, 100, 100));
        mapa.agregarZona(zonaBot);
        mapa.agregarZona(zonaJugador);
        mapa.agregarCamino(new Camino(1, 2,
                List.of(new Punto(0, 50), new Punto(100, 50), new Punto(200, 50))));

        MundoJuego mundo = new MundoJuego();
        mundo.setMapa(mapa);
        ManadaBot bots = new ManadaBot(Especie.DANTA, 1);
        bots.crearMiembros(zonaBot, new Random(1), mapa);
        Animal bot = bots.getVivos().firstElement();
        bot.setUbicacion(bot.getId(), new Punto(75, 50), 1);
        mundo.getManadasBot().add(bots);

        ManadaJugador jugadores = new ManadaJugador(Especie.JAGUAR, new Jugador());
        jugadores.crearMiembros(zonaJugador, new Random(2), mapa);
        Animal jugador = jugadores.getVivos().firstElement();
        jugador.setUbicacion(jugador.getId(), new Punto(125, 50), 2);
        mundo.getManadasJugador().add(jugadores);

        ReglaAtaqueBot reglaAtaque = new ReglaAtaqueBot();
        ReglaMovimiento reglaMovimiento = new ReglaMovimiento(new Random(3));
        Punto posicionInicial = new Punto(bot.getPosicion().getX(), bot.getPosicion().getY());
        for (int tick = 0; tick < 200; tick++) {
            reglaAtaque.aplicar(mundo, tick * 50L, 0.05);
            reglaMovimiento.aplicar(mundo, tick * 50L, 0.05);
            assertTrue(zonaBot.contiene(bot.getPosicion()));
            assertTrue(bot.getIdZona() == zonaBot.getId());
        }

        assertTrue(bot.getPosicion().distancia(posicionInicial) > 0.0);
        assertNull(((model.AnimalBot) bot).getObjetivo());
    }
}
