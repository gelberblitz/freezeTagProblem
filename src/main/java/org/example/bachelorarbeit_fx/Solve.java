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

    // ersten schritt zu robot 1 ?
    // macht nur bei "auf kreis gleichverteilt" sinn !
    static final boolean FIRST_STEP_ROBOT_0_TO_1 = true;
    static final boolean SHOW_ALTERNATIVE_SOLUTIONS = true;

    List<List<Double>> listeMitWCInstanzen = new ArrayList<>();
    int statisticTotalMovesAnalyzed = 0;
    List<Double> angleFromWorstCase = new ArrayList<>();

    double bestSol = 0.0;
    double bestSol2 = 0.0;
    RobotMap wc = new RobotMap();
    RobotMap worstCaseInstance = new RobotMap();
    boolean longestDistanceFound = false;
    List<Line> listeLines = new ArrayList<>();

    List<RobotMap> listOfWcInstances = new ArrayList<>();
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

        mapShortestSol = solve(map, 0);

        showResult("Solution", mapShortestSol);

        System.out.println("Total moves analyzed: "+statisticTotalMovesAnalyzed+" in "+(System.currentTimeMillis()-startTime)+" ms");


        /*
        // suche den wortscase
        // speichert die beste Lösung für eine Instant
        RobotMap currentShortestSolution = mapShortestSol;
        RobotMap worstCase = searchWortsCaseScenario(currentShortestSolution, new RobotMap(), initialRobotsAll, new RobotMap(), -1);

        System.out.println("Längster Weg:" + worstCase.getLongestMovedDistance());

        showResult("Solution", worstCase);

        System.out.println("WC INSTANCE LÄNGE: " + worstCaseInstance.getLongestMovedDistance());

        for (List<Double> doubles : listeMitWCInstanzen) {
            for (Double aDouble : doubles) {
                System.out.println(aDouble);
            }
        }

         */

    }


    private RobotMap searchWortsCaseScenario(RobotMap currentShortestSolution, RobotMap previousShortestSolution,
                                             RobotMap initRobots, RobotMap prevPrevSolution,
                                             double preAngle) {

        //speicher die winkel der aktuellen lösung in aHist
        List<Double> aHist = new ArrayList<>();
        safeAngles(currentShortestSolution, initRobots, aHist);
        if(currentShortestSolution.getLongestMovedDistance() > 3.701){
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

            // GV/Symm ?
            //wird aktuell nicht genutzt
            boolean isGV = isGV(currentShortestSolution, initRobots);

            Robot maxDistanceRobot = new Robot("999", false, 0.0);
                for (Robot r : allRobotsAsListSortedBackwards) {
                    // sichergehen, dass es nicht einer von den anfänglichen robots ist, der den längsten geweckt hat
                    if (r.distanceMoved == currentShortestSolution.getLongestMovedDistance() && r.history.size() == 1) {
                        maxDistanceRobot = r;
                        break;
                    }
                }

                // aufweckkette
                boolean found = false;
                Robot x;
                List<Integer> historyOfBadestPath = new ArrayList<>();
                Robot currRob = maxDistanceRobot.clone();
                historyOfBadestPath.add(Integer.valueOf(currRob.id));

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

                for (Robot r : initRobots.getAllRobots()) {
                    r.id = String.valueOf(counter);
                    counter++;
                    if (r.id.equals(maxDistanceRobot.id)) {
                        // nur zum anschauen
                        double soutt = r.position.asAngel();
                        soutt++;
                        rekMap.createRobot(r.position.asAngel());
                    } else {
                        if (r.id.equals("0")) {
                            rekMap.createRobot(0.0, 0.0);
                        } else {
                            rekMap.createRobot(r.position.asAngel());
                        }
                    }
                }
                for(int i = 0;  i < historyOfBadestPath.size()-1; i++) {
                    String currBadestRobotIndex = String.valueOf(historyOfBadestPath.get(i));

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
                                double soutt = r2.position.asAngel();
                                soutt -= 2;
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

                /*
                // -> bis hier gab es keine schlechtere Instanz -> also vlt die anderen verschieben
                for (Robot robot : allRobotsAsListSortedBackwards) {
                if(!historyOfBadestPath.contains(Integer.parseInt(robot.id))){
                    String currBadestRobotIndex = robot.id;

                    Robot currBadestRobot = rekMap.getAllRobots().stream().toList().get(Integer.parseInt(currBadestRobotIndex));
                    currBadestRobot.position.fromAngleToPosition(currBadestRobot.position.asAngel() + 1);

                    // probiere verschiebung aus
                    solvedMap = solve(rekMap, 0);

                    // speicher die aktuellen positionen als winkel
                    for (Robot robot1 : rekMap.getAllRobots().stream().toList()) {
                        currAngles.add((double) Math.round(robot1.position.asAngel()));
                    }

                    double solvedMapSolutionLength = Math.round(solvedMap.getLongestMovedDistance() * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
                    double currShortestSolution = Math.round(previousShortestSolution.getLongestMovedDistance() * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
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
                                double soutt = r2.position.asAngel();
                                soutt -= 2;
                                r2.position.fromAngleToPosition(r2.position.asAngel() - 2);
                            }
                        }

                        // probiere verschiebung aus
                        solvedMap = solve(rekMap, 0);

                        // speicher die aktuellen positionen als winkel
                        for (Robot robot2 : rekMap.getAllRobots().stream().toList()) {
                            currAngles.add((double) Math.round(robot2.position.asAngel()));
                        }

                        solvedMapSolutionLength = Math.round(solvedMap.getLongestMovedDistance() * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
                        double prevShortestSolution = Math.round(previousShortestSolution.getLongestMovedDistance() * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
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

            }


                 */

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

    private static boolean isGV(RobotMap currentShortestSolution, RobotMap initRobots) {
        int symCounter = 0;
        Robot x;
        Robot y;
        boolean isSymmetric = false;
        double diff = Math.abs((360 / (currentShortestSolution.getAllRobots().size()-1)));
        for(int i = 0; i < initRobots.getAllRobots().size()-1; i++){
            x = initRobots.getAllRobots().stream().toList().get(i);
            int xAsAngle = (int) Math.round(x.position.asAngel());
            y = initRobots.getAllRobots().stream().toList().get(i+1);
            int yAsAngle = (int) Math.round(y.position.asAngel());
            if(xAsAngle < 0 && yAsAngle > 0 || xAsAngle > 0 && yAsAngle < 0){
                int abs1 = Math.abs(180 - Math.abs(xAsAngle));
                int abs2 = Math.abs(180 - Math.abs(yAsAngle));
                if(abs1+abs2 == diff) symCounter++;

            } else if(Math.abs(xAsAngle-yAsAngle) == diff){
                symCounter++;
            }
        }
        if(symCounter == initRobots.getAllRobots().size()-1){
            isSymmetric = true;
        }
        return isSymmetric;
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

                scenario.move(robot.id, target.id);
                //hier vlt screens
                //buildScene(stage, scenario, initialRobots, circle, x);

                double newLongestWay = scenario.getLongestMovedDistance();

                RobotMap solution;

                if (scenario.isSolved()) {
                    solution = scenario;
                } else {
                    // solve remaining
                    int DECIMAL_PLACES = 5;
                    newLongestWay = Math.round(newLongestWay * Math.pow(10, DECIMAL_PLACES)) / Math.pow(10, DECIMAL_PLACES);
                    shortestSolution =  Math.round(shortestSolution * 100000.0) / 100000.0;

                    if (newLongestWay < shortestSolution) {
                        //counter++;
                        solution = solve(scenario, newLongestWay);
                    } else {
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
                } else if (SHOW_ALTERNATIVE_SOLUTIONS && Math.abs(solutionLength - shortestSolution)<0.1) {
                    // gleich/sehr ähnlich gute alternativläsung
                    showResult("Alternative solution", solution);
                    System.out.println();
                    System.out.println("Current Total Moves:");
                    System.out.println(statisticTotalMovesAnalyzed);
                }
            }


        }
        bestSol = shortestSolution;

        return bestSolution;
    }

    public void showResult(String desc, RobotMap bestSolution) {
        System.out.println("\n"+desc+": "+bestSolution.getLongestMovedDistance());
        for (Robot robot : bestSolution.getAllRobots()) {
            System.out.println(robot.id+": "+robot.history+" => total distance: "+robot.distanceMoved);
        }

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