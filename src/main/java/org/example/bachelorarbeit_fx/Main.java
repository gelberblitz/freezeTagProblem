package org.example.bachelorarbeit_fx;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Main extends Application {

    @FXML
    List<Roboter> listeRoboter = new ArrayList<>();

    private Pane pane;



    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("application.fxml"));
        Parent root = loader.load();
        pane = (Pane) root;

        StartingController controller = loader.getController();

        // Window
        double paneWidth = 600;
        double paneHeight = 600;
        pane.setPrefSize(paneWidth, paneHeight);

        // Values to calc circle
        //??
        double radius = paneWidth / 2 - 50;
        double centerX1 = paneWidth / 2;
        double centerX2 = paneHeight / 2;

        Circle circle = new Circle(centerX1, centerX2, radius);
        circle.setStroke(Color.BLACK);
        circle.setFill(null);

        pane.getChildren().add(circle);
        controller.setPane(pane);



        stage.setResizable(false);
        stage.setTitle("Freeze Tag Problem");
        stage.setScene(new Scene(root, paneWidth, paneHeight));
        stage.show();
    }
}