package org.example.bachelorarbeit_fx;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.*;

import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;


public class Main extends Application {

    @FXML
    private Pane pane;

    int counter = 0;
    int size = 0;
    Map<Integer, Double> listOffBestSolutions = new HashMap<>();

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("application.fxml"));
        Parent root = loader.load();
        pane = (Pane) root;

        // Window
        double paneWidth = 600;
        double paneHeight = 600;
        pane.setPrefSize(paneWidth, paneHeight);

        // Values to calc circle
        double radius = 600 / 2 - 50;
        double centerX1 = 600 / 2;
        double centerX2 = paneHeight / 2;

        Circle circle = new Circle(centerX1, centerX2, radius);
        circle.setStroke(Color.BLACK);
        circle.setFill(null);

        stage.setScene(new Scene(root, paneWidth, paneHeight));

        // anzahl der instanzen
        int sampleSize = 0;
        // anzahl der roboter für das beispiel
        int numbRoboter = 0;
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader("/Users/basti/BA_FTP/instance/5_1000.txt"))){
            sampleSize = Integer.parseInt(bufferedReader.readLine());
            numbRoboter = Integer.parseInt(bufferedReader.readLine());

            size = numbRoboter;

            for(int i = 0; i < sampleSize; i++) {
                // lösche alles was bisher gemacht wurde
                pane.getChildren().clear();

                // lese die Winkel-Positionen der Roboter ein
                RobotMap initialRobots = new RobotMap();
                for(int j = 0; j < numbRoboter; j++) {
                    initialRobots.createRobot(Double.parseDouble(bufferedReader.readLine()));
                }

                StartingController controller = loader.getController();
                controller.setPane(pane);
                controller.start();

                Solve solve = new Solve();
                solve.setInitialRobotsAll(initialRobots);
                solve.run();



                RobotMap mapShortestSol = solve.getMapShortestSol();
                List<Paint> colorRobot = new ArrayList<>();
                List<Line> lines = new ArrayList<>();
                List<Paint> colorList = new ArrayList<>();
                for (Map.Entry<String, Robot> entry : mapShortestSol.robots.entrySet()) {
                    Robot robot = entry.getValue();
                    Paint stroke = robot.getColor();
                    colorList.add(stroke);

                    colorRobot.add(stroke);
                    List<Line> listeLines = robot.listeLines;
                    for (Line line : listeLines) {
                        line.setStroke(stroke);
                        line.setStrokeWidth(5);
                        lines.add(line);
                    }
                }

                // speicher die roboter in einer liste
                List<Robot> robots = new ArrayList<>();
                copyRobotsInList(initialRobots, robots);

                // roboter werden der szene hinzugefügt
                drawRobots(robots, colorRobot);

                //List<Line> lines = solve.listeLines;
                for(Line line : lines){
                    pane.getChildren().add(line);
                }

                double wc_zahl = solve.bestSol;
                if(wc_zahl >= 3.352){
                    listOffBestSolutions.put(counter, wc_zahl);
                }


                initialRobots.getAllRobots().stream().forEach(r -> {
                    Text roboPosition = new Text("" + Math.round((r.position.asAngel()*(-1000)) / 1000));

                    roboPosition.setTextAlignment(TextAlignment.LEFT);
                    roboPosition.setX(300 + (r.position.x * 250) + 10);
                    roboPosition.setY(300 + (r.position.y*250)+10);
                    roboPosition.setStyle("-fx-font-size: 20px; -fx-fill: black;");
                    pane.getChildren().add(roboPosition);
                });


                Text zahlText = new Text("WC Laenge: " + String.valueOf(wc_zahl));

                zahlText.setTextAlignment(TextAlignment.LEFT);
                zahlText.setX(pane.getWidth() - 590);
                zahlText.setY(pane.getHeight() - 10);
                zahlText.setStyle("-fx-font-size: 20px; -fx-fill: black;");

                // Füge den Text der Pane hinzu
                pane.getChildren().add(zahlText);

                pane.getChildren().add(circle);

                stage.setResizable(false);
                stage.setTitle("Freeze Tag Problem");
                //stage.setScene(new Scene(root, paneWidth, paneHeight));
                saveStageAsImage(stage);
                stage.show();
            }
        }catch (IOException e) {
            System.err.println("Fehler beim Lesen der Datei: " + e.getMessage());
        }
        System.out.println("ES GIBT " + listOffBestSolutions.size() + " Lösungen, die schlechter als die Gleichverteilung sind");
        for (Integer key : listOffBestSolutions.keySet()) {
            System.out.println(key+1);
        }
    }

    private static void copyRobotsInList(RobotMap initialRobots, List<Robot> robots) {
        robots.add(new Robot("99", true, 0, new ArrayList<>(), new ArrayList<>(),
                new Position(0, 0)));
        for (Map.Entry<String, Robot> entry : initialRobots.robots.entrySet()) {
            Robot robot = entry.getValue();
            robots.add(robot);
        }
    }

    private void drawRobots(List<Robot> robots, List<Paint> colorRobot) {
        int counter = 0;
        for (Robot robot : robots) {
            // Roboter der Szene hinzufügen
            // problem muss da liegen, wo colorList befüllt wird
            Robot r = new Robot(String.valueOf(counter), true, 0, new ArrayList<>(), new ArrayList<>(),
                    new Position(300 + (robot.position.x*250), 300+(robot.position.y*250)));
            //r.color = colorRobot.get(counter);
            r.setFill(colorRobot.get(counter));
            pane.getChildren().add(r);
            counter++;
        }
    }

    // Methodeum die Stage als Bild zu speichern
    private void saveStageAsImage(Stage stage) {
        counter++;
        WritableImage snapshot = stage.getScene().snapshot(null);

        BufferedImage bufferedImage = new BufferedImage((int) snapshot.getWidth(), (int) snapshot.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < snapshot.getWidth(); x++) {
            for (int y = 0; y < snapshot.getHeight(); y++) {
                bufferedImage.setRGB(x, y, snapshot.getPixelReader().getArgb(x, y));
            }
        }

        // Speichern des Bildes in eine Datei
        String desktopPath = System.getProperty("user.home") + "/Desktop/FTP Lösungen/5/" + size + "_" + counter + ".png";
        File file = new File(desktopPath);
        try {
            ImageIO.write(bufferedImage, "PNG", file);
            System.out.println("Bild erfolgreich gespeichert: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/*
best solutions
545
617
378
970
382
 */

/*
public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("application.fxml"));
        Parent root = loader.load();
        pane = (Pane) root;

        // Window
        double paneWidth = 600;
        double paneHeight = 600;
        pane.setPrefSize(paneWidth, paneHeight);

        // Values to calc circle
        double radius = paneWidth / 2 - 50;
        double centerX1 = paneWidth / 2;
        double centerX2 = paneHeight / 2;

        Circle circle = new Circle(centerX1, centerX2, radius);
        circle.setStroke(Color.BLACK);
        circle.setFill(null);

        // Scanner für Eingabe
        Scanner scanner = new Scanner(System.in);
        int anzahlRoboter = scanner.nextInt();
        size = anzahlRoboter;

        // lese die Winkel-Positionen der Roboter ein
        RobotMap initialRobots = new RobotMap();
        for(int i = 0; i < anzahlRoboter; i++) {
            initialRobots.createRobot(scanner.nextDouble());
        }

        StartingController controller = loader.getController();
        controller.setPane(pane);
        controller.start();

        Solve solve = new Solve();
        solve.setInitialRobotsAll(initialRobots);
        solve.run();



        RobotMap mapShortestSol = solve.getMapShortestSol();
        List<Paint> colorRobot = new ArrayList<>();
        List<Line> lines = new ArrayList<>();
        List<Paint> colorList = new ArrayList<>();
        for (Map.Entry<String, Robot> entry : mapShortestSol.robots.entrySet()) {
            Robot robot = entry.getValue();
            Paint stroke = robot.getColor();
            colorList.add(stroke);

            colorRobot.add(stroke);
            List<Line> listeLines = robot.listeLines;
            for (Line line : listeLines) {
                line.setStroke(stroke);
                line.setStrokeWidth(5);
                lines.add(line);
            }
        }

        // speicher die roboter in einer liste
        List<Robot> robots = new ArrayList<>();
        copyRobotsInList(initialRobots, robots);

        // roboter werden der szene hinzugefügt
        drawRobots(robots, colorRobot);

        //List<Line> lines = solve.listeLines;
        for(Line line : lines){
            pane.getChildren().add(line);
        }

        pane.getChildren().add(circle);

        stage.setResizable(false);
        stage.setTitle("Freeze Tag Problem");
        stage.setScene(new Scene(root, paneWidth, paneHeight));
        saveStageAsImage(stage);
        stage.show();
    }
 */