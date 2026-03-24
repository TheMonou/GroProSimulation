package de.monou.model;

import java.util.List;

public class PlatzierungResult {
    private int hoehe;
    private List<Auftrag> auftragList;
    private List<Koordinate> andockpunkte;

    public PlatzierungResult() {

    }

    public int getHoehe() {
        return hoehe;
    }

    public void setHoehe(int hoehe) {
        this.hoehe = hoehe;
    }

    public List<Auftrag> getAuftragList() {
        return auftragList;
    }

    public void addAllAuftraege(List<Auftrag> auftragList) {
        this.auftragList.addAll(auftragList);
    }

    public void addAuftrag(Auftrag auftrag) {
        this.auftragList.add(auftrag);
    }

    public List<Koordinate> getAndockpunkte() {
        return andockpunkte;
    }

    public void addAll(List<Auftrag> auftragList) {
        this.auftragList.addAll(auftragList);
    }

    public void add(Auftrag auftrag) {
        this.auftragList.add(auftrag);
    }
}
