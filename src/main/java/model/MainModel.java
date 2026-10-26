package model;

import java.util.Random;
import util.Constantes;

public final class MainModel {
    private MainModel() {
    }

    public static void main(String[] args) {
        verificarPunto();
        verificarAnimal();
        verificarManadaYtaza();
        System.out.println("MainModel OK");
    }

    private static void verificarPunto() {
        Punto a = new Punto(0, 0);
        Punto b = new Punto(3, 4);
        if (Math.abs(a.distancia(b) - 5.0) > 0.0001) {
            throw new AssertionError("Distancia incorrecta");
        }
        Punto c = a.moverHacia(b, 2.0);
        if (Math.abs(c.distancia(a) - 2.0) > 0.0001) {
            throw new AssertionError("Movimiento incorrecto");
        }
    }

    private static void verificarAnimal() {
        AnimalJugador jugador = new AnimalJugador();
        jugador.id = 1;
        jugador.posicion = new Punto(0, 0);
        jugador.energia = Constantes.ENERGIA_MAX_JUGADOR;

        AnimalBot bot = new AnimalBot();
        bot.id = 2;
        bot.posicion = new Punto(0, 0);
        bot.energia = Constantes.ENERGIA_BOT;

        ManadaJugador manadaJ = new ManadaJugador(Especie.JAGUAR, null);
        manadaJ.miembros.add(jugador);
        jugador.manada = manadaJ;
        manadaJ.danio = Constantes.DANIO_JUGADOR;

        ManadaBot manadaB = new ManadaBot(Especie.DANTA, 0);
        manadaB.miembros.add(bot);
        bot.manada = manadaB;
        manadaB.danio = Constantes.DANIO_BOT;

        if (!jugador.estaVivo()) {
            throw new AssertionError("Jugador vivo incorrecto");
        }
        jugador.recibirDanio(Constantes.ENERGIA_MAX_JUGADOR);
        if (jugador.estaVivo()) {
            throw new AssertionError("Jugador no murio");
        }

        AnimalJugador vivo = new AnimalJugador();
        vivo.id = 3;
        vivo.posicion = new Punto(0, 0);
        vivo.energia = Constantes.ENERGIA_MAX_JUGADOR;
        vivo.manada = manadaJ;
        manadaJ.miembros.add(vivo);

        long ahora = System.currentTimeMillis();
        vivo.ultimoAtaque = ahora - Constantes.COOLDOWN_ATAQUE_MS - 1L;
        bot.ultimoAtaque = ahora - Constantes.COOLDOWN_ATAQUE_MS - 1L;
        if (!vivo.puedeAtacar(ahora)) {
            throw new AssertionError("Cooldown no habilita ataque");
        }
    }

    private static void verificarManadaYtaza() {
        ManadaJugador manada = new ManadaJugador(Especie.PIZOTE, null);
        AnimalJugador a1 = new AnimalJugador();
        a1.id = 1;
        a1.energia = Constantes.ENERGIA_MAX_JUGADOR;
        a1.manada = manada;
        manada.miembros.add(a1);

        TazaCafe taza = new TazaCafe();
        if (taza.consumir(manada)) {
            if (manada.danio != Constantes.DANIO_JUGADOR + Constantes.BONO_DANIO_TAZA) {
                throw new AssertionError("Bono de taza incorrecto");
            }
        } else {
            throw new AssertionError("Taza no consumida");
        }
        if (taza.consumir(manada)) {
            throw new AssertionError("Taza consumida dos veces");
        }
    }
}
