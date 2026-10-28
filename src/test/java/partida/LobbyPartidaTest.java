package partida;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import game.Jugador;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import model.Especie;
import org.junit.jupiter.api.Test;

class LobbyPartidaTest {
    private static final Pattern CODIGO_VALIDO = Pattern.compile("[A-Z0-9]{4}");

    @Test
    void creaCodigosAlfanumericosUnicosParaPartidasActivas() {
        AdministradorPartidas administrador = new AdministradorPartidas();
        Set<String> codigos = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            Jugador anfitrion = jugador("Jugador" + i);
            String codigo = administrador.crearPartida(anfitrion).getCodigo();
            assertTrue(CODIGO_VALIDO.matcher(codigo).matches());
            assertTrue(codigos.add(codigo));
        }
        assertEquals(100, administrador.getCantidadActivas());
    }

    @Test
    void permiteUnirseSinDistinguirMayusculasYRechazaPartidaLlena() {
        AdministradorPartidas administrador = new AdministradorPartidas();
        Jugador anfitrion = jugador("Ana");
        Partida partida = administrador.crearPartida(anfitrion);

        assertThrows(IllegalArgumentException.class,
                () -> administrador.unirse("ZZZZ", jugador("Invalido")));

        Jugador invitado = jugador("Luis");
        Partida encontrada = administrador.unirse(partida.getCodigo().toLowerCase(), invitado);
        assertEquals(partida, encontrada);
        assertEquals(EstadoPartida.ELIGIENDO_ANIMALES, partida.getEstado());
        assertTrue(partida.estaLlena());

        assertThrows(IllegalStateException.class,
                () -> administrador.unirse(partida.getCodigo(), jugador("Tercero")));
        assertNotEquals(anfitrion, invitado);
    }

    @Test
    void seleccionaEspeciesDistintasYDejaLasRestantesParaLosBots() {
        Partida partida = new Partida("A7K2");
        Jugador anfitrion = jugador("Ana");
        Jugador invitado = jugador("Luis");
        partida.agregarJugador(anfitrion);
        partida.agregarJugador(invitado);

        partida.elegirEspecie(anfitrion, Especie.JAGUAR);
        assertThrows(IllegalArgumentException.class,
                () -> partida.elegirEspecie(invitado, Especie.JAGUAR));
        partida.elegirEspecie(invitado, Especie.DANTA);

        assertTrue(partida.todosEligieron());
        assertEquals(Especie.JAGUAR, anfitrion.getEspecie());
        assertEquals(Especie.DANTA, invitado.getEspecie());
        assertEquals(5, partida.getEspeciesDisponibles().size());
        assertFalse(partida.getEspeciesDisponibles().contains(Especie.JAGUAR));
        assertFalse(partida.getEspeciesDisponibles().contains(Especie.DANTA));
        assertNotNull(partida.getRival(anfitrion));
    }

    @Test
    void deselectionReleasesSpeciesWhileMatchIsStillSelecting() {
        Partida partida = new Partida("B8K2");
        Jugador anfitrion = jugador("Ana");
        Jugador invitado = jugador("Luis");
        partida.agregarJugador(anfitrion);
        partida.agregarJugador(invitado);
        partida.elegirEspecie(anfitrion, Especie.JAGUAR);

        partida.deseleccionarEspecie(anfitrion);

        assertEquals(null, partida.getEleccion(anfitrion));
        assertEquals(null, anfitrion.getEspecie());
        assertTrue(partida.getEspeciesDisponibles().contains(Especie.JAGUAR));
        partida.elegirEspecie(invitado, Especie.JAGUAR);
        assertEquals(Especie.JAGUAR, partida.getEleccion(invitado));
    }

    private static Jugador jugador(String nickname) {
        Jugador jugador = new Jugador();
        jugador.setNickname(nickname);
        return jugador;
    }
}
