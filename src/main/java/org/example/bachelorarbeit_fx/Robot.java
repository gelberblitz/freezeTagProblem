package org.example.bachelorarbeit_fx;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.ArrayList;
public class Robot extends Circle
{
    String id;
    Paint color;
    ArrayList<Line> listeLines;
    Position position;
    boolean sleeping;
    double distanceMoved;

    public double getDistanceMoved() {
        return distanceMoved;
    }

    // remember movements
    ArrayList<String> history;

    public Robot(String id, boolean sleeping, Position position) {
        this(id, sleeping, 0.0, new ArrayList<>(), new ArrayList<>(), position);
        if (!sleeping) history.add("Initial running robot");

    }

    public Robot(String id, boolean sleeping, double winkel) {
        this(id, sleeping, 0.0, new ArrayList<>(), new ArrayList<>() ,new Position(winkel));
        if (!sleeping) history.add("Initial running robot");
    }

    public Robot(String id, boolean sleeping, double distanceMoved ,ArrayList<String> history, ArrayList<Line> listeLines ,Position position) {
        super(position.x, position.y, 8);
        this.position = new Position(position.x, position.y);
        this.id = id;
        this.sleeping = sleeping;
        this.distanceMoved = distanceMoved;
        this.history = history;
        this.listeLines = listeLines;

        color = getRandomColor();
        setFill(color);
    }

    public Paint getColor(){
        return this.color;
    }

    private Color getRandomColor() {
        double red = Math.random();
        double green = Math.random();
        double blue = Math.random();
        return Color.color(red, green, blue);
    }

    // methode um einen Roboter zu aktivieren und speichert die ID von dem Roboter, von dem er aktiviert wurde
    public void awake(Robot mover) {
        this.sleeping = false;
        // robot being awaked by another robot start with its distance
        this.distanceMoved = mover.distanceMoved;

        history.add("Awaked by robot "+mover.id+" with distance "+mover.distanceMoved);
    }

    // Roboter läuft von aktueller Position zu other
    public void move(Robot other) {
        // update position and distance
        Line line = new Line();
        line.setStartX(300+(this.position.x*250));
        line.setStartY(300+(this.position.y*250));
        line.setEndX(300+(other.position.x*250));
        line.setEndY(300+(other.position.y*250));

        line.setFill(this.color);
        line.setStrokeWidth(5);
        listeLines.add(line);

        double distance = this.position.update(other.position);
        history.add("Move to robot "+other.id+" with distance "+distance);
        distanceMoved += distance;
    }


    public String toString() {
        return "Robot " +
                "id='" + id + '\'' +
                ", sleeping=" + sleeping +
                ", x=" + position.x +
                ", y=" + position.y +
                " distanceMoved=" + distanceMoved;
    }

    // kopiert den Roboter
    public Robot clone() {
        return new Robot(this.id, this.sleeping, this.distanceMoved, (ArrayList<String>)history.clone(), (ArrayList<Line>)listeLines.clone(),
                new Position(position.x, position.y));
    }


}
