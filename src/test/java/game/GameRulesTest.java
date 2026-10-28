package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;
import java.util.Random;
import mapa.BuscadorBFS;
import mapa.Camino;
import mapa.MainMapa;
import mapa.Mapa;
import mapa.Ruta;
import mapa.Zona;
import model.Animal;
import model.AnimalBot;
import model.AnimalJugador;
import model.Bandera;
import model.Especie;
import model.ManadaBot;
import model.ManadaJugador;
import model.Punto;
import model.TazaCafe;
import red.EstadoVisible;
import org.junit.jupiter.api.Test;

class GameRulesTest {
    @Test
    void playerAndBotAnimalsMoveAtTheSharedReducedSpeed() {
        ManadaJugador jugadores = new ManadaJugador(Especie.JAGUAR, new Jugador());
        ManadaBot bots = new ManadaBot(Especie.DANTA, 1);
        jugadores.crearMiembros(zona(1), new Random(11));
        bots.crearMiembros(zona(1), new Random(12));
        List<Animal> animales = List.of(
                jugadores.getVivos().firstElement(),
                bots.getVivos().firstElement());

        for (Animal animal : animales) {
            ubicar(animal, animal.getId(), 10, 50);
            animal.setRuta(new Ruta(List.of(new Punto(100, 50))));
            animal.avanzar(1.0);
            assertEquals(45.0, animal.getPosicion().distancia(new Punto(10, 50)), 0.000001);
        }
    }

    @Test
    void botsPatrolAlongPathsWithinTheirStartingZone() {
        MundoJuego mundo = new MundoJuego();
        var mapa = MainMapa.crearMapa();
        mundo.setMapa(mapa);
        ManadaBot bots = new ManadaBot(Especie.DANTA, 1);
        bots.crearMiembros(mapa.getZona(1), new Random(11), mapa);
        mundo.getManadasBot().add(bots);
        Animal bot = bots.getVivos().firstElement();
        Punto inicio = new Punto(bot.getPosicion().getX(), bot.getPosicion().getY());

        ReglaMovimiento regla = new ReglaMovimiento(new Random(12));
        boolean seMovio = false;
        for (int tick = 1; tick <= 400; tick++) {
            regla.aplicar(mundo, tick * 50L, 0.05);
            if (bot.getPosicion().distancia(inicio) > 0.0) {
                seMovio = true;
            }
            assertTrue(mapa.getZona(1).contiene(bot.getPosicion()));
        }

        assertTrue(seMovio);
        assertEquals(1, bot.getIdZona());
        assertTrue(bot.getRuta() != null);
    }

    @Test
    void botsOutsideTheMapDoNotStopMovementRule() {
        MundoJuego mundo = new MundoJuego();
        var mapa = MainMapa.crearMapa();
        mundo.setMapa(mapa);
        ManadaBot bots = new ManadaBot(Especie.DANTA, 1);
        bots.crearMiembros(mapa.getZona(1), new Random(11), mapa);
        mundo.getManadasBot().add(bots);
        Animal bot = bots.getVivos().firstElement();
        bot.setUbicacion(bot.getId(), new Punto(2_000, 2_000), 0);

        assertDoesNotThrow(() -> new ReglaMovimiento(new Random(12)).aplicar(mundo, 50L, 0.05));
        assertNull(bot.getRuta());
        assertEquals(new Punto(2_000, 2_000), bot.getPosicion());
    }

    @Test
    void playerAnimalsDoNotPatrolAutomatically() {
        MundoJuego mundo = new MundoJuego();
        var mapa = MainMapa.crearMapa();
        mundo.setMapa(mapa);
        ManadaJugador manada = new ManadaJugador(Especie.JAGUAR, new Jugador());
        manada.crearMiembros(mapa.getZona(1), new Random(11), mapa);
        mundo.getManadasJugador().add(manada);
        Animal animal = manada.getVivos().firstElement();
        Punto inicio = new Punto(animal.getPosicion().getX(), animal.getPosicion().getY());

        new ReglaMovimiento(new Random(12)).aplicar(mundo, 50L, 0.05);

        assertEquals(inicio, animal.getPosicion());
        assertNull(animal.getRuta());
    }

    @Test
    void selectedPlayerAnimalsAttackOnlyAtCooldownIntervals() {
        MundoJuego mundo = new MundoJuego();
        Jugador jugador = new Jugador();
        ManadaJugador manada = new ManadaJugador(Especie.JAGUAR, jugador);
        ManadaBot bots = new ManadaBot(Especie.DANTA, 1);
        manada.crearMiembros(zona(1), new Random(1));
        bots.crearMiembros(zona(1), new Random(2));
        mundo.getManadasJugador().add(manada);
        mundo.getManadasBot().add(bots);

        AnimalJugador atacante = (AnimalJugador) manada.getVivos().firstElement();
        AnimalBot objetivo = (AnimalBot) bots.getVivos().firstElement();
        ubicar(atacante, 1, 50, 50);
        ubicar(objetivo, 11, 50, 50);
        atacante.setObjetivo(objetivo);
        ReglaAtaqueJugador regla = new ReglaAtaqueJugador();

        regla.aplicar(mundo, 1_000L, 0.05);
        regla.aplicar(mundo, 1_500L, 0.05);
        assertEquals(8, objetivo.getEnergia());
        regla.aplicar(mundo, 2_000L, 0.05);
        assertEquals(6, objetivo.getEnergia());
    }

    @Test
    void botsStopPatrollingAndShowTheAttackAnimationWhenAPlayerIsInRange() {
        MundoJuego mundo = new MundoJuego();
        Mapa mapa = new Mapa();
        Zona zona = zona(1);
        mapa.agregarZona(zona);
        mapa.agregarCamino(new Camino(1, 1,
                List.of(new Punto(0, 50), new Punto(100, 50))));
        mundo.setMapa(mapa);

        Jugador jugador = new Jugador();
        ManadaJugador manadaJugador = new ManadaJugador(Especie.JAGUAR, jugador);
        ManadaBot manadaBot = new ManadaBot(Especie.DANTA, 1);
        manadaJugador.crearMiembros(zona, new Random(21));
        manadaBot.crearMiembros(zona, new Random(22));
        jugador.setManada(manadaJugador);
        mundo.getManadasJugador().add(manadaJugador);
        mundo.getManadasBot().add(manadaBot);

        AnimalJugador objetivo = (AnimalJugador) manadaJugador.getVivos().firstElement();
        AnimalBot bot = (AnimalBot) manadaBot.getVivos().firstElement();
        for (Animal miembro : manadaBot.getVivos()) {
            ubicar(miembro, miembro.getId(), 90, 50);
        }
        for (Animal miembro : manadaJugador.getVivos()) {
            ubicar(miembro, miembro.getId(), 90, 50);
        }
        ubicar(objetivo, 1, 40, 50);
        ubicar(bot, 11, 10, 50);

        new ReglaAtaqueBot().aplicar(mundo, 1_000L, 0.05);

        assertTrue(!bot.isAtacando());
        assertSame(objetivo, bot.getObjetivo());
        assertTrue(bot.getRuta() != null);

        Punto posicionInicial = new Punto(bot.getPosicion().getX(), bot.getPosicion().getY());
        new ReglaMovimiento(new Random(23)).aplicar(mundo, 1_050L, 0.05);
        assertTrue(bot.getPosicion().distancia(posicionInicial) > 0.0);
        new ReglaAtaqueBot().aplicar(mundo, 1_050L, 0.05);
        assertTrue(!bot.isAtacando());

        new ReglaMovimiento(new Random(23)).aplicar(mundo, 1_100L, 0.05);
        new ReglaAtaqueBot().aplicar(mundo, 1_100L, 0.05);
        assertTrue(!bot.isAtacando());

        new ReglaMovimiento(new Random(23)).aplicar(mundo, 1_150L, 0.05);
        new ReglaAtaqueBot().aplicar(mundo, 1_150L, 0.05);
        assertTrue(bot.isAtacando());
        assertNull(bot.getRuta());
        assertEquals(19, objetivo.getEnergia());

        Punto posicionDuranteAtaque = new Punto(bot.getPosicion().getX(), bot.getPosicion().getY());
        new ReglaMovimiento(new Random(23)).aplicar(mundo, 1_200L, 0.05);
        assertEquals(posicionDuranteAtaque, bot.getPosicion());

        EstadoVisible estado = new ServicioVisibilidad().crearSnapshot(mundo, jugador);
        var vistaBot = estado.animales.stream().filter(animal -> animal.id == bot.getId())
                .findFirst().orElseThrow();
        assertTrue(vistaBot.atacando);
        assertEquals(objetivo.getId(), vistaBot.idObjetivo);
        assertEquals(objetivo.getPosicion().getX(), vistaBot.objetivoX);
        assertEquals(objetivo.getPosicion().getY(), vistaBot.objetivoY);

        ubicar(objetivo, objetivo.getId(), 90, 50);
        new ReglaAtaqueBot().aplicar(mundo, 1_250L, 0.05);
        assertTrue(!bot.isAtacando());
        assertSame(objetivo, bot.getObjetivo());
        assertTrue(bot.getRuta() != null);
    }

    @Test
    void selectedPlayerAnimalMovesToTargetBeforeStartingAttack() {
        MundoJuego mundo = new MundoJuego();
        Mapa mapa = new Mapa();
        mapa.agregarZona(zona(1));
        mapa.agregarCamino(new Camino(1, 1,
                List.of(new Punto(0, 50), new Punto(100, 50))));
        mundo.setMapa(mapa);
        Jugador jugador = new Jugador();
        ManadaJugador manada = new ManadaJugador(Especie.JAGUAR, jugador);
        ManadaBot bots = new ManadaBot(Especie.DANTA, 1);
        manada.crearMiembros(zona(1), new Random(11));
        bots.crearMiembros(zona(1), new Random(12));
        mundo.getManadasJugador().add(manada);
        mundo.getManadasBot().add(bots);

        AnimalJugador atacante = (AnimalJugador) manada.getVivos().firstElement();
        Animal objetivo = bots.getVivos().firstElement();
        ubicar(atacante, 1, 10, 50);
        ubicar(objetivo, 11, 90, 50);
        atacante.setObjetivo(objetivo);
        ReglaAtaqueJugador regla = new ReglaAtaqueJugador();

        regla.aplicar(mundo, 1_000L, 0.05);
        assertTrue(atacante.getRuta() != null);
        assertTrue(!atacante.isAtacando());
        atacante.avanzar(1.3);
        regla.aplicar(mundo, 1_500L, 0.05);

        assertTrue(atacante.isAtacando());
        assertNull(atacante.getRuta());
        assertEquals(8, objetivo.getEnergia());
    }

    @Test
    void coffeeAppliesPackDamageAndUpdatesPlayerCounterOnce() {
        MundoJuego mundo = new MundoJuego();
        Jugador jugador = new Jugador();
        ManadaJugador manada = new ManadaJugador(Especie.JAGUAR, jugador);
        manada.crearMiembros(zona(1), new Random(3));
        mundo.getManadasJugador().add(manada);
        Animal animal = manada.getVivos().firstElement();
        ubicar(animal, animal.getId(), 50, 50);
        TazaCafe taza = new TazaCafe();
        taza.setUbicacion(1, new Punto(50, 50), 1);
        mundo.getTazas().add(taza);

        ReglaTazaCafe regla = new ReglaTazaCafe();
        regla.aplicar(mundo, 1_000L, 0.05);
        regla.aplicar(mundo, 1_050L, 0.05);

        assertEquals(1, jugador.getTazasConsumidas());
        assertEquals(3, manada.getDanio());
    }

    @Test
    void everySecondBotKillRaisesLivingAnimalsEnergy() {
        MundoJuego mundo = new MundoJuego();
        Jugador jugador = new Jugador();
        ManadaJugador manada = new ManadaJugador(Especie.JAGUAR, jugador);
        ManadaBot bots = new ManadaBot(Especie.DANTA, 1);
        manada.crearMiembros(zona(1), new Random(4));
        bots.crearMiembros(zona(1), new Random(5));
        mundo.getManadasJugador().add(manada);
        mundo.getManadasBot().add(bots);
        AnimalJugador atacante = (AnimalJugador) manada.getVivos().firstElement();
        atacante.recibirDanio(5);
        ReglaBonoEnergia regla = new ReglaBonoEnergia();
        Animal[] objetivos = bots.getVivos().toArray(new Animal[0]);

        for (int i = 0; i < 2; i++) {
            objetivos[i].recibirDanio(8);
            assertTrue(atacante.atacar(objetivos[i], 1_000L + i * 1_000L));
            regla.aplicar(mundo, 1_000L + i * 1_000L, 0.05);
        }

        assertEquals(16, atacante.getEnergia());
        assertEquals(2, jugador.getBotsEliminados());
    }

    @Test
    void opponentFlagTouchSetsTheWinningPlayer() {
        MundoJuego mundo = new MundoJuego();
        Jugador atacante = new Jugador();
        Jugador duenoBandera = new Jugador();
        ManadaJugador manadaAtacante = new ManadaJugador(Especie.JAGUAR, atacante);
        manadaAtacante.crearMiembros(zona(1), new Random(6));
        atacante.setManada(manadaAtacante);
        mundo.getManadasJugador().add(manadaAtacante);
        Animal animal = manadaAtacante.getVivos().firstElement();
        ubicar(animal, animal.getId(), 50, 50);
        Bandera bandera = new Bandera(duenoBandera, "roja");
        bandera.setUbicacion(1, new Punto(50, 50), 1);
        mundo.getBanderas().add(bandera);

        new ReglaVictoria().aplicar(mundo, 1_000L, 0.05);

        assertTrue(mundo.hayGanador());
        assertSame(atacante, mundo.getGanador());
    }

    @Test
    void survivingPlayerWinsWhenOpponentLosesAllAnimals() {
        MundoJuego mundo = new MundoJuego();
        Jugador sobreviviente = new Jugador();
        Jugador eliminado = new Jugador();
        ManadaJugador manadaSobreviviente = new ManadaJugador(Especie.JAGUAR, sobreviviente);
        ManadaJugador manadaEliminada = new ManadaJugador(Especie.DANTA, eliminado);
        manadaSobreviviente.crearMiembros(zona(1), new Random(31));
        manadaEliminada.crearMiembros(zona(2), new Random(32));
        mundo.getManadasJugador().add(manadaSobreviviente);
        mundo.getManadasJugador().add(manadaEliminada);
        for (Animal animal : manadaEliminada.getVivos()) {
            animal.recibirDanio(Integer.MAX_VALUE);
        }

        new ReglaVictoria().aplicar(mundo, 1_000L, 0.05);

        assertTrue(mundo.hayGanador());
        assertSame(sobreviviente, mundo.getGanador());
    }

    @Test
    void gameContinuesWhileBothPlayersStillHaveLivingAnimals() {
        MundoJuego mundo = new MundoJuego();
        ManadaJugador primera = new ManadaJugador(Especie.JAGUAR, new Jugador());
        ManadaJugador segunda = new ManadaJugador(Especie.DANTA, new Jugador());
        primera.crearMiembros(zona(1), new Random(33));
        segunda.crearMiembros(zona(2), new Random(34));
        mundo.getManadasJugador().add(primera);
        mundo.getManadasJugador().add(segunda);
        primera.getVivos().firstElement().recibirDanio(Integer.MAX_VALUE);

        new ReglaVictoria().aplicar(mundo, 1_000L, 0.05);

        assertTrue(!mundo.hayGanador());
    }

    @Test
    void generatedMapContainsTwelveConnectedZones() {
        var mapa = MainMapa.crearMapa();

        assertEquals(12, mapa.getZonas().size());
        assertEquals(29, mapa.getCaminos().size());
        assertEquals(6, new BuscadorBFS().buscarRuta(mapa, 1, 12).size());
    }

    @Test
    void playerViewFollowsAnAnimalAcrossARegionExit() {
        Mapa mapa = new Mapa();
        Zona primera = new Zona(1, new Rectangle2D.Double(0, 0, 100, 100));
        Zona segunda = new Zona(2, new Rectangle2D.Double(100, 0, 100, 100));
        mapa.agregarZona(primera);
        mapa.agregarZona(segunda);
        mapa.agregarCamino(new Camino(1, 2,
                List.of(new Punto(50, 50), new Punto(100, 50), new Punto(150, 50))));

        MundoJuego mundo = new MundoJuego();
        mundo.setMapa(mapa);
        Jugador jugador = new Jugador();
        jugador.setZonaVista(1);
        ManadaJugador manada = new ManadaJugador(Especie.JAGUAR, jugador);
        manada.crearMiembros(primera, new Random(9));
        jugador.setManada(manada);
        mundo.getManadasJugador().add(manada);
        Animal animal = manada.getVivos().firstElement();
        animal.setUbicacion(animal.getId(), new Punto(99, 50), 1);
        animal.setRuta(new Ruta(List.of(new Punto(100, 50), new Punto(101, 50))));

        new ReglaMovimiento().aplicar(mundo, 1_000L, 1.0);

        assertEquals(2, animal.getIdZona());
        assertEquals(2, jugador.getZonaVista());
    }

    @Test
    void visibilitySnapshotContainsOnlyTheSelectedCurrentlyOccupiedZone() {
        MundoJuego mundo = new MundoJuego();
        Jugador jugador = new Jugador();
        ManadaJugador propia = new ManadaJugador(Especie.JAGUAR, jugador);
        ManadaBot bots = new ManadaBot(Especie.DANTA, 2);
        propia.crearMiembros(zona(1), new Random(7));
        bots.crearMiembros(zona(2), new Random(8));
        for (Animal animal : bots.getVivos()) {
            animal.setUbicacion(animal.getId(), new Punto(50, 50), 2);
        }
        jugador.setManada(propia);
        jugador.setZonaVista(1);
        mundo.getManadasJugador().add(propia);
        mundo.getManadasBot().add(bots);

        ServicioVisibilidad servicio = new ServicioVisibilidad();
        EstadoVisible snapshot = servicio.crearSnapshot(mundo, jugador);

        assertEquals(1, snapshot.idZona);
        assertEquals(1, snapshot.zonasVisibles.size());
        assertEquals(10, snapshot.animalesVivos);
        assertEquals(10, snapshot.animales.size());
        assertEquals(Map.of(1, 10), snapshot.animalesPorZona);
        assertTrue(snapshot.animales.stream().allMatch(animal -> animal.esMio));
    }

    @Test
    void selectedZoneWithoutOwnAnimalsReturnsOwnAnimalsButNoFogContents() {
        MundoJuego mundo = new MundoJuego();
        Mapa mapa = new Mapa();
        mapa.agregarZona(zona(1));
        mapa.agregarZona(new Zona(2, new Rectangle2D.Double(100, 0, 100, 100)));
        mundo.setMapa(mapa);

        Jugador jugador = new Jugador();
        ManadaJugador propia = new ManadaJugador(Especie.JAGUAR, jugador);
        ManadaBot bots = new ManadaBot(Especie.DANTA, 2);
        propia.crearMiembros(zona(1), new Random(17));
        bots.crearMiembros(mapa.getZona(2), new Random(18));
        jugador.setManada(propia);
        jugador.setZonaVista(2);
        mundo.getManadasJugador().add(propia);
        mundo.getManadasBot().add(bots);

        EstadoVisible snapshot = new ServicioVisibilidad().crearSnapshot(mundo, jugador);

        assertEquals(2, snapshot.idZona);
        assertEquals(List.of(1), snapshot.zonasVisibles);
        assertEquals(Map.of(1, 10), snapshot.animalesPorZona);
        assertEquals(10, snapshot.animales.size());
        assertTrue(snapshot.animales.stream().allMatch(animal -> animal.esMio));
        assertTrue(snapshot.tazas.isEmpty());
        assertNull(snapshot.bandera);
    }

    private static Zona zona(int id) {
        return new Zona(id, new Rectangle2D.Double(0, 0, 100, 100));
    }

    private static void ubicar(Animal animal, int id, double x, double y) {
        animal.setUbicacion(id, new Punto(x, y), 1);
    }
}
