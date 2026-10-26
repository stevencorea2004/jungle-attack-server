package model;

public enum Especie {
    JAGUAR("Jaguar", "svg/jaguar.svg"),
    DANTA("Danta", "svg/danta.svg"),
    PEREZOSO("Perezoso", "svg/perezoso.svg"),
    MONO_CONGO("Mono congo", "svg/mono_congo.svg"),
    TUCAN("Tucán", "svg/tucan.svg"),
    COCODRILO("Cocodrilo", "svg/cocodrilo.svg"),
    PIZOTE("Pizote", "svg/pizote.svg");

    private final String nombre;
    private final String rutaSvg;

    Especie(String nombre, String rutaSvg) {
        this.nombre = nombre;
        this.rutaSvg = rutaSvg;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRutaSvg() {
        return rutaSvg;
    }
}
