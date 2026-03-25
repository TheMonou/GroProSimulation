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
        int hoehe = 0;

        for(List<Auftrag> abschnitt : input.getAbschnitte()){
            de.monou.model.PlatzierungResult platzierungResult = platziereAuftraege(
                    sortiereAuftraege(abschnitt),
                    input.getBreite(),
                    0,
                    andockpunkte);

            result.addAll(platzierungResult.getAuftragList());
            hoehe += platzierungResult.getHoehe();
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
        List<Auftrag> result = new ArrayList<>();
        int bestHoehe = hoehe;

        // Über alle Aufträge iterieren
        for(int i = 0; i < auftraege.size(); i++){
            Auftrag auftrag = auftraege.remove(i);

            // Alle Aufträge außer den aktuellen betrachten
            List<Auftrag> currentAuftraege = new ArrayList<>(List.copyOf(auftraege));

            // Auftrag in beide Richtungen betrachten
            Auftrag flippedAuftrag = new Auftrag(auftrag.getId(), auftrag.getHoehe(), auftrag.getBreite(), auftrag.getBeschreibung());

            // Aktuelle Pfadhoehe
            int pfadHoehe = 0;

            //Jeden freien Andockpunkt betrachten
            for(int k = 0; k < andockpunkte.size(); k++){
                Koordinate moeglicherAndockpunkt = andockpunkte.get(i);

                //Überprüfen, ob der Auftrag an diesem Andockpunkt platziert werden kann
                if(kannPlatzieren(moeglicherAndockpunkt, auftrag.getBreite(), auftrag.getHoehe(), breite, andockpunkte)){
                    // Auftrag platzieren
                    auftrag.setAnkerpunkt(moeglicherAndockpunkt);
                    List<Koordinate> currentAndockpunkte = new ArrayList<>(andockpunkte);

                    // Andockpunkte aktualisieren
                    currentAndockpunkte.remove(moeglicherAndockpunkt);
                    currentAndockpunkte.add(auftrag.getAndockpunktLO());
                    currentAndockpunkte.add(auftrag.getAndockpunktRU());

                    //Letzter Auftrag
                    if(currentAuftraege.isEmpty()){
                        if(k==0){
                            pfadHoehe = auftrag.getHoehe() +  hoehe;
                        }else{
                            if(pfadHoehe + auftrag.getHoehe() <= bestHoehe){
                                bestHoehe = pfadHoehe + auftrag.getHoehe();
                            }
                        }
                    }else{
                        // Erster Pfad
                        if(k==0){
                            pfadHoehe = auftrag.getHoehe() + platziereAuftraege(currentAuftraege, breite, pfadHoehe, currentAndockpunkte).getHoehe();
                        }else{

                        }

                    }


                    
                }
                if(kannPlatzieren(moeglicherAndockpunkt, flippedAuftrag.getBreite(), flippedAuftrag.getHoehe(), breite, andockpunkte)) {
                    //Flipped Auftrag platzieren
                    flippedAuftrag.setAnkerpunkt(moeglicherAndockpunkt);
                    List<Koordinate> currentAndockpunkte = new ArrayList<>(andockpunkte);
                    // Andockpunkte aktualisieren
                    currentAndockpunkte.remove(moeglicherAndockpunkt);
                    currentAndockpunkte.add(flippedAuftrag.getAndockpunktLO());
                    currentAndockpunkte.add(flippedAuftrag.getAndockpunktRU());

                    //Letzter Auftrag
                    if(currentAuftraege.isEmpty()){
                        if(hoehe + flippedAuftrag.getHoehe() <= bestHoehe){
                            bestHoehe = hoehe + flippedAuftrag.getHoehe();
                        }
                    }
                }

            }
        }
        
        
        



        return  result;
    }

    private static boolean kannPlatzieren(Koordinate andockpunkt, int auftragBreite, int auftragHoehe, int gesamtBreite,
                                          List<Koordinate> andockpunkte){
        Koordinate linksOben = new Koordinate(andockpunkt.getX(), andockpunkt.getY() + auftragHoehe);
        Koordinate rechtsUnten = new Koordinate(andockpunkt.getX() + auftragBreite, andockpunkt.getY());
        if(rechtsUnten.getX() > gesamtBreite){
            return false;
        }
        for(Koordinate k : andockpunkte){
            // Überlappung von unten
            if((k.getX() > linksOben.getX()) && (k.getY() < linksOben.getY())){
                return false;
            }

            //Überlappung von links
            if((k.getX() < rechtsUnten.getX()) && (k.getY() > rechtsUnten.getY())){
                return false;
            }
        }
        return true;
    }
}
