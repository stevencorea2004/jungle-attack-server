package game;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import mapa.Camino;
import mapa.Mapa;
import mapa.Zona;
import model.Animal;
import model.Bandera;
import model.ManadaJugador;
import model.Punto;
import model.TazaCafe;
import util.Constantes;

public class InicializadorMundo {
    private final Random random;

    public InicializadorMundo() {
        this(new Random());
    }

    public InicializadorMundo(Random random) {
        if (random == null) {
            throw new IllegalArgumentException("El generador aleatorio es obligatorio");
        }
        this.random = random;
    }

    public Vector<Integer> elegirZonasIniciales(Mapa mapa, int cantidad) {
        if (mapa == null) {
            throw new IllegalArgumentException("El mapa es obligatorio");
        }
        Vector<Zona> zonas = mapa.getZonas();
        if (cantidad < 0 || cantidad > zonas.size()) {
            throw new IllegalArgumentException("La cantidad debe estar entre 0 y el número de zonas");
        }
        Collections.shuffle(zonas, random);
        Vector<Integer> elegidas = new Vector<>();
        for (int i = 0; i < cantidad; i++) {
            elegidas.add(zonas.get(i).getId());
        }
        return elegidas;
    }

    public void colocarTazas(MundoJuego mundo) {
        if (mundo == null || mundo.getMapa() == null) {
            throw new IllegalArgumentException("El mundo debe tener un mapa");
        }
        if (!mundo.getTazas().isEmpty()) {
            throw new IllegalStateException("Las tazas ya fueron colocadas");
        }

        Vector<Camino> caminos = new Vector<>();
        for (Camino camino : mundo.getMapa().getCaminos()) {
            if (camino != null && camino.getPuntos() != null && !camino.getPuntos().isEmpty()) {
                caminos.add(camino);
            }
        }
        if (caminos.isEmpty()) {
            throw new IllegalStateException("El mapa necesita caminos con puntos para colocar las tazas");
        }

        Vector<TazaCafe> tazas = new Vector<>();
        for (int i = 0; i < Constantes.CANTIDAD_TAZAS; i++) {
            Camino camino = caminos.get(random.nextInt(caminos.size()));
            Punto posicion = camino.puntoAleatorio(random);
            TazaCafe taza = new TazaCafe();
            Zona zona = mundo.getMapa().zonaEn(posicion);
            taza.setUbicacion(i, posicion, zona == null ? 0 : zona.getId());
            tazas.add(taza);
        }
        mundo.getTazas().addAll(tazas);
    }

    public void colocarBanderas(MundoJuego mundo) {
        if (mundo == null || mundo.getMapa() == null) {
            throw new IllegalArgumentException("El mundo debe tener un mapa");
        }
        if (!mundo.getBanderas().isEmpty()) {
            throw new IllegalStateException("Las banderas ya fueron colocadas");
        }
        List<ManadaJugador> manadas = mundo.getManadasJugador();
        if (manadas.size() != 2) {
            throw new IllegalStateException("La partida debe tener exactamente dos manadas de jugador");
        }

        Vector<Bandera> banderas = new Vector<>();
        Vector<Jugador> duenos = new Vector<>();
        for (int i = 0; i < manadas.size(); i++) {
            ManadaJugador manada = manadas.get(i);
            Jugador dueno = manada == null ? null : manada.getDueno();
            if (dueno == null || dueno.getBandera() != null || manada.getVivos().isEmpty()) {
                throw new IllegalStateException("Cada manada debe tener un dueño y animales vivos sin bandera previa");
            }
            Animal animalInicial = manada.getVivos().firstElement();
            int idZona = animalInicial.getIdZona();
            Zona zona = mundo.getMapa().getZona(idZona);
            if (zona == null || dueno.getManada() != manada) {
                throw new IllegalStateException("Cada dueño debe tener una manada ubicada en una zona válida");
            }
            for (ManadaJugador otra : manadas) {
                if (otra != manada && otra != null && !otra.getVivos().isEmpty()
                        && otra.getVivos().firstElement().getIdZona() == idZona) {
                    throw new IllegalStateException("Las manadas de jugadores deben iniciar en zonas distintas");
                }
            }

            Bandera bandera = new Bandera(dueno, i == 0 ? "azul" : "rojo");
            Punto posicion = zona.puntoAleatorio(random);
            Punto conexion = mundo.getMapa().puntoMasCercanoEnZona(idZona, posicion);
            mundo.getMapa().agregarCaminoLocal(idZona, Arrays.asList(conexion, posicion));
            bandera.setUbicacion(i, posicion, idZona);
            banderas.add(bandera);
            duenos.add(dueno);
        }
        mundo.getBanderas().addAll(banderas);
        for (int i = 0; i < duenos.size(); i++) {
            duenos.get(i).setBandera(banderas.get(i));
        }
    }
}
