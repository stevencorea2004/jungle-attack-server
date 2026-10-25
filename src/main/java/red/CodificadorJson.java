package red;

import com.google.gson.Gson;

public final class CodificadorJson {
    private static final CodificadorJson INSTANCIA = new CodificadorJson();
    private final Gson gson = new Gson();

    private CodificadorJson() {
    }

    public static CodificadorJson getInstancia() {
        return INSTANCIA;
    }

    public String codificar(Mensaje mensaje) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public Mensaje decodificar(String linea) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }
}
