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

    @Override
    public void run() {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public void tick(long ahora) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public void detener() {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }
}
