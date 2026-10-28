package red;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.geom.Rectangle2D;
import java.util.List;
import mapa.Camino;
import mapa.Mapa;
import mapa.Ruta;
import mapa.Zona;
import model.Punto;
import org.junit.jupiter.api.Test;

class ComandoMoverTest {
    @Test
    void groupDispersionDoesNotStopBeforeEnteringDestinationZone() {
        Mapa mapa = new Mapa();
        mapa.agregarZona(new Zona(1, new Rectangle2D.Double(0, 0, 100, 100)));
        mapa.agregarZona(new Zona(2, new Rectangle2D.Double(100, 0, 100, 100)));
        mapa.agregarCamino(new Camino(1, 2,
                List.of(new Punto(0, 50), new Punto(100, 50), new Punto(200, 50))));
        Ruta ruta = Ruta.crear(mapa, null, new Punto(50, 50), new Punto(101, 50));

        Ruta rutaDispersa = ComandoMover.recortarFinalRuta(ruta, 18.0, mapa, 2);

        List<Punto> puntos = rutaDispersa.getPuntos();
        assertEquals(2, mapa.zonaEn(puntos.get(puntos.size() - 1)).getId());
        assertEquals(101.0, puntos.get(puntos.size() - 1).getX());
    }

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
