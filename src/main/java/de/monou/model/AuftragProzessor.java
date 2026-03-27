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


        ergebnis.addAllAuftraege(backtracking(sortierteAuftraege, new ArrayList<Auftrag>(), breite, andockpunkte));
        ergebnis.addAllAndockpunkte(andockpunkte);
        ergebnis.setHoehe(bestHoehe);

        return ergebnis;
    }

    private List<Auftrag> backtracking(List<Auftrag> verbleibendeAuftraege, List<Auftrag> platzierteAutraege, int maxBreite, List<Koordinate> andockpunkte) {
        List<Auftrag> auftragList = new ArrayList<>(verbleibendeAuftraege);
        List<Auftrag> wiedergabeAutrage = new ArrayList<>();


        for (int i = 0; i < auftragList.size(); i++) {
            Auftrag auftrag = auftragList.remove(i);

            for (int j = 0; j < andockpunkte.size(); j++) {
                Koordinate moeglicherAndockpunkt = andockpunkte.get(j);
                auftrag.setAnkerpunkt(moeglicherAndockpunkt);
                List<Koordinate> neueAndockpunkte = new ArrayList<>(andockpunkte);
                neueAndockpunkte.remove(moeglicherAndockpunkt);
                neueAndockpunkte.add(auftrag.getAndockpunktLO());
                neueAndockpunkte.add(auftrag.getAndockpunktRU());

                if (kannPlatzieren(auftrag, maxBreite, neueAndockpunkte)) {

                    // Erster Durchlauf
                    if (bestHoehe == 0) {
                        //Rekursionsanker
                        if (auftragList.isEmpty()) {
                            bestHoehe = auftrag.getRo().getY();
                            wiedergabeAutrage.add(auftrag);
                        } else {
                            //Rekursionsschritt
                            wiedergabeAutrage.add(auftrag);
                            wiedergabeAutrage.addAll(backtracking(auftragList, maxBreite, neueAndockpunkte));
                        }
                        andockpunkte = neueAndockpunkte;

                    // Alle weiteren Durchläufe
                    } else {
                        // Rekursionsschritt
                        if (auftragList.isEmpty()) {
                            if (auftrag.getRo().getY() < bestHoehe) {
                                bestHoehe = auftrag.getRo().getY();
                                wiedergabeAutrage.clear();
                                wiedergabeAutrage.add(auftrag);
                                andockpunkte = neueAndockpunkte;
                            }
                        } else {
                            wiedergabeAutrage.clear();
                            wiedergabeAutrage.add(auftrag);
                            wiedergabeAutrage.addAll(backtracking(auftragList, maxBreite, neueAndockpunkte));
                            andockpunkte = neueAndockpunkte;
                        }
                    }
                }
                Auftrag flipAuftrag = new Auftrag(auftrag.getId(), auftrag.getHoehe(), auftrag.getBreite(), auftrag.getBeschreibung());
                flipAuftrag.setAnkerpunkt(moeglicherAndockpunkt);
                if (kannPlatzieren(flipAuftrag, maxBreite, neueAndockpunkte)) {

                    if (auftragList.isEmpty()) {
                        if (flipAuftrag.getRo().getY() < bestHoehe) {
                            bestHoehe = flipAuftrag.getRo().getY();
                            wiedergabeAutrage.clear();
                            wiedergabeAutrage.add(flipAuftrag);
                            andockpunkte = neueAndockpunkte;
                        }
                    } else {
                        wiedergabeAutrage.clear();
                        wiedergabeAutrage.add(flipAuftrag);
                        wiedergabeAutrage.addAll(backtracking(auftragList, maxBreite, neueAndockpunkte));
                        andockpunkte = neueAndockpunkte;
                    }

                }
            }
        }
        return wiedergabeAutrage;
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
