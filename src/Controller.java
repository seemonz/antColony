import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Random;

// manages Ant, TSP and initializing
public class Controller {

    // takes in data from .tsp file and constructs an instance of the TSP class with said data
    private static TSP readInData() throws IOException {
        // file input vars
        File file = new File("./data/oliver30.tsp"); // oliver30 TSP is a standard tsp that is used in the literature
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st;
        int lineCount = 0;
        int tspDimension = 0;

        // matrix for TSP constructor -- [nodes][Xcoord, Ycoord]
        float[][] input = new float[0][0];

        while ((st = br.readLine()) != null) {
            lineCount++;
            // begin reading in nodes into matrix


            // grab the dimension of the tsp
            if (lineCount == 4) {
                Pattern p = Pattern.compile("(\\d+)");
                Matcher m = p.matcher(st);
                m.find();
                tspDimension = Integer.parseInt(m.group(1));

                // set input dimension size
                input = new float[tspDimension][2];
            }

            // works for burma14.tsp and oliver30.tsp and likely any of the tsps with the correct coordinate values
            if (lineCount > 8 && lineCount < (9 + tspDimension)) {
                // grab nodeNumber
                String nodeNum = st;
                nodeNum = nodeNum.replaceAll("(\\d+).+", "$1");

                // grab the two float coordinates
                Pattern p = Pattern.compile("(\\d+.\\d\\d)");
                Matcher m = p.matcher(st);
                m.find();
                String xCoord = m.group(1);
                m.find();
                String yCoord = m.group(1);

                // take three strings and put into input matrix
                int node = Integer.parseInt(nodeNum.trim()); // for correct position in matrix
                input[node - 1][0] = Float.parseFloat(xCoord.trim()); // x
                input[node - 1][1] = Float.parseFloat(yCoord.trim()); // y
            }
        }
        TSP tspInstance = new TSP(input); // our TSP object

        float size = (float) input.length;
        // set the starting pheromone for all edges
        for(int i = 0; i < tspInstance.getSize(); i++) {
            for(int j = 0; j < tspInstance.getSize(); j++) {
                tspInstance.getNodePheromone()[i][j] = 1.0f/size; // starting pheromone is inversely proportional to size
            }
        }

        return tspInstance;
    }

    // AntSystem running function
    // INPUT:
    //  antChoice: 0 - 4 , choose the type of system to run on the TSP
    //  alpha: the weight given to the pheromone value in the ant decision eqn, 1.0 to 5.0 range
    //  beta: the weight given to the shortestEdge value in the ant decision eqn, 1.0 to 5.0 range
    //  evaporationParam: the amount of evaporation that happens at the end of every tour, 0.0 to 1.0 range
    //  pherParam: the pheromone normalizer value
    private static void runSystem(int antChoice, float alpha, float beta, float evaporationParam, double pherParam, int numOfCycles, int numOfRuns) throws IOException {

        switch(antChoice) {
            case 0: // antDensity
                double tourSums = 0;
                double bestTourOfRun = 10000;

                // iterate antCycle solutions
                for(int i = 0; i < numOfRuns; i++) {

                    // init tsp
                    TSP densityTSP = readInData();

                    // init antDensity
                    AntDensity antDensity = new AntDensity(densityTSP, alpha, beta, evaporationParam, pherParam);

                    // run the cycle
                    double currentBestTour = antDensity.cycle(numOfCycles);

                    if(currentBestTour < bestTourOfRun) {
                        bestTourOfRun = currentBestTour;
                    }

                    tourSums += currentBestTour;
                }


                System.out.println("=================== ANT-DENSITY ===================");
                System.out.println("Run the system " + numOfRuns + " times with ");
                System.out.println("Number of cycles: " + numOfCycles);
                System.out.println("Best Tour found: " + bestTourOfRun);
                System.out.println("AvgBestTour: " + tourSums/numOfRuns );
                break;

            case 1: // antQuantity
                tourSums = 0;
                bestTourOfRun = 10000;

                // iterate antCycle solutions
                for(int i = 0; i < numOfRuns; i++) {

                    // init tsp
                    TSP quantityTSP = readInData();

                    // init antQuantity
                    AntQuantity antQuantity = new AntQuantity(quantityTSP, alpha, beta, evaporationParam, pherParam);

                    // run the cycle
                    double currentBestTour = antQuantity.cycle(numOfCycles);

                    if(currentBestTour < bestTourOfRun) {
                        bestTourOfRun = currentBestTour;
                    }

                    tourSums += currentBestTour;
                }


                System.out.println("=================== ANT-QUANTITY ===================");
                System.out.println("Run the system " + numOfRuns + " times with ");
                System.out.println("Number of cycles: " + numOfCycles);
                System.out.println("Best Tour found: " + bestTourOfRun);
                System.out.println("AvgBestTour: " + tourSums/numOfRuns );
                break;

            case 2: // antCycle
                tourSums = 0;
                bestTourOfRun = 10000;

                // iterate antCycle solutions
                for(int i = 0; i < numOfRuns; i++) {

                    // init tsp
                    TSP cycleTSP = readInData();

                    // init antDensity
                    AntCycle antCycle = new AntCycle(cycleTSP, alpha, beta, evaporationParam, pherParam);

                    // run the cycle
                    double currentBestTour = antCycle.cycle(numOfCycles);

                    if(currentBestTour < bestTourOfRun) {
                        bestTourOfRun = currentBestTour;
                    }

                    tourSums += currentBestTour;
                }


                System.out.println("=================== ANT-CYCLE ===================");
                System.out.println("Run the system " + numOfRuns + " times with ");
                System.out.println("Number of cycles: " + numOfCycles);
                System.out.println("Best Tour found: " + bestTourOfRun);
                System.out.println("AvgBestTour: " + tourSums/numOfRuns );
                break;

            case 3: // antElitist
                tourSums = 0;
                bestTourOfRun = 10000;

                // iterate antCycle solutions
                for(int i = 0; i < numOfRuns; i++) {

                    // init tsp
                    TSP elitistTSP = readInData();

                    // init antDensity
                    AntElitist antElitist = new AntElitist(elitistTSP, alpha, beta, evaporationParam, pherParam);

                    // run the cycle
                    double currentBestTour = antElitist.cycleElite(numOfCycles);

                    if(currentBestTour < bestTourOfRun) {
                        bestTourOfRun = currentBestTour;
                    }

                    tourSums += currentBestTour;
                }


                System.out.println("=================== ANT-ELITIST ===================");
                System.out.println("Run the system " + numOfRuns + " times with ");
                System.out.println("Number of cycles: " + numOfCycles);
                System.out.println("Best Tour found: " + bestTourOfRun);
                System.out.println("AvgBestTour: " + tourSums/numOfRuns );
                break;

            case 4: // antMaxMin
                tourSums = 0;
                bestTourOfRun = 10000;

                // iterate antCycle solutions
                for(int i = 0; i < numOfRuns; i++) {

                    // init tsp
                    TSP maxMinTSP = readInData();

                    // init antDensity
                    AntMaxMin antMaxMin = new AntMaxMin(maxMinTSP, alpha, beta, evaporationParam, pherParam);

                    // run the cycle
                    double currentBestTour = antMaxMin.cycleMaxMin(numOfCycles);

                    if(currentBestTour < bestTourOfRun) {
                        bestTourOfRun = currentBestTour;
                    }

                    tourSums += currentBestTour;
                }


                System.out.println("=================== ANT-MAXMIN ===================");
                System.out.println("Run the system " + numOfRuns + " times with ");
                System.out.println("Number of cycles: " + numOfCycles);
                System.out.println("Best Tour found: " + bestTourOfRun);
                System.out.println("AvgBestTour: " + tourSums/numOfRuns );
                break;


            default:
                throw new IllegalStateException("Unexpected value: " + antChoice);
        }
    }

    public static void main(String[] args) throws IOException {

        int numOfCycles = 200;
        // AntDensity
        runSystem(0, 1.0f, 5.0f, 0.08f, 0.05, numOfCycles, 10);

        // AntQuantity
        runSystem(1, 1.0f, 5.0f, 0.08f, 1, numOfCycles, 10);

        // AntCycle
        runSystem(2, 1.0f, 5.0f, 0.08f, 1, numOfCycles, 10);

        // AntElitist
        runSystem(3, 1.0f, 5.0f, 0.08f, 1, numOfCycles, 10);

        // AntMaxMin
        runSystem(4, 1.0f, 5.0f, 0.08f, 1, numOfCycles, 10);


//        int numCycles = 200;
//
//        // ANT-DENSITY =============
//        // init tsp
//        TSP densityTSP = readInData();
//
//        // init antDensity
//        AntDensity antDensity = new AntDensity(densityTSP);
//
//        // iterate antCycle solutions
//        antDensity.cycle(numCycles);
//
//        // ANT-QUANTITY =============
//        // init tsp
//        TSP quantityTSP= readInData();
//
//        // init antDensity
//        AntQuantity antQuantity = new AntQuantity(quantityTSP);
//
//        // iterate antCycle solutions
//        antQuantity.cycle(numCycles);
//
//        // ANT-CYCLE =============
//        // initialize cycleTSP
//        TSP cycleTSP = readInData();
//
//        // init the antCycle object
//        AntCycle cycleTrail = new AntCycle(cycleTSP);
//
//        cycleTrail.cycle(numCycles);
//
//        //ELITIST ====================
//        // initialize cycleTSP
//        TSP elitistTSP = readInData();
//
//        // init the antCycle object
//        AntElitist elitistTrail = new AntElitist(elitistTSP);
//
//        // iterate antCycle solutions
//        elitistTrail.elitistCycleHelper(numCycles);
//
//        //MAX-MIN ====================
//        // initialize cycleTSP
//        TSP maxMinTSP = readInData();
//
//        // init the antCycle object
//        AntMaxMin maxMinTrail = new AntMaxMin(maxMinTSP);
//
//        // iterate antCycle solutions
//        maxMinTrail.maxMinCycler(numCycles);
    }
}
