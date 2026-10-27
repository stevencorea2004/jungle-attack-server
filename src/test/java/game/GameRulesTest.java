package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Rectangle2D;
import java.util.Random;
import mapa.BuscadorBFS;
import mapa.MainMapa;
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
    void generatedMapContainsTwelveConnectedZones() {
        var mapa = MainMapa.crearMapa();

        assertEquals(12, mapa.getZonas().size());
        assertEquals(17, mapa.getCaminos().size());
        assertEquals(6, new BuscadorBFS().buscarRuta(mapa, 1, 12).size());
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
        assertTrue(snapshot.animales.stream().allMatch(animal -> animal.esMio));
    }

    private static Zona zona(int id) {
        return new Zona(id, new Rectangle2D.Double(0, 0, 100, 100));
    }

    private static void ubicar(Animal animal, int id, double x, double y) {
        animal.setUbicacion(id, new Punto(x, y), 1);
    }
}
