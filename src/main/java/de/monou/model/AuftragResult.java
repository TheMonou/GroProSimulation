package de.monou.model;

import java.util.ArrayList;
import java.util.List;

public class AuftragResult {
    private int hoehe = 0;
    private List<Auftrag> auftragList;
    private List<Koordinate> andockpunkte;

    public AuftragResult() {
        auftragList = new ArrayList<Auftrag>();
        andockpunkte = new ArrayList<>();
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

    public void addAllAndockpunkte(List<Koordinate> andockpunkteList) {
        this.andockpunkte.addAll(andockpunkteList);
    }

    public void add(Auftrag auftrag) {
        this.auftragList.add(auftrag);
    }
}
