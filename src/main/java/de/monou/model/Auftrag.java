package de.monou.model;

public class Auftrag {
    private final int id;
    private int breite;

    private int hoehe;

    private final String beschreibung;

    private Koordinate ankerpunkt;

    private Koordinate andockpunktLO;

    private Koordinate andockpunktRU;

    private Koordinate ro;

    public Auftrag(int id, int breite, int hoehe, String beschreibung) {
        this.id = id;
        this.breite = breite;
        this.hoehe = hoehe;
        this.beschreibung = beschreibung;
    }

    public int getId() {
        return id;
    }

    public int getBreite() {
        return breite;
    }

    public void setBreite(int breite) {
        this.breite = breite;
    }

    public int getHoehe() {
        return hoehe;
    }

    public void setHoehe(int hoehe) {
        this.hoehe = hoehe;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void setAnkerpunkt(Koordinate ankerpunkt) {
        this.ankerpunkt = ankerpunkt;
        this.andockpunktLO = new Koordinate(ankerpunkt.getX(),  ankerpunkt.getY() + hoehe);
        this.andockpunktRU = new Koordinate(ankerpunkt.getX() + breite, ankerpunkt.getY());
        this.ro = new Koordinate(ankerpunkt.getX() + breite, ankerpunkt.getY() + hoehe);
    }

    public Koordinate getAnkerpunkt() {
        return ankerpunkt;
    }

    public Koordinate getAndockpunktLO() {
        return andockpunktLO;
    }

    public Koordinate getAndockpunktRU() {
        return andockpunktRU;
    }

    public Koordinate getRo() {
        return ro;
    }

}
