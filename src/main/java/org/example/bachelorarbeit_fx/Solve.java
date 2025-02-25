package org.example.bachelorarbeit_fx;

import javafx.fxml.FXML;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

public class Solve {

    // ersten schritt zu robot 1 ?
    // macht nur bei "auf kreis gleichverteilt" sinn !
    static final boolean FIRST_STEP_ROBOT_0_TO_1 = true;
    static final boolean SHOW_ALTERNATIVE_SOLUTIONS = true;

    int statisticTotalMovesAnalyzed = 0;

    double bestSol = 0.0;

    List<Line> listeLines = new ArrayList<>();


    RobotMap initialRobotsAll;

    @FXML
    RobotMap mapShortestSol = new RobotMap();

    public static void main(String[] args){
        new Solve().run();
    }

    public void run()  {
        // testWinkel();

        // erstelle "Map" mit den Robos
        RobotMap map = setup();
        System.out.println("SOLVE SZENARIO:\n"+map.toString(""));

        long startTime = System.currentTimeMillis();

        mapShortestSol = solve(0, map, 0);

        showResult("Solution", mapShortestSol);

        System.out.println("Total moves analyzed: "+statisticTotalMovesAnalyzed+" in "+(System.currentTimeMillis()-startTime)+" ms");
    }

    public RobotMap getMapShortestSol() {
        return mapShortestSol;
    }

    public void setInitialRobotsAll(RobotMap initialRobotsAll) {
        this.initialRobotsAll = initialRobotsAll;
    }

    public RobotMap setup() {
        RobotMap map = new RobotMap();
        map.createInitialRobot();

        initialRobotsAll.getAllRobots().forEach(r -> {
            map.createRobot(r.position);
        });



        /*if (FIRST_STEP_ROBOT_0_TO_1)
        {
            map.move("", "0", "1"); // wenn alle auf kreis gleichverteilt -> nach 0 grad (1,0)
        }

         */
        return map;
    }

    public RobotMap solve(int level, RobotMap map, double currentLongestWay)
    {
        String prefix = sub(level);
        int nextLevel = level+1;

        Log.log("\n"+prefix+"SOLVE SZENARIO LEVEL "+level+":\n"+map.toString(prefix));

        // done ?
        if (map.isSolved()) return map;

        // brute force
        // - wie herum ist egal (erst die restlichen ziele oder erst die running)

        double shortestSolution = Double.MAX_VALUE;
        RobotMap bestSolution = null;

        // make move for each running robot
        for (Robot robot : map.getRunningRobots()) {
            // test every possible next target
            for (Robot target : map.getSleepingRobots()) {
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
                    // um es der scene hinzuzufügen
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
        bestSol = shortestSolution;

        return bestSolution;
    }

    protected String sub(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }

    public void showResult(String desc, RobotMap bestSolution) {
        System.out.println("\n"+desc+": "+bestSolution.getLongestMovedDistance(""));
        for (Robot robot : bestSolution.getAllRobots()) {
            System.out.println(robot.id+": "+robot.history+" => total distance: "+robot.distanceMoved);
        }

    }

    public void testWinkel() {
        System.out.println(new Position(0));
        System.out.println(new Position(90));
        System.out.println(new Position(180));
        System.out.println(new Position(270));
    }
}

// backup
/*
 public RobotMap solve(int level, RobotMap map, double currentLongestWay)
    {
        String prefix = sub(level);
        int nextLevel = level+1;

        Log.log("\n"+prefix+"SOLVE SZENARIO LEVEL "+level+":\n"+map.toString(prefix));

        // done ?
        if (map.isSolved()) return map;

        // brute force
        // - wie herum ist egal (erst die restlichen ziele oder erst die running)

        double shortestSolution = Double.MAX_VALUE;
        RobotMap bestSolution = null;

        // make move for each running robot
        for (Robot robot : map.getRunningRobots()) {
            // test every possible next target
            for (Robot target : map.getSleepingRobots()) {
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
 */

/*
public RobotMap solve(int level, RobotMap map, double currentLongestWay) {
        String prefix = sub(level);
        int nextLevel = level + 1;

        Log.log("\n" + prefix + "SOLVE SZENARIO LEVEL " + level + ":\n" + map.toString(prefix));

        // Wenn das Szenario gelöst ist, gebe die Lösung zurück
        if (map.isSolved()) {
            return map;
        }

        // ExecutorService für paralleles Arbeiten
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Diese Variablen werden verwendet, um das beste Ergebnis und den kürzesten Weg zu speichern
        final double[] shortestSolution = {Double.MAX_VALUE};
        final AtomicReference<RobotMap> bestSolution = new AtomicReference<>(null);

        // Sammle alle Tasks, die parallel ausgeführt werden sollen
        List<Future<Void>> futures = new ArrayList<>();

        // Über alle laufenden Roboter iterieren
        for (Robot robot : map.getRunningRobots()) {
            // Über alle schlafenden Roboter iterieren
            for (Robot target : map.getSleepingRobots()) {
                // Erstelle einen neuen Task für jede mögliche Roboterbewegung
                final RobotMap scenario = map.clone();
                futures.add(executor.submit(() -> {
                    // Bewege den Roboter und prüfe die neue Lösung
                    scenario.move(prefix, robot.id, target.id);

                    // Berechne den längsten Weg im neuen Szenario
                    double newLongestWay = scenario.getLongestMovedDistance(prefix);

                    // Wenn das Szenario bereits gelöst ist, speichere es
                    if (scenario.isSolved()) {
                        Log.log(prefix + "Test scenario: " + currentLongestWay + " -> " + newLongestWay + " Robot " + robot.id + " moves to " + target.id + " solves in " + newLongestWay);
                        synchronized (shortestSolution) {
                            if (newLongestWay < shortestSolution[0]) {
                                shortestSolution[0] = newLongestWay;
                                bestSolution.set(scenario);
                            }
                        }
                    } else {
                        // Rekursiv weiter lösen
                        if (newLongestWay < shortestSolution[0]) {
                            Log.log(prefix + "Test scenario: " + currentLongestWay + " -> " + newLongestWay + " Robot " + robot.id + " moves to " + target.id);
                            RobotMap solution = solve(nextLevel, scenario, newLongestWay);
                            synchronized (shortestSolution) {
                                if (solution.getLongestMovedDistance(prefix) < shortestSolution[0]) {
                                    shortestSolution[0] = solution.getLongestMovedDistance(prefix);
                                    bestSolution.set(solution);
                                }
                            }
                        }
                    }
                    return null; // Return value für den Task ist void
                }));
            }
        }

        // Warten, bis alle Tasks abgeschlossen sind
        try {
            // Warten, bis alle Futures abgeschlossen sind
            for (Future<Void> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        executor.shutdown(); // Executor Service stoppen, nachdem alle Tasks abgeschlossen sind

        // Das beste Szenario zurückgeben
        Log.log(prefix + "SZENARIO LEVEL " + level + " solved in " + shortestSolution[0] + "\n");

        return bestSolution.get();
    }
 */