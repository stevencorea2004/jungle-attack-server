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
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public void registrarTaza() {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public boolean puedeVer(int idZona) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public String getNickname() {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }
}
