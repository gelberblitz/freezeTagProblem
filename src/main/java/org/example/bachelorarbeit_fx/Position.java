package org.example.bachelorarbeit_fx;

public class Position {
    double x;
    double y;

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Position(double winkel) {
        // deg to rad
        this.x = Math.cos(winkel*Math.PI/180.0);
        this.y = Math.sin(winkel*Math.PI/180.0);

        // auf 0 runden ...
        if (Math.abs(this.x)<0.0001) this.x = 0;
        if (Math.abs(this.y)<0.0001) this.y = 0;
    }


    // aktualisiere die Position des Roboters
    public double update(Position other) {
        double distance = distance(other);

        this.x = other.x;
        this.y = other.y;

        return distance;
    }

    protected double distance(Position other) {
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
    }

    public String toString() {
        return x+","+y;
    }

    // gibt die Position als Winkel aus
    public double asAngel() {
        double angle = Math.toDegrees(Math.atan2(y, x));
        return angle;
    }

    // Erstellt eine Position anhand des Winkels
    public void fromAngleToPosition(double angle) {
        // deg to rad
        this.x = Math.cos(angle*Math.PI/180.0);
        this.y = Math.sin(angle*Math.PI/180.0);

        // auf 0 runden ...
        if (Math.abs(this.x)<0.0001) this.x = 0;
        if (Math.abs(this.y)<0.0001) this.y = 0;
    }
}
