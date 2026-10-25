package red;

public final class FabricaMensajes {
    private FabricaMensajes() {
        throw new AssertionError("Clase de fábrica");
    }

    public static Mensaje crear(TipoMensaje tipo, Object payload) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public static Mensaje error(String razon) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }
}
