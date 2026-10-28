package red;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import mapa.Ruta;
import model.Punto;
import org.junit.jupiter.api.Test;

class ComandoMoverTest {
    @Test
    void recortaElFinalDeLaRutaParaDispersarElGrupo() {
        Ruta ruta = new Ruta(List.of(new Punto(0, 0), new Punto(100, 0)));

        Ruta rutaDispersa = ComandoMover.recortarFinalRuta(ruta, 20.0);

        Punto destino = rutaDispersa.getPuntos().get(rutaDispersa.getPuntos().size() - 1);
        assertEquals(80.0, destino.getX());
        assertEquals(0.0, destino.getY());
    }

    @Test
    void limitaLaDispersionParaQueElAnimalAvanceAlMenosParteDelRecorrido() {
        Ruta ruta = new Ruta(List.of(new Punto(0, 0), new Punto(100, 0)));

        Ruta rutaDispersa = ComandoMover.recortarFinalRuta(ruta, 80.0);

        Punto destino = rutaDispersa.getPuntos().get(rutaDispersa.getPuntos().size() - 1);
        assertEquals(75.0, destino.getX());
    }
}
