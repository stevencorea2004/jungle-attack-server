package red;

import com.google.gson.JsonObject;

public class Mensaje {
    private TipoMensaje tipo;
    private JsonObject payload;

    public TipoMensaje getTipo() {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public Object getPayload(Class<?> tipo) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }
}
