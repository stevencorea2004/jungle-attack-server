package red;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Mensaje {
    private TipoMensaje tipo;
    private JsonObject payload;
    private static final Gson GSON = new Gson();

    public Mensaje() {
    }

    public Mensaje(TipoMensaje tipo, JsonObject payload) {
        this.tipo = tipo;
        this.payload = payload;
    }

    public TipoMensaje getTipo() {
        return tipo;
    }

    public Object getPayload(Class<?> tipoClase) {
        if (payload == null || tipoClase == null) {
            return null;
        }
        return GSON.fromJson(payload, tipoClase);
    }

    public JsonObject getPayloadJson() {
        return payload;
    }

    public void setTipo(TipoMensaje tipo) {
        this.tipo = tipo;
    }

    public void setPayload(JsonObject payload) {
        this.payload = payload;
    }
}
