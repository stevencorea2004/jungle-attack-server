package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import util.Constantes;

class GameModelTest {
    @Test
    void puntoCalculatesDistanceAndMovesWithoutOvershooting() {
        Punto origen = new Punto(0, 0);
        Punto destino = new Punto(3, 4);

        assertEquals(5.0, origen.distancia(destino), 0.0001);
        Punto avance = origen.moverHacia(destino, 2.0);
        assertEquals(2.0, origen.distancia(avance), 0.0001);
        assertEquals(origen, destino.moverHacia(origen, 10.0));
    }

    @Test
    void attackAppliesPackDamageOncePerCooldownAndKillsAtZero() {
        ManadaJugador atacantes = new ManadaJugador(Especie.JAGUAR, null);
        ManadaBot defensores = new ManadaBot(Especie.DANTA, 1);
        AnimalJugador jugador = (AnimalJugador) atacantes.crearMiembro(1, new Punto(0, 0));
        AnimalBot bot = (AnimalBot) defensores.crearMiembro(2, new Punto(0, 0));
        atacantes.miembros.add(jugador);
        defensores.miembros.add(bot);

        long ahora = 10_000L;
        jugador.ultimoAtaque = ahora - Constantes.COOLDOWN_ATAQUE_MS;
        assertTrue(jugador.atacar(bot, ahora));
        assertEquals(Constantes.ENERGIA_BOT - Constantes.DANIO_JUGADOR, bot.energia);
        assertFalse(jugador.atacar(bot, ahora + 100L));

        bot.recibirDanio(Constantes.ENERGIA_BOT);
        assertFalse(bot.estaVivo());
        assertEquals(0, bot.energia);
    }

    @Test
    void animalsCannotDamageMembersOfTheirOwnPack() {
        ManadaJugador manada = new ManadaJugador(Especie.JAGUAR, null);
        AnimalJugador atacante = (AnimalJugador) manada.crearMiembro(1, new Punto(0, 0));
        AnimalJugador aliado = (AnimalJugador) manada.crearMiembro(2, new Punto(0, 0));
        manada.miembros.add(atacante);
        manada.miembros.add(aliado);
        int energiaInicial = aliado.getEnergia();

        assertFalse(atacante.atacar(aliado, 10_000L));
        assertEquals(energiaInicial, aliado.getEnergia());
        assertEquals(null, aliado.getUltimoAtacante());
    }

    @Test
    void coffeeCupIncreasesPackDamageOnlyOnce() {
        ManadaJugador manada = new ManadaJugador(Especie.PIZOTE, null);
        AnimalJugador animal = (AnimalJugador) manada.crearMiembro(1, new Punto(0, 0));
        manada.miembros.add(animal);
        TazaCafe taza = new TazaCafe();

        assertTrue(taza.consumir(manada));
        assertEquals(Constantes.DANIO_JUGADOR + Constantes.BONO_DANIO_TAZA, manada.danio);
        assertFalse(taza.consumir(manada));
        assertTrue(taza.isConsumida());
    }

    @Test
    void energyBonusDoesNotExceedPlayerMaximum() {
        ManadaJugador manada = new ManadaJugador(Especie.TUCAN, null);
        AnimalJugador animal = (AnimalJugador) manada.crearMiembro(5, new Punto(0, 0));
        animal.energia = Constantes.ENERGIA_MAX_JUGADOR - 1;
        manada.miembros.add(animal);

        manada.aplicarBonoEnergia(4);

        assertEquals(Constantes.ENERGIA_MAX_JUGADOR, animal.energia);
        assertNotNull(manada.getVivos());
    }
}
