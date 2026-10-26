package red;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public final class FabricaMensajes {
    private static final Gson GSON = new Gson();

    private FabricaMensajes() {
        throw new AssertionError("Clase de fábrica");
    }

    public static Mensaje crear(TipoMensaje tipo, Object payload) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo del mensaje es obligatorio");
        }
        JsonObject json = payload == null ? new JsonObject() : GSON.toJsonTree(payload).getAsJsonObject();
        return new Mensaje(tipo, json);
    }

    public static Mensaje error(String razon) {
        JsonObject json = new JsonObject();
        json.addProperty("error", razon);
        return new Mensaje(TipoMensaje.ERROR, json);
    }
}