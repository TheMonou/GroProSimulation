package de.monou.model;

import java.util.ArrayList;
import java.util.List;

public class AuftragProzessor {
    private int bestHoehe = 0;

    public AuftragResult optimiereNutzung(List<Auftrag> auftragList, int breite) {
        AuftragResult ergebnis = new AuftragResult();
        List<Auftrag> sortierteAuftraege = sortiereAuftraege(auftragList);
        List<Koordinate> andockpunkte = new ArrayList<>();
        andockpunkte.add(new Koordinate(0, 0));


        ergebnis.addAllAuftraege(backtracking(sortierteAuftraege, new ArrayList<Auftrag>(), breite, 0, andockpunkte));
        ergebnis.addAllAndockpunkte(andockpunkte);
        ergebnis.setHoehe(bestHoehe);

        return ergebnis;
    }

    private List<Auftrag> backtracking(List<Auftrag> verbleibendeAuftraege, List<Auftrag> platzierteAutraege, int maxBreite, int pfadHoehe, List<Koordinate> andockpunkte) {
        List<Auftrag> wiedergabeAuftraege = new ArrayList<>();


        for (int i = 0; i < verbleibendeAuftraege.size(); i++) {
            List<Auftrag> auftragList = new ArrayList<>(verbleibendeAuftraege);
            Auftrag auftrag = auftragList.remove(i);

            for (int j = 0; j < andockpunkte.size(); j++) {
                Koordinate moeglicherAndockpunkt = andockpunkte.get(j);
                auftrag.setAnkerpunkt(moeglicherAndockpunkt);
                List<Koordinate> neueAndockpunkte = new ArrayList<>(andockpunkte);
                neueAndockpunkte.remove(moeglicherAndockpunkt);
                neueAndockpunkte.add(auftrag.getAndockpunktLO());
                neueAndockpunkte.add(auftrag.getAndockpunktRU());



                if (kannPlatzieren(auftrag, maxBreite, platzierteAutraege)) {
                        //Rekursionsanker
                        if (auftragList.isEmpty()) {
                            if(bestHoehe==0){
                                bestHoehe = pfadHoehe;
                                wiedergabeAuftraege.add(auftrag);
                                andockpunkte = neueAndockpunkte;
                            }else if(pfadHoehe < bestHoehe){
                                bestHoehe = auftrag.getRo().getY();
                                wiedergabeAuftraege.add(auftrag);
                                andockpunkte = neueAndockpunkte;
                            }
                        } else {
                            //Rekursionsschritt
                            // 1. Nimm die Liste der Eltern (platzierteAutraege) und kopiere sie in eine NEUE Liste
                            List<Auftrag> neuePlatzierteAutraege = new ArrayList<>(platzierteAutraege);

                            // 2. Füge den aktuell getesteten Auftrag zu dieser neuen, pfadspezifischen Liste hinzu
                            neuePlatzierteAutraege.add(auftrag);

                            // 3. Gib diese neue Liste in den Rekursionsschritt weiter
                            wiedergabeAuftraege.addAll(backtracking(auftragList, neuePlatzierteAutraege, maxBreite, pfadHoehe, neueAndockpunkte));
                        }

                }
                Auftrag flipAuftrag = new Auftrag(auftrag.getId(), auftrag.getHoehe(), auftrag.getBreite(), auftrag.getBeschreibung());
                flipAuftrag.setAnkerpunkt(moeglicherAndockpunkt);

            }

        }
        return wiedergabeAuftraege;
    }

    private boolean kannPlatzieren(
            Auftrag auftrag,
            int maxBreite,
            List<Auftrag> platzierteAuftraege) {

        // Nicht platzieren, wenn die maximale Breite der Rolle überschritten wird
        if (auftrag.getRo().getX() > maxBreite) {
            return false;
        }

        // Nicht platzieren, wenn wir schon eine bessere/gleich gute Lösung gefunden haben
        if (bestHoehe != 0 && auftrag.getRo().getY() > bestHoehe) {
            return false;
        }

        // Überprüfen, ob der neue Auftrag mit irgendeinem bereits platzierten Auftrag kollidiert
        for (Auftrag platzierterAuftrag : platzierteAuftraege) {

            // Standard 2D-Kollisionsabfrage (Bounding-Box)
            boolean ueberlapptX = auftrag.getAnkerpunkt().getX() < platzierterAuftrag.getRo().getX() &&
                    auftrag.getRo().getX() > platzierterAuftrag.getAnkerpunkt().getX();

            boolean ueberlapptY = auftrag.getAnkerpunkt().getY() < platzierterAuftrag.getRo().getY() &&
                    auftrag.getRo().getY() > platzierterAuftrag.getAnkerpunkt().getY();

            if (ueberlapptX && ueberlapptY) {
                return false; // Überlappung
            }
        }

        return true;
    }

    private static List<Auftrag> sortiereAuftraege(List<Auftrag> auftraege) {
        return auftraege.stream()
                .sorted((a1, a2) -> {
                    int flaeche1 = a1.getBreite() * a1.getHoehe();
                    int flaeche2 = a2.getBreite() * a2.getHoehe();
                    return Integer.compare(flaeche2, flaeche1); // Sortiere absteigend nach Fläche
                })
                .toList();
    }
}
