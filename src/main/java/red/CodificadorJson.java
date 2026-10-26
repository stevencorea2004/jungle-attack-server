package red;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public final class CodificadorJson {
    private static final CodificadorJson INSTANCIA = new CodificadorJson();
    private final Gson gson = new Gson();

    private CodificadorJson() {
    }

    public static CodificadorJson getInstancia() {
        return INSTANCIA;
    }

    public String codificar(Mensaje mensaje) {
        JsonObject json = new JsonObject();
        json.addProperty("tipo", mensaje.getTipo().name());
        if (mensaje.getPayloadJson() != null) {
            json.add("payload", mensaje.getPayloadJson());
        }
        return gson.toJson(json);
    }

    public Mensaje decodificar(String linea) {
        if (linea == null || linea.isBlank()) {
            return null;
        }
        JsonObject json = gson.fromJson(linea, JsonObject.class);
        TipoMensaje tipo = TipoMensaje.valueOf(json.get("tipo").getAsString());
        JsonObject payload = json.has("payload") ? json.getAsJsonObject("payload") : new JsonObject();
        return new Mensaje(tipo, payload);
    }
}
