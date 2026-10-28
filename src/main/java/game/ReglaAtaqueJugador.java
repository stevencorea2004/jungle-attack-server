package game;

import model.Animal;
import model.AnimalJugador;
import model.ManadaJugador;
import mapa.Ruta;
import util.Constantes;

public class ReglaAtaqueJugador implements IReglaJuego {
    @Override
    public void aplicar(MundoJuego mundo, long ahora, double deltaSeg) {
        if (mundo == null) {
            return;
        }
        for (ManadaJugador manada : mundo.getManadasJugador()) {
            if (manada == null) {
                continue;
            }
            for (Animal atacante : manada.getVivos()) {
                if (!(atacante instanceof AnimalJugador)) {
                    continue;
                }
                AnimalJugador jugador = (AnimalJugador) atacante;
                Animal objetivo = jugador.getObjetivo();
                if (objetivo == null) {
                    continue;
                }
                if (!objetivo.estaVivo() || objetivo.getManada() == manada) {
                    jugador.limpiarObjetivo();
                    continue;
                }
                if (!jugador.estaEnRadio(objetivo, Constantes.RADIO_ENCUENTRO)) {
                    jugador.setAtacando(false);
                    Ruta ruta = jugador.getRuta();
                    if (ruta == null || ruta.terminada()) {
                        ruta = Ruta.crear(mundo.getMapa(), null,
                                jugador.getPosicion(), objetivo.getPosicion());
                        if (!ruta.esValida()) {
                            jugador.limpiarObjetivo();
                            jugador.setRuta(null);
                            continue;
                        }
                        jugador.setRuta(ruta);
                    }
                    continue;
                }
                jugador.setRuta(null);
                jugador.setAtacando(true);
                jugador.atacar(objetivo, ahora);
            }
        }
    }
}
