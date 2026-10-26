package game;

import model.Bandera;
import model.Especie;
import model.ManadaJugador;
import red.ManejadorCliente;

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

    public boolean registrarBotEliminado() {
        botsEliminados++;
        return true;
    }

    public void registrarTaza() {
        tazasConsumidas++;
    }

    public boolean puedeVer(int idZona) {
        return this.zonaVista == idZona || this.zonaVista == 0;
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
}
