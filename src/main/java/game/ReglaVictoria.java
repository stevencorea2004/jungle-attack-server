package game;

import model.Animal;
import model.Bandera;
import model.ManadaJugador;

public class ReglaVictoria implements IReglaJuego {
    @Override
    public void aplicar(MundoJuego mundo, long ahora, double deltaSeg) {
        if (mundo == null || mundo.hayGanador()) {
            return;
        }
        if (mundo.getManadasJugador().size() == 2) {
            ManadaJugador eliminada = null;
            ManadaJugador superviviente = null;
            for (ManadaJugador manada : mundo.getManadasJugador()) {
                if (manada == null) {
                    continue;
                }
                if (manada.estaEliminada()) {
                    eliminada = manada;
                } else {
                    superviviente = manada;
                }
            }
            if (eliminada != null && superviviente != null
                    && superviviente.getDueno() != null) {
                mundo.terminar(superviviente.getDueno());
                return;
            }
        }
        for (Bandera bandera : mundo.getBanderas()) {
            if (bandera == null || bandera.getDueno() == null) {
                continue;
            }
            for (Animal animal : mundo.getAnimalesVivos()) {
                if (animal.getManada() instanceof ManadaJugador
                        && bandera.esTocadaPor(animal)) {
                    ManadaJugador manada = (ManadaJugador) animal.getManada();
                    mundo.terminar(manada.getDueno());
                    return;
                }
            }
        }
    }
}
