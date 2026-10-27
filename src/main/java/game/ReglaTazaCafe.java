package game;

import model.Animal;
import model.Bandera;
import model.ManadaJugador;
import model.TazaCafe;
import util.Constantes;

public class ReglaTazaCafe implements IReglaJuego {
    @Override
    public void aplicar(MundoJuego mundo, long ahora, double deltaSeg) {
        if (mundo == null) {
            return;
        }
        for (TazaCafe taza : mundo.getTazas()) {
            if (taza == null || taza.isConsumida()) {
                continue;
            }
            for (ManadaJugador manada : mundo.getManadasJugador()) {
                if (manada == null) {
                    continue;
                }
                boolean recogida = false;
                for (Animal animal : manada.getVivos()) {
                    if (animal.estaEnRadio(taza, Constantes.RADIO_TAZA)) {
                        Jugador dueno = manada.getDueno();
                        if (taza.consumir(manada) && dueno != null) {
                            dueno.registrarTaza();
                        }
                        recogida = true;
                        break;
                    }
                }
                if (recogida) {
                    break;
                }
            }
        }
    }
}
