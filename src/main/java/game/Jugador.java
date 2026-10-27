package game;

import model.Bandera;
import model.Animal;
import model.Especie;
import model.ManadaJugador;
import partida.Partida;
import red.ManejadorCliente;
import util.Constantes;

public class Jugador {
    private String nickname;
    private ManejadorCliente conexion;
    private Especie especie;
    private ManadaJugador manada;
    private Bandera bandera;
    private int zonaVista;
    private int tazasConsumidas;
    private int botsEliminados;
    private int animalesPerdidos;
    private Partida partida;

    public boolean registrarBotEliminado() {
        botsEliminados++;
        return botsEliminados % Constantes.BOTS_POR_BONO == 0;
    }

    public void registrarTaza() {
        tazasConsumidas++;
    }

    public boolean puedeVer(int idZona) {
        if (manada == null) {
            return false;
        }
        for (Animal animal : manada.getVivos()) {
            if (animal.getIdZona() == idZona) {
                return true;
            }
        }
        return false;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setZonaVista(int zonaVista) {
        this.zonaVista = zonaVista;
    }

    public int getZonaVista() {
        return zonaVista;
    }

    public ManadaJugador getManada() {
        return manada;
    }

    public void setManada(ManadaJugador manada) {
        this.manada = manada;
    }

    public Especie getEspecie() {
        return especie;
    }

    public void setEspecie(Especie especie) {
        this.especie = especie;
    }

    public Bandera getBandera() {
        return bandera;
    }

    public void setBandera(Bandera bandera) {
        this.bandera = bandera;
    }

    public int getTazasConsumidas() {
        return tazasConsumidas;
    }

    public int getBotsEliminados() {
        return botsEliminados;
    }

    public int getAnimalesPerdidos() {
        return animalesPerdidos;
    }

    public Partida getPartida() {
        return partida;
    }

    public void setPartida(Partida partida) {
        this.partida = partida;
    }

    public ManejadorCliente getConexion() {
        return conexion;
    }

    public void setConexion(ManejadorCliente conexion) {
        this.conexion = conexion;
    }
}
