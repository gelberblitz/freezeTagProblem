package org.example.bachelorarbeit_fx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.effect.Light;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.*;

public class StartingController implements Initializable {

    @FXML
    RobotMap mapShortestSol = new RobotMap();

    @FXML
    Pane pane;

    public void setPane(Pane pane) {
        this.pane = pane;
    }

    @FXML
    protected void start() {

        /*double centerX1 = pane.getWidth() / 2;
        double centerX2 = pane.getHeight() / 2;
        double radius = (pane.getWidth() / 2) - 50;

         */

        ////////////////////// Start process
        /*Solve solve = new Solve();
        solve.run();

        mapShortestSol = solve.getMapShortestSol();


        List<Robot> robots = new ArrayList<>();

        // maße des einheitskreises
        double centerX1 = 300;
        double centerX2 = 300;
        double radius = 250;

        // speicher die roboter in einer liste
        for (Map.Entry<String, Robot> entry : mapShortestSol.robots.entrySet()) {
            Robot robot = entry.getValue();
            robots.add(robot);
        }

        // roboter werden der szene hinzugefügt
        for (Robot robot : robots) {
            // Berechne neue Position basierend auf dem Einheitskreis
           /* double x1 = (centerX1 + robot.position.x * radius);
            double x2 = (centerX2 + robot.position.y * radius);

            // Debug-Ausgabe der Position
            System.out.println("Roboter " + robot.id + " Position vorher: (" + x1 + ", " + x2 + ")");

            // Update der Robot-Position
            Robot r = robot;
            r.updatePosition(new Position(x1, x2));

            // Roboter der Szene hinzufügen
            pane.getChildren().add(r);

            */
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



0.0 0.0
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



// Translate Action
        TranslateTransition translateTransition = new TranslateTransition();
        translateTransition.setNode(roboter);
        translateTransition.setDuration(Duration.millis(4000));
        translateTransition.setByX(distX);
        translateTransition.setByY(distY);
 */