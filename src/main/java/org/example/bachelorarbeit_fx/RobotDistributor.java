package org.example.bachelorarbeit_fx;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RobotDistributor {

    // Klasse um eine Menge an Instanzen mit unterschiedlichen Verteilungen zu erstellen

    public static List<Double> gleichverteilung(int n, int sampleSize) {
        List<Double> angles = new ArrayList<>();
        double step = 360.0 / n;

        for(int j = 0; j < sampleSize; j++) {
            for (int i = 0; i < n; i++) {
                angles.add(i * step);
            }
        }

        return angles;
    }


    public static List<Double> zufallsverteilung(int n, int sampleSize) {
        List<Double> angles = new ArrayList<>();
        Random random = new Random();
        for(int x = 0; x < sampleSize; x++) {
            for(int j = 0; j < sampleSize; j++) {
                for (int i = 0; i < n; i++) {
                    angles.add(random.nextDouble() * 360);
                }
            }
        }

        return angles;
    }


    public static List<Double> clustering(int n, int clusters, int sampleSize) {
        List<Double> angles = new ArrayList<>();


        for(int x = 0; x < sampleSize; x++) {
            for (int i = 0; i < clusters; i++) {
                double center = Math.random() * 360;

                for (int j = 0; j < n / clusters; j++) {
                    double offset = Math.random() * 10 - 5;
                    angles.add((center + offset) % 360);
                }
            }
        }

        return angles;
    }

    public static List<Double> mathematischVerteilung(int n, int sampleSize) {
        List<Double> angles = new ArrayList<>();

        for(int j = 0; j < sampleSize; j++) {
            for (int i = 0; i < n; i++) {
                double angle = Math.sin(i * 2 * Math.PI / n) * 180 + 180;
                angles.add(angle);
            }
        }
        return angles;
    }

    public static List<Double> symmetrischeVerteilung(int n, int sampleSize) {
        List<Double> angles = new ArrayList<>();

        for(int j = 0; j < sampleSize; j++){
            angles.add(0.0);
            Random random = new Random();
            for (int i = 0; i < (n - 1) / 2; i++) {
                double angle = random.nextDouble() * 180;
                angles.add(angle);
                angles.add(-angle);
            }
            if ((n - 1) % 2 != 0) {
                double angle = random.nextDouble() * 180;
                angles.add(angle);
            }
        }

        return angles;
    }


    // erstelle x Instanzen mit den vorgegebenen Verteilungen
    public static void main(String[] args) {
        int n = 7;
        int overallSize = 301;
        int sampleSize = 12/4;

        List<Double> gleichVerteilteWinkel = gleichverteilung(n, 1);
        List<Double> zufallVerteilteWinkel = zufallsverteilung(n, sampleSize);
        List<Double> clusterVerteilteWinkel = clustering(n, 2, sampleSize);
        List<Double> symmetrischVerteilteWinkel = symmetrischeVerteilung(n, sampleSize);
        List<Double> mathematischVerteilteWinkel = mathematischVerteilung(n, sampleSize);

        String dateiName = System.getProperty("user.home") + "/Desktop/7_13.txt";

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dateiName)))){
            for(Double zahl : gleichVerteilteWinkel){
                writer.write(zahl.toString());
                writer.newLine();
            }

            for (Double v : symmetrischVerteilteWinkel) {
                writer.write(v.toString());
                writer.newLine();
            }

            for(Double zahl : zufallVerteilteWinkel){
                writer.write(zahl.toString());
                writer.newLine();
            }

            for(Double zahl : clusterVerteilteWinkel){
                writer.write(zahl.toString());
                writer.newLine();
            }

            for(Double zahl : mathematischVerteilteWinkel){
                writer.write(zahl.toString());
                writer.newLine();
            }
        }catch(IOException e){
            System.err.println("Fehler beim Laden der Datei " + dateiName + " " + e);
        }
    }
}
