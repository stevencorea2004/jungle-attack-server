package util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class UtilTest {
    @Test
    void mainIsCallableAndServerConstantsAreConfigured() {
        assertDoesNotThrow(() -> MainUtil.main(new String[0]));

        assertEquals(5000, Constantes.PUERTO);
        assertEquals("UTF-8", Constantes.CHARSET);
        assertEquals(10, Constantes.CANTIDAD_TAZAS);
        assertEquals(12, Constantes.CANTIDAD_ZONAS);
        assertEquals(1000L, Constantes.COOLDOWN_ATAQUE_MS);
    }

    @Test
    void observableRegistersNotifiesOnceAndStopsAfterRemoval() {
        Observable observable = new Observable() { };
        Object event = new Object();
        AtomicInteger notifications = new AtomicInteger();
        IObserver observer = (source, subject) -> {
            assertSame(event, source);
            assertSame(observable, subject);
            notifications.incrementAndGet();
        };

        observable.addObserver(observer);
        observable.addObserver(observer);
        observable.addObserver(null);
        observable.notifyObservers(event);

        assertEquals(1, notifications.get());

        observable.removeObserver(observer);
        observable.removeObserver(null);
        observable.notifyObservers(event);
        assertEquals(1, notifications.get());
    }
}
