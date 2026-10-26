package red;

import java.util.EnumMap;
import java.util.Map;

/** Command (invocador): busca el comando del tipo de mensaje y lo ejecuta. */
public class EnrutadorMensajes {
    private Map<TipoMensaje, IComando> comandos = new EnumMap<>(TipoMensaje.class);

    public void registrar(TipoMensaje tipo, IComando comando) {
        comandos.put(tipo, comando);
    }

    public void enrutar(ManejadorCliente cliente, Mensaje mensaje) {
        IComando comando = comandos.get(mensaje.getTipo());
        if (comando == null) {
            cliente.enviar(FabricaMensajes.error("Tipo de mensaje no soportado: " + mensaje.getTipo()));
            return;
        }
        comando.ejecutar(cliente, mensaje);
    }
}
