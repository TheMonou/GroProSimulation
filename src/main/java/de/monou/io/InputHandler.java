package de.monou.io;

import de.monou.model.Auftrag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class InputHandler {

    Logger logger = Logger.getLogger(InputHandler.class.getName());

    public AuftragInput handleInput(String filepath) {
        List<String> input;

        try {
            input = new ArrayList<>(Files.readAllLines(Path.of(filepath + ".in"))
                    .stream()
                    .filter(line -> !line.isBlank())
                    .toList());

        } catch (IOException e) {
            throw new InputHandlerException("Could not read file: " + e.getMessage());
        }

        String beschreibung = input.removeFirst();
        if (!beschreibung.startsWith("//")) {
            throw new InputHandlerException("Die erste Zeile muss eine Beschreibung mit dem Präfix '//' enthalten.");
        }
        String breiteTiefe = input.removeFirst();

        int breite;
        int tiefe;

        try {
            breite = Integer.parseInt(breiteTiefe.split(", ")[0]);
            tiefe = Integer.parseInt(breiteTiefe.split(", ")[1]);
        } catch (NumberFormatException e) {
            throw new InputHandlerException("Die zweite Zeile muss die Breite und Optimierungstiefe im Format 'breite, tiefe' enthalten.");
        }
        AuftragInput auftragInput = new AuftragInput(breite, beschreibung);

        List<Auftrag> auftraege = new ArrayList<>();
        int i = 0;

        for (String zeile : input) {
            Auftrag auftrag = validiere(zeile);
            if (auftrag == null) {
                throw new InputHandlerException("Ungültige Zeile: " + zeile);
            }
            auftraege.add(auftrag);
            i++;
            if (i == tiefe) {
                auftragInput.addAbschnitt(auftraege);
                auftraege = new ArrayList<>();
                i = 0;
            }
        }
        auftragInput.addAbschnitt(auftraege);

        return auftragInput;
    }

    private Auftrag validiere(String zeile) {
        int breite;
        try{
            breite = Integer.parseInt(zeile.split(", ")[0]);
        }catch(NumberFormatException | ArrayIndexOutOfBoundsException e){
            throw new InputHandlerException("Breite des Auftrags konnte nicht gelesen werden" + zeile);
        }
        int hoehe;
        try{
            hoehe = Integer.parseInt(zeile.split(", ")[1]);
        }catch(NumberFormatException | ArrayIndexOutOfBoundsException e){
            throw new InputHandlerException("Höhe des Auftrags konnte nicht gelesen werden" + zeile);
        }

        int id;
        try{
            id = Integer.parseInt(zeile.split(", ")[2]);
        }catch(NumberFormatException | ArrayIndexOutOfBoundsException e){
            throw new InputHandlerException("ID des Auftrags konnte nicht gelesen werden" + zeile);
        }

        String beschreibung;
        try{
            beschreibung = zeile.split(", ")[3];
        }catch(ArrayIndexOutOfBoundsException e){
            throw new InputHandlerException("Beschreibung des Auftrags konnte nicht gelesen werden" + zeile);
        }


        return new Auftrag(id, breite, hoehe, beschreibung);
    }
}
