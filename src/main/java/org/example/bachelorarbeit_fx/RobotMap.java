package org.example.bachelorarbeit_fx;

import java.util.*;

public class RobotMap {

    // alle Roboter
    Map<String, Robot> robots = new HashMap<>();

    // inaktive Roboter
    Map<String, Robot> sleeping = new HashMap<>();

    // aktive Roboter
    Map<String, Robot> running = new HashMap<>();

    // Erstelle Roboter im Nullpunkt
    public void createInitialRobot() {
        createRobot(0,0);
    }


    // Konstruktorkette zum Erstellen von Robotern
    public void createRobot(double x, double y) {
        // create robot at position with id and state (first running - all other sleeping)
        createRobot(new Position(x, y));
    }
    public void createRobot(double winkel) {
        createRobot(new Position(winkel));
    }

    public void createRobot(Position position) {
        add(new Robot(String.valueOf(robots.size()), robots.size() != 0, position));
    }

    public void add(Robot robot) {

        robots.put(robot.id, robot);

        if (robot.sleeping) {
            sleeping.put(robot.id, robot);
        } else {
            running.put(robot.id, robot);
        }
    }


    public Collection<Robot> getAllRobots() {
        return robots.values();
    }

    public Collection<Robot> getRunningRobots() {
        return running.values();
    }

    public Collection<Robot> getSleepingRobots() {
        return sleeping.values();
    }

    // wenn alle Roboter aktiviert wurden ist das Szenario gelöst
    public boolean isSolved() {
        return getSleepingRobots().isEmpty();
    }

    // laufe mit robotId zu targetId
    public void move(String robotId, String targetId) {

        Robot robot = robots.get(robotId);
        Robot target = robots.get(targetId);

        // laufe zu target
        robot.move(target);

        // aktiviere target
        target.awake(robot);

        // aktualisiere die Listen
        sleeping.remove(target.id);
        running.put(target.id, target);
    }

    // gebe aktuell zurückgelegte Distanz zurück
    public double getLongestMovedDistance() {
        double longtestDistance = 0.0;
        for (Robot robot : robots.values()) {
            // System.out.println(prefix+"  check longest move for robot: "+robot.id+" distance: "+robot.distanceMoved);
            if (robot.distanceMoved > longtestDistance) {
                longtestDistance = robot.distanceMoved;
            }
        }
        return longtestDistance;
    }

    public String toString(String prefix) {
        StringBuilder sb = new StringBuilder();
        for (Robot robot : robots.values()) {
            sb.append(prefix+robot+"\n");
        }
        return sb.toString();
    }

    //kopiere RobotMap
    public RobotMap clone() {
        RobotMap cloneMap = new RobotMap();
        Robot cloneRobot;
        for (Robot robot : robots.values()) {
            cloneRobot = robot.clone();
            cloneMap.add(cloneRobot);
            if (cloneRobot.sleeping) {
                cloneMap.sleeping.put(cloneRobot.id, cloneRobot);
            } else {
                cloneMap.running.put(cloneRobot.id, cloneRobot);
            }
        }
        return cloneMap;
    }

}
