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
    List<Double> angleFromWorstCase = new ArrayList<>();

    double bestSol = 0.0;
    double bestSol2 = 0.0;
    RobotMap wc = new RobotMap();

    List<Line> listeLines = new ArrayList<>();


    RobotMap initialRobotsAll;

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

        mapShortestSol = solve(0, map, 0);

        showResult("Solution", mapShortestSol);

        System.out.println("Total moves analyzed: "+statisticTotalMovesAnalyzed+" in "+(System.currentTimeMillis()-startTime)+" ms");


        // suche den wortscase
        // speichert die beste Lösung für eine Instant
        RobotMap currentShortestSolution = mapShortestSol;
        RobotMap worstCase = searchWortsCaseSzenario(currentShortestSolution, new RobotMap(), initialRobotsAll, new RobotMap());

        System.out.println("Längster Weg:" + worstCase.getLongestMovedDistance());
        Collection<Robot> rall = worstCase.getAllRobots();
        Robot robot = null;
        for (Robot r : rall) {
            System.out.println("Roboter " + r.id  + " Winkel: " + r.position.asAngel());
            if (r.id.equals("3")){
                robot = r;
            }
        }
        System.out.println("Winkel sollte 154 oder 155 sein" + robot.position.asAngel());

        showResult("Solution", worstCase);


        System.out.println("Liste der WINKEL:");
        angleFromWorstCase.forEach(System.out::println);
    }

    private RobotMap searchWortsCaseSzenario(RobotMap currentShortestSolution, RobotMap previousShortestSolution, RobotMap initRobots, RobotMap prevPrevSolution) {
        int DECIMAL_PLACES = 5;
        int DECIMAL_PLACES2 = 9;
        Collection<Robot> allRobots = currentShortestSolution.getAllRobots();
        previousShortestSolution = currentShortestSolution.clone();
        RobotMap worstCase = new RobotMap();
        RobotMap solvedMap = new RobotMap();
        Robot rMaxDistance = null;
        RobotMap rekMap = new RobotMap();

        // prüfe ob Loop, bzw ob es die Anordnung der Winkel schon mal gab

        //speicher die winkel der aktuellen lösung in aHist
        List<Double> aHist = new ArrayList<>();
        safeAngles(currentShortestSolution, initRobots, aHist);

        if(containsList(angleHistory, aHist)){
            //LOOP ist eingetreten
            //brauche Root für die Verschiebung einer Loop
            String rootRobot = currentShortestSolution.getAllRobots().stream()
                    .toList()
                    .get(0)
                    .history
                    .get(1).substring(14, 15);

            int counter = 0;
            if(initRobots.getAllRobots().size()+1 == currentShortestSolution.getAllRobots().size()){
                rekMap.createInitialRobot();
                counter = 1;
            }
            for(Robot r : initRobots.getAllRobots()) {
                r.id = String.valueOf(counter);
                counter++;
                if(r.id.equals(rootRobot)) {
                    // nur zum anschauen
                    double soutt = r.position.asAngel();
                    soutt++;
                    rekMap.createRobot(r.position.asAngel()+1.0);
                    System.out.println(r.position.asAngel());
                }else{
                    if (r.id.equals("0")) {
                        rekMap.createRobot(0.0, 0.0);
                    }else{
                        rekMap.createRobot(r.position.asAngel());
                    }
                    System.out.println(r.position.asAngel());
                }
            }

            solvedMap = solve(0, rekMap, 0);

            double solvedMapSolutionLength = Math.round(solvedMap.getLongestMovedDistance() * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
            double currShortestSolution = Math.round(currentShortestSolution.getLongestMovedDistance() * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
            if(solvedMapSolutionLength > currShortestSolution) {
                // probiere nochmal
                prevPrevSolution = previousShortestSolution.clone();
                previousShortestSolution = solvedMap.clone();
                solvedMap = searchWortsCaseSzenario(solvedMap, previousShortestSolution, rekMap, prevPrevSolution);

            } else {
                //previousShortestSolution = solvedMap.clone();
                for (Robot r : rekMap.getAllRobots()) {
                    if(r.id.equals(rootRobot)) {
                        double soutt = r.position.asAngel();
                        soutt-=2;
                        r.position.fromAngleToPosition(r.position.asAngel()-2);
                    }
                }

                solvedMap = solve(0, rekMap, 0);

                solvedMapSolutionLength = Math.round(solvedMap.getLongestMovedDistance() * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
                double prevShortestSolution = Math.round(previousShortestSolution.getLongestMovedDistance() * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
                if(solvedMapSolutionLength > prevShortestSolution) {
                    // probiere nochmal
                    prevPrevSolution = previousShortestSolution.clone();
                    previousShortestSolution = solvedMap.clone();
                    solvedMap = searchWortsCaseSzenario(solvedMap, previousShortestSolution, rekMap, prevPrevSolution);
                }else{
                    // setze den wert wieder zurück
                    resetRekMap(rekMap, rootRobot);

                    // solvedMap ist wenn -2 augrund von +1 nicht geht, hier wieder auf dem urzustand
                    solvedMap = solve(0, rekMap, 0);
                }
            }

        }
        angleHistory.add(aHist);



        // suche längsten Weg & Roboter
        List<Robot> list = allRobots.stream()
                .sorted(Comparator.comparingDouble(Robot::getDistanceMoved).reversed())
                .toList();


        // falsch -> wenn 0 nicht den weitestens gefunden hat, dann ist das ein problem
        if(list.get(0).id.equals("0")){
            rMaxDistance = list.get(1);
        }else{
            if(list.get(0).history.size() < list.get(1).history.size()){
                rMaxDistance = list.get(0);
            }else{
                rMaxDistance = list.get(1);
            }
        }
        //rMaxDistance = (list.get(0).id.equals("0")) ? list.get(1) : list.get(0);

        if(rMaxDistance != null) {
            // probiere 1. Verschiebung so oft wie es schlechter wird (beide richtungen prüfen)
            //hole Position als Winkel und inkrementiere, bzw reduziere
            //löse die verschiebung

            int counter = 0;

            if(initRobots.getAllRobots().size()+1 == currentShortestSolution.getAllRobots().size()){
                rekMap.createInitialRobot();
                counter = 1;
            }

            System.out.println("DAS SIND DIE WINKEL DER INITALEN MAP");
            for(Robot r : initRobots.getAllRobots()) {
                r.id = String.valueOf(counter);
                counter++;
                if(r.id.equals(rMaxDistance.id)) {
                    // nur zum anschauen
                    double soutt = r.position.asAngel();
                    soutt++;
                    rekMap.createRobot(r.position.asAngel()+1.0);
                    System.out.println(r.position.asAngel());
                }else{
                    if (r.id.equals("0")) {
                        rekMap.createRobot(0.0, 0.0);
                    }else{
                        rekMap.createRobot(r.position.asAngel());
                    }
                    System.out.println(r.position.asAngel());
                }
            }

            //check
            List<Double> l = new ArrayList<>();
            rekMap.getAllRobots().forEach(robot -> {
                l.add(robot.position.asAngel());
            });
            solvedMap = solve(0, rekMap, 0);

            Robot maxDisSolved1 = (solvedMap.getAllRobots().stream().sorted(Comparator.comparingDouble(Robot::getDistanceMoved).reversed()).toList().get(0).id.equals("0"))
                    ? solvedMap.getAllRobots().stream().sorted(Comparator.comparingDouble(Robot::getDistanceMoved).reversed()).toList().get(1)
                    : solvedMap.getAllRobots().stream().sorted(Comparator.comparingDouble(Robot::getDistanceMoved).reversed()).toList().get(0);

            Robot maxDisPrevPrev1;
            if (!prevPrevSolution.getAllRobots().isEmpty()) {
                maxDisPrevPrev1 = (prevPrevSolution.getAllRobots().stream().sorted(Comparator.comparingDouble(Robot::getDistanceMoved).reversed()).toList().get(0).id.equals("0"))
                        ? prevPrevSolution.getAllRobots().stream().sorted(Comparator.comparingDouble(Robot::getDistanceMoved).reversed()).toList().get(1)
                        : prevPrevSolution.getAllRobots().stream().sorted(Comparator.comparingDouble(Robot::getDistanceMoved).reversed()).toList().get(0);
            }else{
                maxDisPrevPrev1 = new Robot("9999", false, 0);
            }

            // runde um fehler zu vermeiden
            boolean loop1 = isLoop(maxDisSolved1, DECIMAL_PLACES2, maxDisPrevPrev1);

            double solvedMapSolutionLength = Math.round(solvedMap.getLongestMovedDistance() * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
            double currShortestSolution = Math.round(currentShortestSolution.getLongestMovedDistance() * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
            if(solvedMapSolutionLength >= currShortestSolution && !loop1) {
                // probiere nochmal
                prevPrevSolution = previousShortestSolution.clone();
                previousShortestSolution = solvedMap.clone();
                solvedMap = searchWortsCaseSzenario(solvedMap, previousShortestSolution, rekMap, prevPrevSolution);

            } else {
                //previousShortestSolution = solvedMap.clone();
                for (Robot r : rekMap.getAllRobots()) {
                    if(r.id.equals(rMaxDistance.id)) {
                        double soutt = r.position.asAngel();
                        soutt-=2;
                        r.position.fromAngleToPosition(r.position.asAngel()-2);
                    }
                }

                solvedMap = solve(0, rekMap, 0);
                // todo prüfen, ob der roboter aus der prevprevSolution an der gleichen position mit der gleichen aufweckzeit wie solvedMap ist
                // -> jz probieren // eigentlich will man hier den gerade geänderten haben?
                //Robot newMaxDisSolved = rMaxDistance.clone();
                //newMaxDisSolved.position.fromAngleToPosition((rekMap.getAllRobots().stream().toList().get(Integer.parseInt(rMaxDistance.id)).position.asAngel()-2));
                Robot maxDisSolved = rekMap.getAllRobots().stream().toList().get(Integer.parseInt(rMaxDistance.id));

                Robot maxDisPrevPrev;
                if (!prevPrevSolution.getAllRobots().isEmpty()) {
                    maxDisPrevPrev = (prevPrevSolution.getAllRobots().stream().sorted(Comparator.comparingDouble(Robot::getDistanceMoved).reversed()).toList().get(0).id.equals("0"))
                            ? prevPrevSolution.getAllRobots().stream().sorted(Comparator.comparingDouble(Robot::getDistanceMoved).reversed()).toList().get(1)
                            : prevPrevSolution.getAllRobots().stream().sorted(Comparator.comparingDouble(Robot::getDistanceMoved).reversed()).toList().get(0);
                }else{
                    maxDisPrevPrev = new Robot("9999", false, 0);
                }


                boolean loop = isLoop(maxDisSolved, DECIMAL_PLACES2, maxDisPrevPrev);

                solvedMapSolutionLength = Math.round(solvedMap.getLongestMovedDistance() * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
                double prevShortestSolution = Math.round(previousShortestSolution.getLongestMovedDistance() * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
                if(solvedMapSolutionLength >= prevShortestSolution && !loop) {
                    // probiere nochmal
                    prevPrevSolution = previousShortestSolution.clone();
                    previousShortestSolution = solvedMap.clone();
                    solvedMap = searchWortsCaseSzenario(solvedMap, previousShortestSolution, rekMap, prevPrevSolution);
                }else{
                    // setze den wert wieder zurück
                    resetRekMap(rekMap, rMaxDistance.id);

                    // solvedMap ist wenn -2 augrund von +1 nicht geht, hier wieder auf dem urzustand
                    solvedMap = solve(0, rekMap, 0);
                    //prevPrevSolution = previousShortestSolution.clone();
                    //previousShortestSolution = solvedMap.clone();
                }
            }


            // wenn nicht besser, dann probiere 2. Verschiebung so oft wie es schlechter wird (beide richtungen prüfen)
            //TODO: suche den nächstkleineren Roboter, der rMaxDistance aufgeweckt hat aus der Liste list
            List<Robot> list2 = solvedMap.getAllRobots().stream()
                    .sorted(Comparator.comparingDouble(Robot::getDistanceMoved).reversed())
                    .toList();

            Robot rMaxDistance2_2 = (list2.get(0).id.equals("0")) ? list2.get(1) : list2.get(0);
            
            String indexOfRobot = rMaxDistance2_2.history.get(0).substring(16, 17);
            String indexOfSecondRobot = "";
            int cnter = 0;
            for (Robot robot : list2) {
                if(robot.id.equals(indexOfRobot)) {
                    indexOfSecondRobot = robot.history.get(list2.get(cnter).history.size() - 2).substring(14, 15);
                }
                cnter++;
            }

            // todo änder den winkel von indexofsecondRobot +1 ind -1 bis es schlechter wird
            Robot newLongestRobot = rekMap.getAllRobots()
                    .stream()
                    .toList()
                    .get(Integer.parseInt(indexOfSecondRobot));

            newLongestRobot.position.fromAngleToPosition(newLongestRobot.position.asAngel()+1.0);
            //löse die verschiebung
            solvedMap = solve(0, rekMap, 0);

            solvedMapSolutionLength = Math.round(solvedMap.getLongestMovedDistance() * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
            double prevShortestSolution = Math.round(previousShortestSolution.getLongestMovedDistance() * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
            if(solvedMapSolutionLength >= prevShortestSolution){
                // probiere nochmal
                prevPrevSolution = previousShortestSolution.clone();
                previousShortestSolution = solvedMap.clone();
                solvedMap = searchWortsCaseSzenario(solvedMap, previousShortestSolution, rekMap, prevPrevSolution);

            }else {
                for (Robot r : rekMap.getAllRobots()) {
                    if(r.id.equals(newLongestRobot.id)) {
                        double soutt = r.position.asAngel();
                        r.position.fromAngleToPosition(r.position.asAngel()-2);
                    }
                }

                solvedMap = solve(0, rekMap, 0);

                solvedMapSolutionLength = Math.round(solvedMap.getLongestMovedDistance() * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
                prevShortestSolution = Math.round(previousShortestSolution.getLongestMovedDistance() * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
                if(solvedMapSolutionLength >= prevShortestSolution){
                    // probiere nochmal
                    prevPrevSolution = previousShortestSolution.clone();
                    previousShortestSolution = solvedMap.clone();
                    solvedMap = searchWortsCaseSzenario(solvedMap, previousShortestSolution, rekMap, prevPrevSolution);
                }else{
                    // setze den wert wieder zurück
                    resetRekMap(rekMap, newLongestRobot.id);

                    // solvedMap ist wenn -2 augrund von +1 nicht geht, hier wieder auf dem urzustand
                    solvedMap = solve(0, rekMap, 0);
                    //previousShortestSolution = solvedMap.clone();
                }
            }

            // wenn nicht schlechter, probiere 3. Verschiebung so oft wie es schlechter wird (beide richtungen prüfen)
            List<String> historyOfThird = list2.get(Integer.parseInt(indexOfRobot)).history;
            int indexOThird = ((list2.get(Integer.parseInt(indexOfRobot)).history.size() - 3) > 0) ? (list2.get(Integer.parseInt(indexOfRobot)).history.size() - 3) : 0;
            String historyOfThirdRob = historyOfThird
                    .get(indexOThird);

            String indexOfThirdRobot = "";
            if(historyOfThirdRob.contains("Awaked")){
                indexOfThirdRobot = historyOfThirdRob.substring(16, 17);
            }else{indexOfThirdRobot = historyOfThirdRob.substring(14, 15);}


            // todo änder den winkel von indexofThirdRobot +1 ind -1 bis es schlechter wird
            newLongestRobot = rekMap.getAllRobots()
                    .stream()
                    .toList()
                    .get(Integer.parseInt(indexOfThirdRobot));

            newLongestRobot.position.fromAngleToPosition(newLongestRobot.position.asAngel()+1.0);
            //löse die verschiebung
            solvedMap = solve(0, rekMap, 0);

            solvedMapSolutionLength = Math.round(solvedMap.getLongestMovedDistance() * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
            prevShortestSolution = Math.round(previousShortestSolution.getLongestMovedDistance() * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
            if(solvedMapSolutionLength >= prevShortestSolution){
                // probiere nochmal
                prevPrevSolution = previousShortestSolution.clone();
                previousShortestSolution = solvedMap.clone();
                solvedMap = searchWortsCaseSzenario(solvedMap, previousShortestSolution, rekMap, prevPrevSolution);

            }else {
                for (Robot r : rekMap.getAllRobots()) {
                    if(r.id.equals(newLongestRobot.id)) {
                        double soutt = r.position.asAngel();
                        r.position.fromAngleToPosition(r.position.asAngel()-2);
                    }
                }

                solvedMap = solve(0, rekMap, 0);

                solvedMapSolutionLength = Math.round(solvedMap.getLongestMovedDistance() * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
                prevShortestSolution = Math.round(previousShortestSolution.getLongestMovedDistance() * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
                if(solvedMapSolutionLength >= prevShortestSolution){
                    // probiere nochmal
                    prevPrevSolution = previousShortestSolution.clone();
                    previousShortestSolution = solvedMap.clone();
                    solvedMap = searchWortsCaseSzenario(solvedMap, previousShortestSolution, rekMap, prevPrevSolution);
                }else{
                    // setze den wert wieder zurück
                    resetRekMap(rekMap, newLongestRobot.id);

                    // solvedMap ist wenn -2 augrund von +1 nicht geht, hier wieder auf dem urzustand
                    solvedMap = solve(0, rekMap, 0);
                }
            }

        }




        List<Robot> rekMapAngels = rekMap.getAllRobots().stream().toList();
        rekMapAngels.forEach(rekMapAngel -> {angleFromWorstCase.add(rekMapAngel.position.asAngel());});

        if(solvedMap.getLongestMovedDistance() < previousShortestSolution.getLongestMovedDistance()){
            bestSol2 = (previousShortestSolution.getLongestMovedDistance() > bestSol2) ? previousShortestSolution.getLongestMovedDistance() : bestSol2;
            if(previousShortestSolution.getLongestMovedDistance() >= bestSol2){
                wc = previousShortestSolution;
                return previousShortestSolution;
            }else{
                return wc;
            }
        }


        bestSol2 = (solvedMap.getLongestMovedDistance() > bestSol2) ? solvedMap.getLongestMovedDistance() : bestSol2;
        if(solvedMap.getLongestMovedDistance() >= bestSol2){
            wc = solvedMap;
            return solvedMap;
        }else{
            return wc;
        }
    }

    private static boolean isLoop(Robot maxDisSolved, int DECIMAL_PLACES2, Robot maxDisPrevPrev) {
        // runde um fehler zu vermeiden
        maxDisSolved.position.x = Math.round(maxDisSolved.position.x * Math.pow(10, DECIMAL_PLACES2)) / Math.pow(10, DECIMAL_PLACES2);
        maxDisSolved.position.y = Math.round(maxDisSolved.position.y * Math.pow(10, DECIMAL_PLACES2)) / Math.pow(10, DECIMAL_PLACES2);

        double angleMaxDisSolved = Math.round(maxDisSolved.position.asAngel() * Math.pow(10, 0)) / Math.pow(10, 0);


        maxDisPrevPrev.position.x = Math.round(maxDisPrevPrev.position.x * Math.pow(10, DECIMAL_PLACES2)) / Math.pow(10, DECIMAL_PLACES2);
        maxDisPrevPrev.position.y = Math.round(maxDisPrevPrev.position.y * Math.pow(10, DECIMAL_PLACES2)) / Math.pow(10, DECIMAL_PLACES2);

        double angleMaxDisPrev = Math.round(maxDisPrevPrev.position.asAngel() * Math.pow(10, 0)) / Math.pow(10, 0);


        boolean loop = false;
        if(maxDisSolved.id.equals(maxDisPrevPrev.id)) {
            if(angleMaxDisSolved == angleMaxDisPrev) {
            //if(maxDisSolved.position.x == maxDisPrevPrev.position.x && maxDisSolved.position.y == maxDisPrevPrev.position.y) {
                loop = true;
            }
        }
        return loop;
    }

    private static void resetRekMap(RobotMap rekMap, String rootRobot) {
        Robot robot = rekMap.getAllRobots().stream()
                .toList()
                .get(Integer.parseInt(rootRobot));
        robot.position.fromAngleToPosition(robot.position.asAngel()+1.0);
    }

    private static void safeAngles(RobotMap currentShortestSolution, RobotMap initRobots, List<Double> aHist) {
        if(initRobots.getAllRobots().size() < currentShortestSolution.getAllRobots().size()){
            aHist.add(0.0);
        }
        for (Robot currRobot : initRobots.getAllRobots()) {
            aHist.add(Math.round(currRobot.position.asAngel() * Math.pow(10, 0)) / Math.pow(10, 0));
            //aHist.add(currRobot.position.asAngel());
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
        //Collection<Robot> inactiveRobots = new ArrayList<>();
        for (Robot robot : map.getRunningRobots()) {
            // test every possible next target
            int counter = 0;
            for (Robot target : map.getSleepingRobots()) {
                RobotMap scenario = map.clone();

                statisticTotalMovesAnalyzed++;

                scenario.move(prefix, robot.id, target.id);
                //hier vlt screens
                //buildScene(stage, scenario, initialRobots, circle, x);
                 x++;

                double newLongestWay = scenario.getLongestMovedDistance();

                RobotMap solution;

                if (scenario.isSolved()) {
                    Log.log(prefix + "Test scenario: "+currentLongestWay+" -> "+newLongestWay+" Robot " + robot.id + " moves to " + target.id+" solves in "+newLongestWay);
                    solution = scenario;
                } else {
                    // solve remaining
                    int DECIMAL_PLACES = 5;
                    newLongestWay = Math.round(newLongestWay * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
                    shortestSolution = Math.round(shortestSolution * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);

                    if (newLongestWay < shortestSolution) {
                        Log.log(prefix + "Test scenario: "+currentLongestWay+" -> "+newLongestWay+" Robot " + robot.id + " moves to " + target.id);
                        //counter++;
                        solution = solve(nextLevel, scenario, newLongestWay);
                    } else {
                        Log.log(prefix + "Skip scenario: "+newLongestWay+" cannot be shorter than current solution "+shortestSolution+": Robot " + robot.id + " moves to " + target.id);
                        continue;
                    }
                }

                double solutionLength = solution.getLongestMovedDistance();
                // System.out.println("\n"+prefix+"check solution: is "+solutionLength+" faster than "+shortestSolution);
                int DECIMAL_PLACES = 5;
                solutionLength = Math.round(solutionLength * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
                shortestSolution = Math.round(shortestSolution * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
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

            /*// inaktivieren, wenn er keine kürzeren Wege findet
            if(counter==0) {
                inactiveRobots.add(robot);
            }

             */

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
        System.out.println("\n"+desc+": "+bestSolution.getLongestMovedDistance());
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

    public void setForImage(Stage stage, RobotMap initialRobots, Circle circle, Pane pane) {
        this.stage = stage;
        this.initialRobots = initialRobots;
        this.circle = circle;
        this.pane = pane;
    }

    public static boolean containsList(List<List<Double>> angleHistory, List<Double> aHist) {
        for (List<Double> list : angleHistory) {
            if (list.size() == aHist.size() && list.equals(aHist)) {
                return true;
            }
        }
        return false;
    }

    void buildScene(Stage stage, RobotMap solve, RobotMap initialRobots, Circle circle, int counter) {
        pane.getChildren().clear();
        RobotMap mapShortestSol = solve;
        List<Paint> colorRobot = new ArrayList<>();
        List<Line> lines = new ArrayList<>();
        List<Paint> colorList = new ArrayList<>();
        for (Map.Entry<String, Robot> entry : mapShortestSol.robots.entrySet()) {
            Robot robot = entry.getValue();
            Paint stroke = robot.getColor();
            colorList.add(stroke);

            colorRobot.add(stroke);
            List<Line> listeLines = robot.listeLines;
            for (Line line : listeLines) {
                line.setStroke(stroke);
                line.setStrokeWidth(5);
                lines.add(line);
            }
        }

        // speicher die roboter in einer liste
        List<Robot> robots = new ArrayList<>();
        copyRobotsInList(initialRobots, robots);

        // roboter werden der szene hinzugefügt
        drawRobots(robots, colorRobot);

        //List<Line> lines = solve.listeLines;
        for(Line line : lines){
            pane.getChildren().add(line);
        }




        initialRobots.getAllRobots().stream().forEach(r -> {
            Text roboPosition = new Text("" + Math.round((r.position.asAngel()*(-1000)) / 1000));

            roboPosition.setTextAlignment(TextAlignment.LEFT);
            roboPosition.setX(300 + (r.position.x * 250) + 10);
            roboPosition.setY(300 + (r.position.y*250)+10);
            roboPosition.setStyle("-fx-font-size: 20px; -fx-fill: black;");
            pane.getChildren().add(roboPosition);
        });

        pane.getChildren().add(circle);

        stage.setResizable(false);
        stage.setTitle("Freeze Tag Problem");
        //stage.setScene(new Scene(root, paneWidth, paneHeight));
        saveStageAsImage(stage, counter);
    }

    private static void copyRobotsInList(RobotMap initialRobots, List<Robot> robots) {
        robots.add(new Robot("99", true, 0, new ArrayList<>(), new ArrayList<>(),
                new Position(0, 0)));
        for (Map.Entry<String, Robot> entry : initialRobots.robots.entrySet()) {
            Robot robot = entry.getValue();
            robots.add(robot);
        }
    }

    private void saveStageAsImage(Stage stage, int counter) {
        WritableImage snapshot = stage.getScene().snapshot(null);

        BufferedImage bufferedImage = new BufferedImage((int) snapshot.getWidth(), (int) snapshot.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < snapshot.getWidth(); x++) {
            for (int y = 0; y < snapshot.getHeight(); y++) {
                bufferedImage.setRGB(x, y, snapshot.getPixelReader().getArgb(x, y));
            }
        }

        // Speichern des Bildes in eine Datei
        //String desktopPath = System.getProperty("user.home") + "/Desktop/FTP Lösungen/5/" + size + "_" + counter + ".png";
        File file = new File(System.getProperty("user.home") + "/Desktop/FTP Lösungen/6_"+ counter + ".png");
        try {
            ImageIO.write(bufferedImage, "PNG", file);
            System.out.println("Bild erfolgreich gespeichert: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawRobots(List<Robot> robots, List<Paint> colorRobot) {
        int counter = 0;
        for (Robot robot : robots) {
            // Roboter der Szene hinzufügen
            // problem muss da liegen, wo colorList befüllt wird
            Robot r = new Robot(String.valueOf(counter), true, 0, new ArrayList<>(), new ArrayList<>(),
                    new Position(300 + (robot.position.x*250), 300+(robot.position.y*250)));
            //r.color = colorRobot.get(counter);
            r.setFill(colorRobot.get(counter));
            pane.getChildren().add(r);
            counter++;
        }
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