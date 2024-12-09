package org.example.bachelorarbeit_fx;

import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;

public class StartingController implements Initializable {

    @FXML
    List<Roboter> listeRoboter = new ArrayList<>();
    Pane pane;

    private boolean isPaused = false;
    private Button pauseButton;
    public void setPane(Pane pane) {
        this.pane = pane;
    }


    private void initializePauseButton() {
        pauseButton = new Button("pause");
        pauseButton.setOnAction(event -> {
            if (isPaused) {
                isPaused = false;
                pauseButton.setText("Pause");
            } else {
                isPaused = true;
                pauseButton.setText("Play");
            }
        });
    }

    @FXML
    public void pause(ActionEvent event) {
        if (isPaused) {
            isPaused = false;
            System.out.println("Fortsetzen!");
        } else {
            isPaused = true;
            System.out.println("Pause gedrückt!");
        }
    }


    private void waitForResume(Runnable action) {
        if (isPaused) {
            PauseTransition pause = new PauseTransition(Duration.millis(100));
            pause.setOnFinished(e -> waitForResume(action));
            pause.play();
        } else {
            action.run();
        }
    }


    @FXML
    protected void start(ActionEvent event) {

        System.out.println("Bitte geben Sie eine Anzahl an Robotern an, die erzeugt werden sollen:");

        Scanner scan = new Scanner(System.in);
        int numbRobots = scan.nextInt();

        System.out.println("Geben Sie die Koordinaten für genau " + numbRobots + " Roboter an");
        System.out.println("Das Format für die Koordinaten der Roboter ist double double, wobei folgendes gilt 0≤ WERT ≤1 :");

        double[][] robots = new double[numbRobots][2];
        String[] values;

        double centerX1 = pane.getWidth() / 2;
        double centerX2 = pane.getHeight() / 2;
        double radius = (pane.getWidth() / 2) - 50;

        ////////////////////// Start process
        scan.nextLine();
        // ERROR HANDELING FOR VALUES - RANGE BETWEEN/EQUALS 0 and 1
        for(int i = 0; i < numbRobots; i++){
            String input = scan.nextLine();

            values = input.split(" ");

            robots[i][0] = Double.parseDouble(values[0]);
            robots[i][1] = Double.parseDouble(values[1]);
        }

        for(int i = 0 ; i < numbRobots; i++){
            //??
            double x1 = centerX1 + robots[i][0] * radius;
            double x2 = centerX2 + robots[i][1] * radius;
            if(i == 0){
                listeRoboter.add(new Roboter(new Position(x1, x2), true, true));
            }else{
                listeRoboter.add(new Roboter(new Position(x1, x2), false, false));
            }
        }

        for (Roboter roboter : listeRoboter) {
            pane.getChildren().add(roboter);
        }
        // initialer Roboter der wach ist
        Roboter r1 = listeRoboter.get(0);

        // init Roboter soll ersten aufwecken und dann soll er und jeder
        // der zusätzlich aktiviert werden rekursiv andere aufwecken
        if(!listeRoboter.isEmpty()){
            //hier kann man auch andere Methoden zu suchen einfügen
            startDynamicSearch(r1);
        }else {
            System.out.println("Es wurden keine Roboter übergeben");
        }
    }

    private void startDynamicSearch(Roboter roboter) {
        Roboter nextRoboter = findNextInactiveRoboter(roboter);
        if(nextRoboter != null){
            listeRoboter.add(nextRoboter);
        }

        if(nextRoboter == null){
            // Sinnvoll?
            System.out.println("Es gab ein Problem bei der Suche für einen weiteren inaktiven Roboter");
            return;
        }

        double distX = nextRoboter.getPosition().getX1() - roboter.getPosition().getX1();
        double distY = nextRoboter.getPosition().getX2() - roboter.getPosition().getX2();
        double distance = Math.sqrt(distX * distX + distY * distY);

        roboter.setDistance(roboter.getDistance() + distance);
        Position pos = new Position(roboter.getPosition().getX1(), roboter.getPosition().getX2());
        roboter.setLastPosition(pos);

        //System.out.println("Roboter: " + roboter.getRoboterId() + "  Distanz: " + (roboter.getDistance() / 250) + "Einheiten");


        // Translate Action
        TranslateTransition translateTransition = new TranslateTransition();
        translateTransition.setNode(roboter);
        translateTransition.setDuration(Duration.millis(4000));
        translateTransition.setByX(distX);
        translateTransition.setByY(distY);

        translateTransition.setOnFinished(e -> {
            nextRoboter.setState(true);

            roboter.setPosition(nextRoboter.getPosition());


            for(Roboter r : listeRoboter){
                System.out.println("Roboter: " + r.getRoboterId() + " " + r.getDistance()/250);
            }

            waitForResume(() -> {
                startDynamicSearch(roboter);
                startDynamicSearch(nextRoboter);
            });

        });
        translateTransition.play();
    }

    private Roboter findNextInactiveRoboter(Roboter roboter) {
        Roboter nextRobot = null;
        double shortestDistance = Double.MAX_VALUE;
        for (Roboter robot : listeRoboter) {
            if (robot.getState() == false && robot != roboter && !robot.isTargeted()) {
                // suche den Roboter, der am kürzesten aktiv ist

                double distX = robot.getPosition().getX1() - roboter.getPosition().getX1();
                double distY = robot.getPosition().getX2() - roboter.getPosition().getX2();
                double euclideanDistance = Math.sqrt(distX * distX + distY * distY);

                if(euclideanDistance < shortestDistance){
                    shortestDistance = euclideanDistance;
                    nextRobot = robot;
                }
            }
        }
        if(nextRobot != null){
            nextRobot.setTargeted(true);
            return nextRobot;
        }else{
            return null;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

}

/*
0.0 0.0
1.0 0.0
0.0 1.0
-1.0 0.0
0.0 -1.0
 */

/*
0.0 0.0
-0.65 0.34
0.47 -0.48
-0.92 0.06
0.59 0.32
-0.45 -0.54
0.73 -0.12
-0.34 0.29
0.14 -0.85
-0.68 0.29
0.87 -0.10
-0.15 -0.72
0.46 0.36
-0.72 0.10
0.28 0.62
-0.58 -0.29
 */