package red;

import java.util.Vector;
import java.util.Map;
import model.Punto;

public class EstadoVisible {
    public int idZona;
    public Vector<Integer> zonasVisibles;
    public Vector<VistaAnimal> animales;
    public Map<Integer, Integer> animalesPorZona;
    public Vector<Punto> tazas;
    public Punto bandera;
    public String colorBandera;
    public Vector<Object> eventos;
    public int animalesVivos;
    public int danio;
    public int tazasConsumidas;
    public int botsEliminados;
}
