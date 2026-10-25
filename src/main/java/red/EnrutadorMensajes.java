package red;

import java.util.Map;

public class EnrutadorMensajes {
    private Map<TipoMensaje, IComando> comandos;

    public void registrar(TipoMensaje tipo, IComando comando) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    public void enrutar(ManejadorCliente cliente, Mensaje mensaje) {
        throw new UnsupportedOperationException("Pendiente de implementación");
    }
}
