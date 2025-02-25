package org.example.bachelorarbeit_fx;

import java.util.List;

public class MyThread implements Runnable {

    private List<Robot> list;
    private List<Robot> list2;
    int level;
    RobotMap map;
    double currentLongestWay;

    double shortestSolution = Double.MAX_VALUE;
    RobotMap bestSolution = null;


    static final boolean SHOW_ALTERNATIVE_SOLUTIONS = true;
    int statisticTotalMovesAnalyzed = 0;

    public MyThread(List<Robot> list1, List<Robot> list2, int level, RobotMap map, double currentLongestWay) {
        this.list = list1;
        this.list2 = list2;
    }

    // aufgabe aus solve
    @Override
    public void run() {
        solve(this.level, this.map, this.currentLongestWay);
    }


    public RobotMap solve(int Level, RobotMap map,double currentLongestWay){
        String prefix = sub(level);
        int nextLevel = level+1;

        for (Robot robot : list) {
            // test every possible next target
            for (Robot target : list2) {
                RobotMap scenario = map.clone();

                statisticTotalMovesAnalyzed++;

                scenario.move(prefix, robot.id, target.id);

                double newLongestWay = scenario.getLongestMovedDistance(prefix);

                RobotMap solution;

                if (scenario.isSolved()) {
                    Log.log(prefix + "Test scenario: "+currentLongestWay+" -> "+newLongestWay+" Robot " + robot.id + " moves to " + target.id+" solves in "+newLongestWay);
                    solution = scenario;
                } else {
                    // solve remaining
                    if (newLongestWay < shortestSolution) {
                        Log.log(prefix + "Test scenario: "+currentLongestWay+" -> "+newLongestWay+" Robot " + robot.id + " moves to " + target.id);
                        solution = solve(nextLevel, scenario, newLongestWay);
                    } else {
                        Log.log(prefix + "Skip scenario: "+newLongestWay+" cannot be shorter than current solution "+shortestSolution+": Robot " + robot.id + " moves to " + target.id);
                        continue;
                    }
                }

                double solutionLength = solution.getLongestMovedDistance(prefix);
                // System.out.println("\n"+prefix+"check solution: is "+solutionLength+" faster than "+shortestSolution);
                if (solutionLength < shortestSolution) {
                    shortestSolution = solutionLength;
                    bestSolution = solution;
                } else if (SHOW_ALTERNATIVE_SOLUTIONS && level==0 && Math.abs(solutionLength - shortestSolution)<0.1) {
                    // gleich/sehr ähnlich gute alternativläsung
                    showResult("Alternative solution", solution);
                    System.out.println();
                    System.out.println("Current Total Moves:");
                    System.out.println(statisticTotalMovesAnalyzed);
                }
            }
        }

        Log.log(prefix+"SZENARIO LEVEL "+level+" solved in "+shortestSolution+"\n");

        return bestSolution;
    }

    public void showResult(String desc, RobotMap bestSolution) {
        System.out.println("\n"+desc+": "+bestSolution.getLongestMovedDistance(""));
        for (Robot robot : bestSolution.getAllRobots()) {
            System.out.println(robot.id+": "+robot.history+" => total distance: "+robot.distanceMoved);
        }

    }

    protected String sub(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }
}
