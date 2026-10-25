package util;

public final class Constantes {
    public static final int TAM_MANADA_JUGADOR = 10;
    public static final int ENERGIA_JUGADOR = 20;
    public static final int ENERGIA_MAX_JUGADOR = 20;
    public static final int DANIO_JUGADOR = 2;
    public static final int TAM_MANADA_BOT = 4;
    public static final int ENERGIA_BOT = 10;
    public static final int DANIO_BOT = 1;
    public static final int CANTIDAD_ESPECIES = 7;
    public static final long COOLDOWN_ATAQUE_MS = 1000L;
    public static final double RADIO_ENCUENTRO = 0;
    public static final int BOTS_POR_BONO = 2;
    public static final int BONO_ENERGIA = 1;
    public static final int CANTIDAD_ZONAS = 12;
    public static final int CANTIDAD_TAZAS = 10;
    public static final int BONO_DANIO_TAZA = 1;
    public static final double RADIO_TAZA = 0;
    public static final double RADIO_BANDERA = 0;
    public static final double VELOCIDAD_ANIMAL = 0;
    public static final long TICK_MS = 0L;
    public static final int LARGO_CODIGO = 4;
    public static final String CARACTERES_CODIGO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static final int PUERTO = 0;
    public static final String CHARSET = "UTF-8";

    private Constantes() {
        throw new AssertionError("Clase de constantes");
    }
}
