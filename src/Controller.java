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

//    // if we set either to 1 then that one isn't considered in the probability function
//    private static float alpha = 1.0f; // parameter for weighting the pheromone
//    private static float beta = 1.0f; // parameter for weighting the distance heuristic
//
    // takes in data from .tsp file and constructs an instance of the TSP class with said data
    private static TSP readInData() throws IOException {
        // file input vars
        File file = new File("./data/oliver30.tsp");
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
                tspInstance.getNodePheromone()[i][j] = 1.0f/size;
            }
        }

        return tspInstance;
    }

    public static void main(String[] args) throws IOException {

        int numCycles = 200;

        // ANT-DENSITY =============
        // init tsp
        TSP densityTSP = readInData();

        // init antDensity
        AntDensity antDensity = new AntDensity(densityTSP);

        // iterate antCycle solutions
        antDensity.cycle(numCycles);

        // ANT-QUANTITY =============
        // init tsp
        TSP quantityTSP= readInData();

        // init antDensity
        AntQuantity antQuantity = new AntQuantity(quantityTSP);

        // iterate antCycle solutions
        antQuantity.cycle(numCycles);

        // ANT-CYCLE =============
        // initialize cycleTSP
        TSP cycleTSP = readInData();

        // init the antCycle object
        AntCycle cycleTrail = new AntCycle(cycleTSP);

        cycleTrail.cycle(numCycles);

        //ELITIST ====================
        // initialize cycleTSP
        TSP elitistTSP = readInData();

        // init the antCycle object
        AntElitist elitistTrail = new AntElitist(elitistTSP);

        // iterate antCycle solutions
        elitistTrail.elitistCycleHelper(numCycles);

        //MAX-MIN ====================
        // initialize cycleTSP
        TSP maxMinTSP = readInData();

        // init the antCycle object
        AntMaxMin maxMinTrail = new AntMaxMin(maxMinTSP);

        // iterate antCycle solutions
        maxMinTrail.maxMinCycler(numCycles);
    }
}
