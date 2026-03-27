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
                             String beschreibung, String filename, String errors[]) {
        double nutzungsgrad = (double) (auftraege.stream().mapToInt(auftrag -> auftrag.getBreite() * auftrag.getHoehe()).sum() / (breite * hoehe)) * 100;
        try {
            writeOutputFile(auftraege, andockpunkte, breite, hoehe, nutzungsgrad, beschreibung, filename, errors);
        } catch (IOException e) {
            throw new OutputHandlerException("Fehler beim Schreiben der Ausgabedatei: " + e.getMessage());
        }

        try {
            createOutputGraph(auftraege, andockpunkte, breite, hoehe, nutzungsgrad, filename);
        } catch (Exception e) {
            throw new OutputHandlerException("Fehler beim Erstellen der Graph-Ausgabedatei: " + e.getMessage());
        }

            try {

                runGnuplot(filename);
            } catch (Exception e) {
                throw new OutputHandlerException("Fehler beim Ausführen von Gnuplot: " + e.getMessage());
            }
    }

    private void writeOutputFile(List<Auftrag> auftraege,
                                 List<Koordinate> andockpunkte,
                                 int rollenBreite,
                                 int rollenHoehe,
                                 double nutzungsgrad,
                                 String beschreibung,
                                 String filename,
                                 String errors[]
    ) throws IOException {
        String filenameOnly = Paths.get(filename).getFileName().toString();
        String filepath = OUTPUT_PREFIX + filenameOnly + ".out";


        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
            //Beschreibung
            writer.write(beschreibung);
            writer.newLine();
            //Länge
            writer.write("Benötigte Länge: " + formatiereLaenge(rollenHoehe) + "cm");
            writer.newLine();
            //Nutzungsgrad
            int gesamtFlaeche = rollenBreite * rollenHoehe;

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

        } catch (IOException e) {
            throw new OutputHandlerException("Ausgabedatei konnte nicht erstellt werden: " + e.getMessage());
        }
    }

    private void createOutputGraph(List<Auftrag> auftraege,
                                   List<Koordinate> andockpunkte,
                                   int breite,
                                   int hoehe,
                                   double nutzungsgrad,
                                   String filename
    ) {
        String filenameOnly = Paths.get(filename).getFileName().toString();
        String filepath = OUTPUT_PREFIX + filenameOnly + ".gnu";
        String imgePath = OUTPUT_PREFIX + filenameOnly + ".png";


        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
            writer.write("reset");
            writer.newLine();
            writer.write(String.format("set term png size %d, %d", breite, hoehe + 100));
            writer.newLine();
            writer.write(String.format("set output '%s'", imgePath));
            writer.newLine();
            writer.write(String.format("set xrange [0:%d]", breite));
            writer.newLine();
            writer.write(String.format("set yrange [0:%d]", hoehe));
            writer.newLine();
            writer.write("set size ratio -1 ");
            writer.newLine();
            writer.newLine();
            writer.write("set title \"\\");
            writer.newLine();
            writer.write(String.format("%s\\n\\", filenameOnly));
            writer.newLine();
            writer.write(String.format("Benötigte Länge: %s cm\\n\\", formatiereLaenge(hoehe)));
            writer.newLine();
            writer.write(String.format("Genutzte Fläche: %.2f%%\"", nutzungsgrad));
            writer.newLine();
            writer.newLine();
            writer.write("set style fill transparent solid 0.5 border");
            writer.newLine();
            writer.write("set key noautotitle");
            writer.newLine();
            writer.newLine();
            writer.write("$data << EOD");
            writer.newLine();

            //Aufträge
            for (Auftrag auftrag : auftraege) {
                writer.write(String.format("  %d %d %d %d \"%s\" %d",
                        auftrag.getAnkerpunkt().getX(), auftrag.getAnkerpunkt().getY(),
                        auftrag.getRo().getX(), auftrag.getRo().getY(),
                        auftrag.getBeschreibung(),
                        auftrag.getId()));
                writer.newLine();
            }


            writer.write("EOD");
            writer.newLine();
            writer.newLine();
            writer.write("$anchor << EOD");
            writer.newLine();

            //Andockpunkte
            for (Koordinate andockpunkt : andockpunkte) {
                writer.write(String.format("  %d %d", andockpunkt.getX(), andockpunkt.getY()));
                writer.newLine();
            }
            writer.write("EOD");
            writer.newLine();
            writer.newLine();

            writer.write("plot \\");
            writer.newLine();

            writer.write("'$data' using (($3-$1)/2+$1):(($4-$2)/2+$2):(($3-$1)/2):(($4-$2)/2):6 \\");
            writer.newLine();
            writer.write("    with boxxy linecolor var, \\");
            writer.newLine();

            writer.write("'$data' using (($3-$1)/2+$1):(($4-$2)/2+$2):5 \\");
            writer.newLine();
            writer.write("    with labels font \"arial,9\", \\");
            writer.newLine();

            writer.write("'$anchor' using 1:2 with circles lc rgb \"red\", \\");
            writer.newLine();

            writer.write("'$data' using 1:2 with points lw 8 lc rgb \"dark-green\"");
            writer.newLine();

        } catch (IOException e) {
            throw new OutputHandlerException("Ausgabedatei konnte nicht erstellt werden: " + e.getMessage());
        }
    }

    private void runGnuplot(String filename) throws IOException, InterruptedException {
        String filenameOnly = Paths.get(filename).getFileName().toString();
        String filepath = OUTPUT_PREFIX + filenameOnly + ".gnu";
        ProcessBuilder processBuilder = new ProcessBuilder("gnuplot", filepath);
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Gnuplot konnte nicht ausgeführt werden. Exit-Code: " + exitCode);
        }
    }
    private double formatiereLaenge(int hoehe) {
        return hoehe / 10.0;
    }
}
