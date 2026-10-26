package model;

import java.util.Objects;

public class Punto {
    private double x;
    private double y;

    public Punto() {
        this(0, 0);
    }

    public Punto(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double distancia(Punto otro) {
        if (otro == null) {
            return Double.MAX_VALUE;
        }
        return Math.hypot(x - otro.x, y - otro.y);
    }

    public Punto moverHacia(Punto destino, double maxDistancia) {
        if (destino == null) {
            return this;
        }
        double dx = destino.x - this.x;
        double dy = destino.y - this.y;
        double distancia = Math.hypot(dx, dy);
        if (distancia <= 0.000001 || maxDistancia <= 0) {
            return this;
        }
        double factor = Math.min(1.0, maxDistancia / distancia);
        return new Punto(this.x + dx * factor, this.y + dy * factor);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Punto)) return false;
        Punto other = (Punto) obj;
        return Double.compare(x, other.x) == 0 && Double.compare(y, other.y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Punto{" + "x=" + x + ", y=" + y + '}';
    }
}
