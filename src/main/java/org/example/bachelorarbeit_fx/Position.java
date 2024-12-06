package org.example.bachelorarbeit_fx;

public class Position {
    private double x1;
    private double x2;

    public Position(double x1, double x2) {
        this.x1 = x1;
        this.x2 = x2;
    }

    public double getX1() {
        return x1;
    }

    public double getX2() {
        return x2;
    }

    public void setX2(double d) {
        this.x2 += d;
    }
    public void setX1(double v) {
        this.x1 += v;
    }
}
