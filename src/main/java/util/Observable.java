package util;

import java.util.Vector;

public abstract class Observable {
    protected final Vector<IObserver> observers = new Vector<>();

    public void addObserver(IObserver ob) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public void removeObserver(IObserver ob) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public void notifyObservers(Object source) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }
}
