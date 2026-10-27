package game;

import java.util.HashSet;
import java.util.Set;
import model.Animal;
import model.AnimalJugador;
import model.ManadaBot;
import model.ManadaJugador;

public class ReglaBonoEnergia implements IReglaJuego {
    private final Set<Animal> botsProcesados = new HashSet<>();

    @Override
    public void aplicar(MundoJuego mundo, long ahora, double deltaSeg) {
        if (mundo == null) {
            return;
        }
        for (ManadaBot bots : mundo.getManadasBot()) {
            if (bots == null) {
                continue;
            }
            for (Animal bot : bots.getMiembros()) {
                if (bot == null || bot.estaVivo() || !botsProcesados.add(bot)) {
                    continue;
                }
                Animal atacante = bot.getUltimoAtacante();
                if (!(atacante instanceof AnimalJugador)) {
                    continue;
                }
                if (!(atacante.getManada() instanceof ManadaJugador)) {
                    continue;
                }
                ManadaJugador manada = (ManadaJugador) atacante.getManada();
                Jugador dueno = manada.getDueno();
                if (dueno != null && dueno.registrarBotEliminado()) {
                    manada.aplicarBonoEnergia(util.Constantes.BONO_ENERGIA);
                }
            }
        }
    }
}
