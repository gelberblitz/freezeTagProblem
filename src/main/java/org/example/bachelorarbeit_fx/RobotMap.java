package org.example.bachelorarbeit_fx;

import java.util.*;

public class RobotMap {

    // all robots
    Map<String, Robot> robots = new HashMap<>();

    // copies:
    Map<String, Robot> sleeping = new HashMap<>();
    Map<String, Robot> running = new HashMap<>();

    public void createInitialRobot() {
        createRobot(0,0);
    }

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

    public void setRunningRobots(Map<String, Robot> robots) {
        running = robots;
    }

    public Collection<Robot> getSleepingRobots() {
        return sleeping.values();
    }

    public boolean isSolved() {
        return getSleepingRobots().isEmpty();
    }

    public void move(String robotId, String targetId) {

        Robot robot = robots.get(robotId);
        Robot target = robots.get(targetId);

        // robot move to target
        robot.move(target);

        // target awakes
        target.awake(robot);

        // update copies
        sleeping.remove(target.id);
        running.put(target.id, target);
    }

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


    public Map<String, Robot> listToMap(Collection<Robot> runningList) {
        Map<String, Robot> robots = new HashMap<>();
        for (Robot robot : runningList) {
            robots.put(robot.id, robot);
        }
        return robots;
    }
}
