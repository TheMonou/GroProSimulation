package de.monou.io;

import de.monou.model.Auftrag;
import de.monou.model.Koordinate;

import java.util.ArrayList;
import java.util.List;

public class AuftragInput {
    private final int breite;
    List<Koordinate> urspruenge;
    List<List<Auftrag>> abschnitte;
    private final String beschreibung;

    public AuftragInput(int breite, String beschreibung) {
        this.breite = breite;
        urspruenge = new ArrayList<>();
        urspruenge.add(new Koordinate(0,0));
        abschnitte = new ArrayList<>();
        this.beschreibung = beschreibung;
    }

        public void addUrsprung(Koordinate k){
            urspruenge.add(k);
        }

        public void addAbschnitt(List<Auftrag> abschnitt){
            abschnitte.add(abschnitt);
        }

        public List<Koordinate> getUrspruenge(){
            return urspruenge;
        }

        public List<List<Auftrag>> getAbschnitte(){
            return abschnitte;
        }

        public int getBreite() {
            return breite;
        }

        public String getBeschreibung() {
            return beschreibung;
        }


}
