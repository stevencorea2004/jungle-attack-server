package partida;

import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import util.Constantes;

public class GeneradorCodigo {
    private static final int INTENTOS_MAXIMOS = 1024;
    private final Random random;

    public GeneradorCodigo() {
        this(new Random());
    }

    GeneradorCodigo(Random random) {
        if (random == null) {
            throw new IllegalArgumentException("El generador aleatorio es obligatorio");
        }
        this.random = random;
    }

    public synchronized String generar(Set<String> existentes) {
        Set<String> normalizados = new HashSet<>();
        if (existentes != null) {
            for (String existente : existentes) {
                String normalizado = normalizar(existente);
                if (normalizado != null) {
                    normalizados.add(normalizado);
                }
            }
        }

        int cantidadCodigos = 1;
        for (int i = 0; i < Constantes.LARGO_CODIGO; i++) {
            cantidadCodigos *= Constantes.CARACTERES_CODIGO.length();
        }
        if (normalizados.size() >= cantidadCodigos) {
            throw new IllegalStateException("No quedan códigos de partida disponibles");
        }

        for (int intento = 0; intento < INTENTOS_MAXIMOS; intento++) {
            StringBuilder codigo = new StringBuilder(Constantes.LARGO_CODIGO);
            for (int i = 0; i < Constantes.LARGO_CODIGO; i++) {
                int indice = random.nextInt(Constantes.CARACTERES_CODIGO.length());
                codigo.append(Constantes.CARACTERES_CODIGO.charAt(indice));
            }
            String generado = codigo.toString();
            if (!normalizados.contains(generado)) {
                return generado;
            }
        }
        throw new IllegalStateException("No se pudo generar un código de partida único");
    }

    public static String normalizar(String codigo) {
        return codigo == null ? null : codigo.trim().toUpperCase(java.util.Locale.ROOT);
    }
}
