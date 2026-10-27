package partida;

import game.CicloJuego;
import game.InicializadorMundo;
import game.Jugador;
import game.MundoJuego;
import game.ServicioVisibilidad;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;
import mapa.Camino;
import mapa.MainMapa;
import mapa.Zona;
import model.Animal;
import model.Especie;
import model.ManadaBot;
import model.ManadaJugador;
import red.DatosInicio;
import red.ManejadorCliente;
import red.EstadoVisible;
import red.FabricaMensajes;
import red.ResumenPartida;
import red.TipoMensaje;
import red.VistaAnimal;
import red.VistaCamino;
import red.VistaMapa;
import red.VistaZona;
import util.IObserver;
import util.Observable;

public class Partida implements IObserver {
    private final String codigo;
    private volatile EstadoPartida estado;
    private final Vector<Jugador> jugadores;
    private final SelectorEspecies selector;
    private MundoJuego mundo;
    private CicloJuego ciclo;
    private long inicio;
    private final ServicioVisibilidad visibilidad = new ServicioVisibilidad();
    private Runnable alTerminar = () -> { };

    public Partida(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código de partida es obligatorio");
        }
        this.codigo = GeneradorCodigo.normalizar(codigo);
        this.estado = EstadoPartida.ESPERANDO_JUGADOR;
        this.jugadores = new Vector<>();
        this.selector = new SelectorEspecies();
    }

    public synchronized void agregarJugador(Jugador jugador) {
        if (jugador == null) {
            throw new IllegalArgumentException("El jugador es obligatorio");
        }
        if (estado != EstadoPartida.ESPERANDO_JUGADOR || estaLlena()) {
            throw new IllegalStateException("La partida ya está llena o no acepta jugadores");
        }
        if (jugadores.contains(jugador)) {
            throw new IllegalArgumentException("El jugador ya pertenece a esta partida");
        }
        if (jugador.getPartida() != null && jugador.getPartida() != this) {
            throw new IllegalArgumentException("El jugador ya pertenece a otra partida");
        }
        jugadores.add(jugador);
        jugador.setPartida(this);
        if (estaLlena()) {
            estado = EstadoPartida.ELIGIENDO_ANIMALES;
        }
    }

    public synchronized boolean estaLlena() {
        return jugadores.size() >= 2;
    }

    public synchronized void elegirEspecie(Jugador jugador, Especie especie) {
        if (estado != EstadoPartida.ELIGIENDO_ANIMALES) {
            throw new IllegalStateException("La partida no está en fase de selección");
        }
        if (!jugadores.contains(jugador)) {
            throw new IllegalArgumentException("El jugador no pertenece a esta partida");
        }
        if (!selector.elegir(jugador, especie)) {
            throw new IllegalArgumentException("La especie no está disponible para este jugador");
        }
        jugador.setEspecie(especie);
    }

    public synchronized Jugador getRival(Jugador jugador) {
        if (!jugadores.contains(jugador)) {
            return null;
        }
        for (Jugador participante : jugadores) {
            if (participante != jugador) {
                return participante;
            }
        }
        return null;
    }

    public synchronized Vector<Jugador> getJugadores() {
        return new Vector<>(jugadores);
    }

    public synchronized Vector<Especie> getEspeciesDisponibles() {
        return selector.getDisponibles();
    }

    public synchronized boolean todosEligieron() {
        return selector.todosEligieron();
    }

    public synchronized Especie getEleccion(Jugador jugador) {
        return selector.getEleccion(jugador);
    }

    public String getCodigo() {
        return codigo;
    }

    public void setAlTerminar(Runnable alTerminar) {
        this.alTerminar = alTerminar == null ? () -> { } : alTerminar;
    }

    public EstadoPartida getEstado() {
        return estado;
    }

    public synchronized void iniciar() {
        if (estado == EstadoPartida.EN_JUEGO) {
            return;
        }
        if (estado != EstadoPartida.ELIGIENDO_ANIMALES || !todosEligieron()) {
            throw new IllegalStateException("Ambos jugadores deben elegir una especie antes de iniciar");
        }
        mundo = new MundoJuego();
        mundo.setMapa(MainMapa.crearMapa());
        mundo.addObserver(this);
        InicializadorMundo inicializador = new InicializadorMundo(new Random());
        Vector<Integer> zonas = inicializador.elegirZonasIniciales(
                mundo.getMapa(), getJugadores().size() + selector.especiesBot().size());

        for (int i = 0; i < jugadores.size(); i++) {
            Jugador jugador = jugadores.get(i);
            ManadaJugador manada = new ManadaJugador(jugador.getEspecie(), jugador);
            manada.crearMiembros(mundo.getMapa().getZona(zonas.get(i)), new Random(), mundo.getMapa());
            jugador.setManada(manada);
            jugador.setZonaVista(zonas.get(i));
            mundo.getManadasJugador().add(manada);
        }
        Vector<Especie> especiesBot = selector.especiesBot();
        for (int i = 0; i < especiesBot.size(); i++) {
            ManadaBot manada = new ManadaBot(especiesBot.get(i), zonas.get(jugadores.size() + i));
            manada.crearMiembros(mundo.getMapa().getZona(zonas.get(jugadores.size() + i)),
                    new Random(), mundo.getMapa());
            mundo.getManadasBot().add(manada);
        }
        inicializador.colocarTazas(mundo);
        inicializador.colocarBanderas(mundo);

        inicio = System.currentTimeMillis();
        estado = EstadoPartida.EN_JUEGO;
        enviarInicio();
        enviarEstado();
        ciclo = new CicloJuego(mundo);
        Thread hilo = new Thread(ciclo, "jungle-attack-" + codigo);
        hilo.setDaemon(true);
        hilo.start();
    }

    public void enviarEstado() {
        for (Jugador jugador : getJugadores()) {
            enviarEstado(jugador);
        }
    }

    public void enviarEstado(Jugador jugador) {
        if (jugador == null || jugador.getConexion() == null || mundo == null
                || estado != EstadoPartida.EN_JUEGO) {
            return;
        }
        EstadoVisible snapshot = visibilidad.crearSnapshot(mundo, jugador);
        jugador.getConexion().enviar(FabricaMensajes.crear(TipoMensaje.STATE_UPDATE, snapshot));
    }

    public synchronized void terminar(Jugador ganador) {
        if (estado == EstadoPartida.TERMINADA) {
            return;
        }
        if (ganador != null && !jugadores.contains(ganador)) {
            throw new IllegalArgumentException("El ganador debe pertenecer a la partida");
        }
        estado = EstadoPartida.TERMINADA;
        if (ciclo != null) {
            ciclo.detener();
        }
        if (mundo != null && !mundo.hayGanador() && ganador != null) {
            mundo.terminar(ganador);
        }
        ResumenPartida resumen = crearResumen(ganador);
        for (Jugador jugador : getJugadores()) {
            ManejadorCliente conexion = jugador.getConexion();
            if (conexion != null) {
                conexion.enviar(FabricaMensajes.crear(TipoMensaje.GAME_OVER, resumen));
                conexion.setJugador(null);
            }
        }
        alTerminar.run();
    }

    public synchronized void manejarDesconexion(Jugador jugador) {
        if (jugador == null || !jugadores.contains(jugador) || estado == EstadoPartida.TERMINADA) {
            return;
        }
        jugador.setConexion(null);
        Jugador rival = getRival(jugador);
        if (estado == EstadoPartida.EN_JUEGO && rival != null) {
            terminar(rival);
        } else {
            terminar(null);
        }
    }

    @Override
    public void update(Object source, Observable ob) {
        if (estado != EstadoPartida.EN_JUEGO || mundo == null) {
            return;
        }
        if (mundo.hayGanador()) {
            terminar(mundo.getGanador());
        } else {
            enviarEstado();
        }
    }

    public MundoJuego getMundo() {
        return mundo;
    }

    private void enviarInicio() {
        VistaMapa vistaMapa = new VistaMapa();
        vistaMapa.zonas = new Vector<>();
        for (Zona zona : mundo.getMapa().getZonas()) {
            VistaZona vistaZona = new VistaZona();
            vistaZona.id = zona.getId();
            vistaZona.x = zona.getLimites().getX();
            vistaZona.y = zona.getLimites().getY();
            vistaZona.ancho = zona.getLimites().getWidth();
            vistaZona.alto = zona.getLimites().getHeight();
            vistaMapa.zonas.add(vistaZona);
        }
        vistaMapa.caminos = new Vector<>();
        for (Camino camino : mundo.getMapa().getCaminos()) {
            VistaCamino vistaCamino = new VistaCamino();
            vistaCamino.zonaA = camino.getIdZonaA();
            vistaCamino.zonaB = camino.getIdZonaB();
            vistaCamino.puntos = camino.getPuntos();
            vistaMapa.caminos.add(vistaCamino);
        }
        for (Jugador jugador : getJugadores()) {
            if (jugador.getConexion() == null) {
                continue;
            }
            DatosInicio datos = new DatosInicio();
            datos.mapa = vistaMapa;
            datos.zonaPropia = jugador.getZonaVista();
            datos.manada = new Vector<>();
            for (Animal animal : jugador.getManada().getVivos()) {
                datos.manada.add(crearVista(animal, true));
            }
            jugador.getConexion().enviar(FabricaMensajes.crear(TipoMensaje.GAME_STARTED, datos));
        }
    }

    private ResumenPartida crearResumen(Jugador ganador) {
        ResumenPartida resumen = new ResumenPartida();
        resumen.ganador = ganador == null ? "" : ganador.getNickname();
        resumen.botsEliminados = new HashMap<>();
        resumen.animalesPerdidos = new HashMap<>();
        resumen.tazas = new HashMap<>();
        for (Jugador jugador : getJugadores()) {
            String nombre = jugador.getNickname();
            resumen.botsEliminados.put(nombre, jugador.getBotsEliminados());
            resumen.tazas.put(nombre, jugador.getTazasConsumidas());
            int vivos = jugador.getManada() == null ? 0 : jugador.getManada().getVivos().size();
            resumen.animalesPerdidos.put(nombre,
                    jugador.getManada() == null ? 0 : jugador.getManada().getMiembros().size() - vivos);
        }
        resumen.duracionSeg = inicio == 0 ? 0
                : Math.max(0, (System.currentTimeMillis() - inicio) / 1000);
        return resumen;
    }

    private static VistaAnimal crearVista(Animal animal, boolean propio) {
        VistaAnimal vista = new VistaAnimal();
        vista.id = animal.getId();
        vista.especie = animal.getEspecie();
        vista.esBot = animal.esBot();
        vista.esMio = propio;
        vista.x = animal.getPosicion().getX();
        vista.y = animal.getPosicion().getY();
        vista.energia = animal.getEnergia();
        return vista;
    }
}
