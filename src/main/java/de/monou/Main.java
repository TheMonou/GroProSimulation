package de.monou;

import de.monou.io.AuftragInput;
import de.monou.io.InputHandler;
import de.monou.io.InputHandlerException;
import de.monou.io.OutputHandler;
import de.monou.model.Auftrag;
import de.monou.model.Koordinate;
import de.monou.model.PlatzierungResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    final static InputHandler inputHandler = new InputHandler();
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static OutputHandler outputHandler = new OutputHandler();

    public static void main(String[] args) {
        AuftragInput input =  new AuftragInput(0, "");
        String filename = args[0];
        List<String> errors = new ArrayList<>();
        try{
            input = inputHandler.handleInput(filename);
        }catch (InputHandlerException e){
            errors.add("Error reading file: " + e.getMessage());
            logger.error("Error reading file: " + e.getMessage());
        }

        List<Auftrag> result = new ArrayList<>();
        List<Koordinate> andockpunkte = new ArrayList<>();
        andockpunkte.add(new Koordinate(0,0));
        int hoehe = 0;

        for(List<Auftrag> abschnitt : input.getAbschnitte()){
            de.monou.model.PlatzierungResult platzierungResult = platziereAuftraege(
                    sortiereAuftraege(abschnitt),
                    input.getBreite(),
                    0,
                    andockpunkte);

            result.addAll(platzierungResult.getAuftragList());
            hoehe += platzierungResult.getHoehe();
            andockpunkte.clear();
            andockpunkte.addAll(platzierungResult.getAndockpunkte());
        }


        outputHandler.createOutput(result, andockpunkte, input.getBreite(), hoehe, input.getBeschreibung(),  filename, errors.toArray(String[]::new));




    }

    private static List<Auftrag> sortiereAuftraege(List<Auftrag> auftraege){
        return auftraege.stream()
                .sorted((a1, a2) -> {
                    int flaeche1 = a1.getBreite() * a1.getHoehe();
                    int flaeche2 = a2.getBreite() * a2.getHoehe();
                    return Integer.compare(flaeche2, flaeche1); // Sortiere absteigend nach Fläche
                })
                .toList();
    }

    private static PlatzierungResult platziereAuftraege(List<Auftrag> auftraege, int breite, int hoehe, List<Koordinate> andockpunkte){
        List<Auftrag> auftragList = new ArrayList<>(auftraege);
        List<Auftrag> platzierteAuftragList = new ArrayList<>();
        List<Koordinate> uebrigeAndockpunkte = new ArrayList<>(andockpunkte);
        int bestHoehe = hoehe;

        // Über alle Aufträge iterieren
        for(int i = 0; i < auftragList.size(); i++){
            Auftrag auftrag = auftragList.remove(i);
            logger.info("Auftrag {} wird verwendet", auftrag.toString());

            // Alle Aufträge außer den aktuellen betrachten
            List<Auftrag> currentAuftraege = new ArrayList<>(List.copyOf(auftragList));

            // Auftrag in beide Richtungen betrachten
            Auftrag flippedAuftrag = new Auftrag(auftrag.getId(), auftrag.getHoehe(), auftrag.getBreite(), auftrag.getBeschreibung());

            // Aktuelle Pfadhoehe
            int pfadHoehe = 0;

            Auftrag platzierterAuftrag = null;
            List<Auftrag> pfadAuftraegeList = new ArrayList<>();

            //Jeden freien Andockpunkt betrachten
            for(int k = 0; k < andockpunkte.size(); k++){
                Koordinate moeglicherAndockpunkt = andockpunkte.get(i);

                logger.info("Möglicher Andockpunkt {}", moeglicherAndockpunkt.toString());
                //Überprüfen, ob der Auftrag an diesem Andockpunkt platziert werden kann

                // Auftrag platzieren
                auftrag.setAnkerpunkt(moeglicherAndockpunkt);
                List<Koordinate> currentAndockpunkte = new ArrayList<>(andockpunkte);

                if(kannPlatzieren(moeglicherAndockpunkt, auftrag, breite, andockpunkte)){
                    logger.info("Auftrag konnte platziert werden");

                    // Andockpunkte aktualisieren
                    currentAndockpunkte.remove(moeglicherAndockpunkt);
                    currentAndockpunkte.add(auftrag.getAndockpunktLO());
                    currentAndockpunkte.add(auftrag.getAndockpunktRU());

                    //Letzter Auftrag / Rekursionsanker
                    if(currentAuftraege.isEmpty()){
                        //Erster Pfad
                        if(pfadHoehe==0){
                            pfadHoehe = auftrag.getHoehe() +  hoehe;
                            platzierterAuftrag = auftrag;
                            uebrigeAndockpunkte = currentAndockpunkte;
                        }else{
                            //Überschreiben, wenn besser als bisherige Lösung
                            if(pfadHoehe + auftrag.getHoehe() <= bestHoehe){
                                bestHoehe = pfadHoehe + auftrag.getHoehe();
                                platzierterAuftrag = auftrag;
                                uebrigeAndockpunkte = currentAndockpunkte;
                            }
                        }
                        //Nicht der letzte
                    }else{

                        PlatzierungResult rekursionsSchritt = platziereAuftraege(currentAuftraege, breite, pfadHoehe, currentAndockpunkte);

                        // Erster Pfad -> Immer überschreiben
                        if(pfadHoehe==0){
                            pfadHoehe = auftrag.getHoehe() + rekursionsSchritt.getHoehe();
                            platzierterAuftrag = auftrag;
                            pfadAuftraegeList.addAll(rekursionsSchritt.getAuftragList());
                            currentAndockpunkte.addAll(rekursionsSchritt.getAndockpunkte());
                        }else{
                            //Nicht erster Pfad -> überschreiben, wenn besser als bisheriger Pfad
                            if(auftrag.getHoehe() + rekursionsSchritt.getHoehe() <= pfadHoehe){
                                // Höhe neu berechnen
                                pfadHoehe = auftrag.getHoehe() + rekursionsSchritt.getHoehe();

                                // aktuellen Auftrag als beste Lösung speichern
                                platzierterAuftrag = auftrag;

                                // Übrigen Andockpunkte speichern
                                uebrigeAndockpunkte = currentAndockpunkte;

                                // Pfad in die Wiedergabe hinzufügen
                                pfadAuftraegeList = rekursionsSchritt.getAuftragList();
                            }
                        }

                    }

                    // Beste Position platzieren
                    platzierteAuftragList.addFirst(platzierterAuftrag);
                }

                if(bestHoehe==0){
                    bestHoehe = pfadHoehe;
                    platzierteAuftragList.add(auftrag);
                }else if(pfadHoehe<=bestHoehe){
                    bestHoehe = pfadHoehe;
                    platzierteAuftragList.add(auftrag);
                }

                flippedAuftrag.setAnkerpunkt(moeglicherAndockpunkt);
                // Gedrehte Version
                if(kannPlatzieren(moeglicherAndockpunkt, flippedAuftrag, breite, andockpunkte)) {

                }

            }


        }
        
        
        PlatzierungResult ergebnis = new PlatzierungResult();
        // Fertig platzierte Aufträge
        ergebnis.addAllAuftraege(platzierteAuftragList);
        // Ermittelte Hoehe
        ergebnis.setHoehe(bestHoehe);
        // Übbrig gebliebene Aufträge
        ergebnis.addAllAndockpunkte(uebrigeAndockpunkte);



        return  ergebnis;
    }

    private static boolean kannPlatzieren(Koordinate andockpunkt, Auftrag auftrag, int gesamtBreite,
                                          List<Koordinate> andockpunkte){

        if(auftrag.getAndockpunktRU().getX() > gesamtBreite){
            return false;
        }
        for(Koordinate k : andockpunkte){
            // Überlappung von unten
            if((k.getX() > auftrag.getAndockpunktLO().getX()) && (k.getY() < auftrag.getAndockpunktLO().getY()) && (k.getY() > auftrag.getAnkerpunkt().getY())){
                System.out.println("Überlappung von unten");
                System.out.println("Koordinate: (x: " + k.getX() + ", y: " + k.getY() + "), Auftrag: (x: " + auftrag.getAndockpunktLO().getX() + ", y: " + auftrag.getAndockpunktLO().getY() + ")");
                return false;
            }

            //Überlappung von links
            if((k.getX() < auftrag.getAndockpunktRU().getX()) && (k.getX() > auftrag.getAnkerpunkt().getX()) && (k.getY() > auftrag.getAndockpunktRU().getY())){
                return false;
            }
        }
        return true;
    }
}
