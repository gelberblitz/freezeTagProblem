package org.example.bachelorarbeit_fx;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Roboter extends Circle {
    private Position position;

    private int id;
    private int idCounter = 1;
    private boolean state;
    private boolean isTargeted;

    public Position getLastPosition() {
        return lastPosition;
    }

    private double distance;

    private Position lastPosition;

    public Roboter(Position position, boolean state, boolean isTargeted) {
        super(position.getX1(), position.getX2(), 10);
        this.position = position;
        this.state = state;
        this.isTargeted = isTargeted;
        this.setFill(state ? Color.GREEN : Color.RED);
        this.distance = 0;
        this.lastPosition = new Position(position.getX1(), position.getX2());
        setIdCounter();
    }
    public boolean isTargeted() {
        return isTargeted;
    }

    public void setIdCounter(){
        this.id = idCounter;
        idCounter++;
    }

    public int getRoboterId(){
        return this.id;
    }

    public void setTargeted(boolean targeted) {
        isTargeted = targeted;
    }

    public void setLastPosition(Position lastPosition) {
        this.lastPosition = lastPosition;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public Node getStyleableNode() {
        return this;
    }
}