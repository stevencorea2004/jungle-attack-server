package util;

import java.util.Vector;

public abstract class Observable {
    protected final Vector<IObserver> observers = new Vector<>();

    public void addObserver(IObserver ob) {
        if (ob == null || observers.contains(ob)) {
            return;
        }
        observers.add(ob);
    }

    public void removeObserver(IObserver ob) {
        if (ob == null) {
            return;
        }
        observers.remove(ob);
    }

    public void notifyObservers(Object source) {
        for (IObserver ob : new Vector<>(observers)) {
            if (ob != null) {
                ob.update(source, this);
            }
        }
    }
}
