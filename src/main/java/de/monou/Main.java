package de.monou;

import de.monou.io.AuftragInput;
import de.monou.io.InputHandler;
import de.monou.io.InputHandlerException;
import de.monou.io.OutputHandler;
import de.monou.model.Auftrag;
import de.monou.model.AuftragProzessor;
import de.monou.model.Koordinate;
import de.monou.model.AuftragResult;
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
    final static AuftragProzessor auftragProzessor = new AuftragProzessor();

     private static List<Auftrag> sortiereAuftraege(List<Auftrag> auftragList){
        List<Auftrag> sortierteAuftraege = new ArrayList<>(auftragList);
        sortierteAuftraege.sort((a1, a2) -> {
            int flaeche1 = a1.getBreite() * a1.getHoehe();
            int flaeche2 = a2.getBreite() * a2.getHoehe();
            return Integer.compare(flaeche2, flaeche1);
        });
        return sortierteAuftraege;
    }

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
            AuftragResult auftragResult = auftragProzessor.optimiereNutzung(abschnitt, input.getBreite());

            result.addAll(auftragResult.getAuftragList());
            hoehe += auftragResult.getHoehe();
            andockpunkte.clear();
            andockpunkte.addAll(auftragResult.getAndockpunkte());
        }


        outputHandler.createOutput(result, andockpunkte, input.getBreite(), hoehe, input.getBeschreibung(),  filename, errors.toArray(String[]::new));




    }
}
