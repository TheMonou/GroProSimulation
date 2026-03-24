package de.monou.io;
import de.monou.model.Auftrag;
import de.monou.model.Koordinate;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class OutputHandler {

    private static String OUTPUT_PREFIX = "output/";

    public void createOutput(List<Auftrag> auftraege, List<Koordinate> andockpunkte, int breite, int hoehe,
                             String beschreibung, String filename, String errors[]){
        String filenameOnly = Paths.get(filename).getFileName().toString();
        String filepath = OUTPUT_PREFIX + filenameOnly + ".out";


        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
            //Beschreibung
            writer.write(beschreibung);
            writer.newLine();
            //Länge
            writer.write("Benötigte Länge: " + formatiereLaenge(hoehe));
            writer.newLine();
            //Nutzungsgrad
            int gesamtFlaeche = breite * hoehe;
            int genutzteFlaeche = auftraege.stream().mapToInt(auftrag -> auftrag.getBreite() * auftrag.getHoehe()).sum();
            double nutzungsgrad = (double) genutzteFlaeche / gesamtFlaeche * 100;
            writer.write(String.format("Genutzte Fläche: %.2f%%", nutzungsgrad));
            writer.newLine();
            //Aufträge
            writer.write("Positionierung der Kundenaufträge:");
            writer.newLine();

            for (Auftrag auftrag : auftraege) {
                writer.write(String.format("  %d %d %d %d - %d - %s",
                        auftrag.getAnkerpunkt().getX(), auftrag.getAnkerpunkt().getY(),
                        auftrag.getRo().getX(), auftrag.getRo().getY(), auftrag.getId(), auftrag.getBeschreibung()));
                writer.newLine();
            }

            //Andockpunkte
            writer.write("Verbleibende Andockpunkte:");
            writer.newLine();
            for (Koordinate andockpunkt : andockpunkte) {
                writer.write(String.format("  %d %d", andockpunkt.getX(), andockpunkt.getY()));
                writer.newLine();
            }

            //errors
            writer.write("**errors: ");
            writer.newLine();

            for (String line : errors) {
                writer.write(line);
                writer.newLine();
            }

        }catch(IOException e){
            throw new OutputHandlerException("Ausgabedatei konnte nicht erstellt werden: " + e.getMessage());
        }
    }

    private double formatiereLaenge(int hoehe){
        return hoehe/ 10.0;
    }
}
