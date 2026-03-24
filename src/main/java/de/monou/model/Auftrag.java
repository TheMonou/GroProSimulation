package de.monou.model;

public class Auftrag {
    private final int id;
    private int breite;

    private int hoehe;

    private final String beschreibung;

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
}
