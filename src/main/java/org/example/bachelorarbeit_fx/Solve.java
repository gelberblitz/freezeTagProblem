package org.example.bachelorarbeit_fx;

import javafx.fxml.FXML;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Solve {
    static final boolean SHOW_ALTERNATIVE_SOLUTIONS = true;

    // liste mit den worst-case-instanzen
    List<List<Double>> listeMitWCInstanzen = new ArrayList<>();
    int statisticTotalMovesAnalyzed = 0;

    // länge der besten Lösung
    double bestSol = 0.0;

    // robotmap mit der worst-cas-instanz
    RobotMap worstCaseInstance = new RobotMap();
    boolean longestDistanceFound = false;

    // liste mit den winkeln von der worst-case-instanz
    List<RobotMap> listOfWcInstances = new ArrayList<>();
    // initiale Roboter - ausgehende Instanz
    RobotMap initialRobotsAll;
    // liste mit den winkeln der instanzen die betrachtet wurden
    List<List<Double>> angleHistory = new ArrayList<>();
    @FXML
    RobotMap mapShortestSol = new RobotMap();
    private Stage stage;
    private RobotMap initialRobots;
    private Circle circle;
    private Pane pane;
    int x = 1;
    public static void main(String[] args){
        new Solve().run();
    }


    public void run()  {
        // testWinkel();

        // erstelle "Map" mit den Robos
        RobotMap map = setup();
        System.out.println("SOLVE SZENARIO:\n"+map.toString(""));

        long startTime = System.currentTimeMillis();

        mapShortestSol = solve(map, 0);

        showResult("Solution", mapShortestSol);

        System.out.println("Total moves analyzed: "+statisticTotalMovesAnalyzed+" in "+(System.currentTimeMillis()-startTime)+" ms");

        // suche den wortscase
        // speichert die beste Lösung für eine Instant
        RobotMap currentShortestSolution = mapShortestSol;
        RobotMap worstCase = searchWortsCaseScenario(currentShortestSolution, new RobotMap(), initialRobotsAll, new RobotMap(), -1);

        System.out.println("Längster Weg:" + worstCase.getLongestMovedDistance());

        // gebe historie aus
        showResult("Solution", worstCase);



        System.out.println("WC INSTANCE LÄNGE: " + worstCaseInstance.getLongestMovedDistance());

    }


    private RobotMap searchWortsCaseScenario(RobotMap currentShortestSolution, RobotMap previousShortestSolution,
                                             RobotMap initRobots, RobotMap prevPrevSolution,
                                             double preAngle) {

        //speicher die winkel der aktuellen lösung in aHist
        List<Double> aHist = new ArrayList<>();
        safeAngles(currentShortestSolution, initRobots, aHist);
        if(currentShortestSolution.getLongestMovedDistance() > 3.4161){
            listeMitWCInstanzen.add(aHist);
        }

        if(longestDistanceFound){
            return worstCaseInstance;
        }else if(!containsList(angleHistory, aHist)){
            preAngle = Math.round(preAngle);

            // fürs runden auf die nachkommastellen
            int DECIMAL_PLACES = 8;

            //Map um die Winkel zu ändern
            RobotMap rekMap = new RobotMap();

            //Map um die aktuelle Lösung zu prüfen
            RobotMap solvedMap = new RobotMap();

            // für test, ob es schon welche gibt
            List<Double> currAngles = new ArrayList<>();

            angleHistory.add(aHist);

            // speicher die aktuelle Lösung
            previousShortestSolution = currentShortestSolution;

            // alle Roboter aus aktueller Lösung, abwärts sorted
            List<Robot> allRobotsAsListSortedBackwards = currentShortestSolution.getAllRobots().stream()
                    .sorted(Comparator.comparingDouble(Robot::getDistanceMoved).reversed())
                    .toList();


            // zähle, ob 4 Roboter die gleiche Länge haben, wenn ja, dann GV bzw symmetrisch
            int counterForGv = 0;
            for (Robot r : allRobotsAsListSortedBackwards) {
                if (r.distanceMoved == currentShortestSolution.getLongestMovedDistance()) {
                    counterForGv++;
                }
            }

            // suche roboter der als letztes aktiviert wurde
            Robot maxDistanceRobot = new Robot("999", false, 0.0);
                for (Robot r : allRobotsAsListSortedBackwards) {
                    // sichergehen, dass es nicht einer von den anfänglichen robots ist, der den längsten geweckt hat
                    if (r.distanceMoved == currentShortestSolution.getLongestMovedDistance() && r.history.size() == 1) {
                        maxDistanceRobot = r;
                        break;
                    }
                }

                // suche aufweckkette vom längsten pfad
                boolean found = false;
                Robot x;
                // liste mit den robotern des längsten pfades
                List<Integer> historyOfBadestPath = new ArrayList<>();
                Robot currRob = maxDistanceRobot.clone();
                historyOfBadestPath.add(Integer.valueOf(currRob.id));

                // rückwärtssuche, bis initial aktiver roboter gefunden wurde
                while (!found) {
                    int idOfNext = Integer.parseInt(currRob.history.get(0).substring(16, 17));

                    x = findRobotById(currentShortestSolution, idOfNext);

                    if (!x.history.get(1).substring(14, 15)
                            .equals(currRob.id)) {
                        for (int i = x.history.size() - 2; i > 0; i--) {
                            historyOfBadestPath.add(Integer.valueOf(x.history.get(i).substring(14, 15)));
                        }
                    }

                    historyOfBadestPath.add(idOfNext);

                    currRob = findRobotById(currentShortestSolution, idOfNext);


                    if (String.valueOf(idOfNext).equals("0")) {
                        found = true;
                    }
                }

                //verschiebe den längsten roboter
                int counter = 0;
                if (initRobots.getAllRobots().size() + 1 == currentShortestSolution.getAllRobots().size()) {
                    rekMap.createInitialRobot();
                    counter = 1;
                }

                // kopiere die aktuelle map um verschiebungen vorzunehmen
                for (Robot r : initRobots.getAllRobots()) {
                    r.id = String.valueOf(counter);
                    counter++;
                    if (r.id.equals(maxDistanceRobot.id)) {
                        rekMap.createRobot(r.position.asAngel());
                    } else {
                        if (r.id.equals("0")) {
                            rekMap.createRobot(0.0, 0.0);
                        } else {
                            rekMap.createRobot(r.position.asAngel());
                        }
                    }
                }

                // alle roboter vom zuletzt aktivierten roboter bis zum initial aktive roboter auf dem längsten pfad werden verschoben
                for(int i = 0;  i < historyOfBadestPath.size()-1; i++) {
                    String currBadestRobotIndex = String.valueOf(historyOfBadestPath.get(i));

                    //verschiebe roboter um einen winkel in + richtung
                    Robot currBadestRobot = rekMap.getAllRobots().stream().toList().get(Integer.parseInt(currBadestRobotIndex));
                    currBadestRobot.position.fromAngleToPosition(currBadestRobot.position.asAngel() + 1);

                    // probiere verschiebung aus
                    solvedMap = solve(rekMap, 0);

                    // speicher die aktuellen positionen als winkel
                    for (Robot robot : rekMap.getAllRobots().stream().toList()) {
                        currAngles.add((double) Math.round(robot.position.asAngel()));
                    }

                    double solvedMapSolutionLength = Math.round(solvedMap.getLongestMovedDistance() * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
                    double currShortestSolution = Math.round(previousShortestSolution.getLongestMovedDistance() * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
                    // wenn die aufweckzeit mehr geworden ist, starte rekursiven aufruf
                    if (solvedMapSolutionLength >= currShortestSolution
                            && Math.round(rekMap.getAllRobots().stream().toList().get(Integer.parseInt(currBadestRobot.id)).position.asAngel()) != preAngle
                            && !containsList(angleHistory, currAngles)) {
                        // probiere nochmal
                        prevPrevSolution = previousShortestSolution.clone();
                        previousShortestSolution = solvedMap.clone();
                        // angel -1 weil hier der geänderte winkel gesafed wird
                        solvedMap = searchWortsCaseScenario(solvedMap, previousShortestSolution, rekMap, prevPrevSolution, currBadestRobot.position.asAngel() - 1);
                    } else {
                        currAngles.clear();
                        // +1 geht nicht, geht -1?
                        for (Robot r2 : rekMap.getAllRobots()) {
                            if (r2.id.equals(currBadestRobot.id)) {
                                r2.position.fromAngleToPosition(r2.position.asAngel() - 2);
                            }
                        }

                        // probiere verschiebung aus
                        solvedMap = solve(rekMap, 0);

                        // speicher die aktuellen positionen als winkel
                        for (Robot robot : rekMap.getAllRobots().stream().toList()) {
                            currAngles.add((double) Math.round(robot.position.asAngel()));
                        }

                        solvedMapSolutionLength = Math.round(solvedMap.getLongestMovedDistance() * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
                        double prevShortestSolution = Math.round(previousShortestSolution.getLongestMovedDistance() * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
                        // wenn die aufweckzeit mehr geworden ist, starte rekursiven aufruf
                        if (solvedMapSolutionLength >= prevShortestSolution
                                && Math.round(rekMap.getAllRobots().stream().toList().get(Integer.parseInt(currBadestRobot.id)).position.asAngel()) != preAngle
                                && !containsList(angleHistory, currAngles)) {
                            // probiere nochmal
                            prevPrevSolution = previousShortestSolution.clone();
                            previousShortestSolution = solvedMap.clone();
                            // angel +2 weil hier der geänderte winkel gesafed wird
                            solvedMap = searchWortsCaseScenario(solvedMap, previousShortestSolution, rekMap, prevPrevSolution, currBadestRobot.position.asAngel());
                        } else {
                            currAngles.clear();
                            // setze den wert wieder zurück
                            resetRekMap(rekMap, currBadestRobot.id);
                        }
                    }

                }

                // speicher die worst-case instanz wenn das szenario schlechter geworden ist
            double wcDist = Math.round(worstCaseInstance.getLongestMovedDistance() * Math.pow(10, 10)) / Math.pow(10, 10);
            double currDist = Math.round(currentShortestSolution.getLongestMovedDistance() * Math.pow(10, 10)) / Math.pow(10, 10);
            if (wcDist <= currDist) {
                worstCaseInstance = currentShortestSolution.clone();
                listOfWcInstances.add(worstCaseInstance);
            } else {
                longestDistanceFound = true;
            }
        }
        return currentShortestSolution;
    }


    private Robot findRobotById(RobotMap map, int idOfNext) {
        List<Robot> l = map.getAllRobots().stream().toList();

        for (Robot robot : l) {
            if(robot.id.equals(String.valueOf(idOfNext))){
                return robot;
            }
        }
        return null;
    }

// setzte verschiebungen zurück
    private static void resetRekMap(RobotMap rekMap, String rootRobot) {
        Robot robot = rekMap.getAllRobots().stream()
                .toList()
                .get(Integer.parseInt(rootRobot));
        robot.position.fromAngleToPosition(robot.position.asAngel()+1.0);
    }

    // speicher winkel in liste
    private static void safeAngles(RobotMap currentShortestSolution, RobotMap initRobots, List<Double> aHist) {
        if(initRobots.getAllRobots().size() < currentShortestSolution.getAllRobots().size()){
            aHist.add(0.0);
        }
        for (Robot currRobot : initRobots.getAllRobots()) {
            aHist.add(Math.round(currRobot.position.asAngel() * Math.pow(10, 0)) / Math.pow(10, 0));
        }
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

        return map;
    }

    public RobotMap solve(RobotMap map, double currentLongestWay)
    {

        // fertig ?
        if (map.isSolved()) return map;

        // brute force
        double shortestSolution = Double.MAX_VALUE;
        RobotMap bestSolution = null;

        // probiere jede möglichkeit
        for (Robot robot : map.getRunningRobots()) {
            int counter = 0;
            for (Robot target : map.getSleepingRobots()) {
                // erstelle Szenario
                RobotMap scenario = map.clone();

                statisticTotalMovesAnalyzed++;


                scenario.move(robot.id, target.id);

                double newLongestWay = scenario.getLongestMovedDistance();

                RobotMap solution;

                // wenn alle aktiv -> beende
                if (scenario.isSolved()) {
                    solution = scenario;
                    // wenn nicht alle aktiv sind, aktiviere die restlichen
                } else {
                    int DECIMAL_PLACES = 5;
                    newLongestWay = Math.round(newLongestWay * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
                    shortestSolution =  Math.round(shortestSolution * 100000.0) / 100000.0;

                    // wenn der weg kürzer ist als die bisher kürzeste lösung
                    if (newLongestWay < shortestSolution) {
                        // starte rekursiven aufruf
                        solution = solve(scenario, newLongestWay);
                    } else {
                        continue;
                    }
                }

                double solutionLength = solution.getLongestMovedDistance();
                int DECIMAL_PLACES = 5;

                // wenn lösung besser als bisher bekannte, speichern als beste lösung
                solutionLength = Math.round(solutionLength * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
                shortestSolution = Math.round(shortestSolution * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
                if (solutionLength < shortestSolution) {
                    shortestSolution = solutionLength;
                    bestSolution = solution;
                }
            }
        }

        // speicher die beste lösung
        bestSol = shortestSolution;

        return bestSolution;
    }

    // lösungsweg für das aktivieren anzeigen
    public void showResult(String desc, RobotMap bestSolution) {
        System.out.println("\n"+desc+": "+bestSolution.getLongestMovedDistance());
        for (Robot robot : bestSolution.getAllRobots()) {
            System.out.println(robot.id+": "+robot.history+" => total distance: "+robot.distanceMoved);
        }

    }

    // um in Main das Bild zu malen
    public void setForImage(Stage stage, RobotMap initialRobots, Circle circle, Pane pane) {
        this.stage = stage;
        this.initialRobots = initialRobots;
        this.circle = circle;
        this.pane = pane;
    }

    // prüfe ob winkel in der history so schon mal vorkamen
    public static boolean containsList(List<List<Double>> angleHistory, List<Double> aHist) {
        for (List<Double> list : angleHistory) {
            if (list.size() == aHist.size() && list.equals(aHist)) {
                return true;
            }
        }
        return false;
    }

}