package red;

import java.util.Vector;
import model.Punto;

public class EstadoVisible {
    public int idZona;
    public Vector<Integer> zonasVisibles;
    public Vector<VistaAnimal> animales;
    public Vector<Punto> tazas;
    public Punto bandera;
    public String colorBandera;
    public Vector<Object> eventos;
    public int animalesVivos;
    public int danio;
    public int tazasConsumidas;
    public int botsEliminados;
}
