package game;

import java.time.Clock;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

public class CicloJuego implements Runnable {
    private MundoJuego mundo;
    private Vector<IReglaJuego> reglas;
    private Clock reloj;
    private long ultimoTick;
    private AtomicBoolean activo;

    public CicloJuego(MundoJuego mundo) {
        this.mundo = mundo;
        this.reglas = new Vector<>();
        this.reglas.add(new ReglaMovimiento());
        this.reglas.add(new ReglaAtaqueBot());
        this.reglas.add(new ReglaAtaqueJugador());
        this.reglas.add(new ReglaBonoEnergia());
        this.reglas.add(new ReglaTazaCafe());
        this.reglas.add(new ReglaVictoria());
        this.reloj = Clock.systemDefaultZone();
        this.ultimoTick = reloj.millis();
        this.activo = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        activo.set(true);
        while (activo.get()) {
            long ahora = reloj.millis();
            tick(ahora);
            try {
                Thread.sleep(util.Constantes.TICK_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void tick(long ahora) {
        if (mundo == null || mundo.hayGanador()) {
            return;
        }
        double deltaSeg = (ahora - ultimoTick) / 1000.0;
        ultimoTick = ahora;
        for (IReglaJuego regla : reglas) {
            regla.aplicar(mundo, ahora, deltaSeg);
        }
        mundo.notifyObservers(mundo);
    }

    public void detener() {
        activo.set(false);
    }
}
