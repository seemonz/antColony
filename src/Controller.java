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
    // if we set either to 1 then that one isn't considered in the probablity function
    private static float alpha = 1.0f; // parameter for weighting the pheromone -- currently not considered
    private static float beta = 1.0f; // parameter for weighting the distance heuristic

    // takes in data from .tsp file and constructs an instance of the TSP class with said data
    private static TSP readInData() throws IOException {
        // file input vars
        File file = new File("./data/burma14.tsp");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st;
        int lineCount = 0;

        // matrix for TSP constructor -- [nodes][Xcoord, Ycoord]
        float[][] input = new float[14][2];

        while ((st = br.readLine()) != null) {
            lineCount++;
            // begin reading in nodes into matrix

            // this currently only works for burma14.tsp -- will have to change to work for any size tsp
            if (lineCount > 8 && lineCount < 23) {
                // grab nodeNumber
                String nodeNum = st;
                nodeNum = nodeNum.replaceAll("(\\d+).+", "$1");

                // grab the two float coordinates
                Pattern p = Pattern.compile("(\\d\\d.\\d\\d)");
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
        return tspInstance;
    }

    // sets up ants on each node of the TSP -- currently we use as many ants as there are nodes in the tsp
    private static ArrayList<Ant> initializeAnts(TSP tspInstance) {
        ArrayList<Ant> ants = new ArrayList<>(); // list of our ants

        // for each node we make an ant and set it's currentNode
        for(int i = 0; i < tspInstance.getSize(); i++) {
            Ant currentAnt = new Ant(tspInstance.getSize(), i); // make ant,A -- we set the startNode and currentNode in the constructor
            currentAnt.getPath()[currentAnt.getCount()] = i; // the starting node we set in the path[]
            currentAnt.setCount(currentAnt.getCount() + 1); // iterate the count of nodes visited
            currentAnt.getVisited()[i] = 1; // set our visited for our initial startNode
            ants.add(currentAnt); // add to list
        }
        return ants;
    }

    // this steps through the tsp instance one cycle and provides solutions for all the ants on it
    private static void findSolutions(TSP tspInstance, ArrayList<Ant> ants) {
        // for the size of the tsp we iterate through the ants and set their nextStep -- ie choose the greedy node, no pheromone yet
        for(int i = 0; i < tspInstance.getSize() - 1; i++) {
            stepAnts(tspInstance, ants);
        }
    }

    // this sets the next node for ants to visit
    private static void stepAnts(TSP tspInstance, ArrayList<Ant> ants) {
        // lets just pick the greedy choice for each ant to start with
        for(int i = 0; i < ants.size(); i++) {
            // get currentAnt and get greedy choice for it
            Ant currentAnt = ants.get(i);
            int currentGreedyChoice = chooseEdge(tspInstance, currentAnt);

            // set the new currentNode and update path and visited and iterate count
            currentAnt.setCurrentNode(currentGreedyChoice);
            currentAnt.getVisited()[currentGreedyChoice] = 1;
            currentAnt.getPath()[currentAnt.getCount()] = currentGreedyChoice;
            currentAnt.setCount(currentAnt.getCount() + 1);
        }
    }

    private static double pathLength(TSP tspInstance, Ant ant) {
       double pathLength = 0;

       for(int i = 0; i < tspInstance.getSize(); i++) {
           double[][] dist = tspInstance.getNodeDistances();
           int[] path = ant.getPath();

           if(i == tspInstance.getSize() - 1) { // the last value, connect to the first node in path, otherwise connect to path[i+1]
               pathLength += dist[path[i]][path[0]];
           } else {
               pathLength += dist[path[i]][path[i+1]];
           }
       }

       return pathLength;
    }

    // takes in an Ant and returns the next closest node to go to
    private static int greedyChoice(Ant currentAnt, TSP tspInstance) {
        double minDistPosition = 0;
        double minDist = 1000000000; // set to a high val

        // loop through every position of the TSP
        for(int i = 0; i < tspInstance.getSize(); i++) {
            boolean visited = false;

            // check if visited
            if(currentAnt.getVisited()[i] == 1) {
                visited = true;
            }


            // if we havent visited i in currentAnt.path then we check the dist
            if(!visited && currentAnt.getCurrentNode() != i) {

                double currentDist = tspInstance.getNodeDistances()[currentAnt.getCurrentNode()][i]; // grab dist from the tsp

                // check it against the minDist
                if(currentDist < minDist) {
                   minDist = currentDist; // set new minDist
                   minDistPosition = i; // set the position of the new minDist
                }
            }
        }

        return (int) minDistPosition; // returns the position of the nextNode to move to -- we downcast the double to int
    }

    // we do our probabilitic selection of the nextMove of the ant
    private static int chooseEdge(TSP tspInstance, Ant currentAnt) {
        int edgeChoice = 0;
        double[] initalProbs = new double[tspInstance.getSize()]; // this we store our intial probability values
        double[] probablities = new double[tspInstance.getSize()];

        // for currentAnt currentNode we compute the probabilities of all its potential moves
        // loop through every position of the TSP
        for(int i = 0; i < tspInstance.getSize(); i++) {
            boolean visited = false;

            // check if visited
            if(currentAnt.getVisited()[i] == 1) {
                visited = true;
            }

            // if we havent visited i in currentAnt.path then we compute its prob and save into initialProb
            if(!visited && currentAnt.getCurrentNode() != i) {
                double pheromoneProb = Math.pow(tspInstance.getNodePheromone()[currentAnt.getCurrentNode()][i], alpha); // pheromone
                double heurisiticProb = Math.pow(1/(tspInstance.getNodeDistances()[currentAnt.getCurrentNode()][i]), beta); // 1/distance
                double currentProb = pheromoneProb * heurisiticProb; // current probability of moving from currentNode to node i

                initalProbs[i] = currentProb; // set it in the array
            }
        }

        // we sum of the initial probabilities
        double sumOfProbs = 0;
        for(int i = 0; i < initalProbs.length; i++) {
            sumOfProbs += initalProbs[i];
        }

        // initial actual probalities
        for(int i = 0; i < probablities.length; i++) {
           probablities[i] =  initalProbs[i]/sumOfProbs;
        }

        // sample q from uniform distribution and apply probs
        Random rand = new Random();
        double sample = rand.nextDouble();

        double sum = 0;
        for(int i = 0; i < probablities.length; i++) {
            sum += probablities[i]; // add each prob as we go

            // if our sample lies inside this interval then choose i
            if(sample <= sum) {
                edgeChoice = i; // this is our probalistic choice
                break; // exit,
            }
        }

        return edgeChoice;
    }

    // evaporate some amount of the pheromone off all edges of the tsp, rate is a percentage ie 0.05 is 5%
    private static void evaporate(TSP tspInstance, float rate) {
        // decrease all pheromone on all edges by rate amount
        for(int i = 0; i < tspInstance.getSize(); i++) {
            for(int j = 0; j < tspInstance.getSize(); j++) {
                tspInstance.getNodePheromone()[i][j] = tspInstance.getNodePheromone()[i][j]*(1.0f - rate);
            }
        }
    }

    // cycle run ants on the tsp and update pheromone with solutions
    private static void cycle(TSP tspInstance) {
        // initialize
        ArrayList<Ant> ants = new ArrayList<>();
        ants = initializeAnts(tspInstance);

        double shortestTour = 1000000;
        int shortestTourIndex = 0;
        double tourSum = 0;
        // run solver
        findSolutions(tspInstance, ants);
        for(int i = 0; i < ants.size(); i++) {
            Ant anty = ants.get(i);
            double pathLen = pathLength(tspInstance, anty);
            tourSum += pathLen;
            System.out.println(pathLen);

            // find shortestTour
            if(pathLen < shortestTour) {
                shortestTour = pathLen;
                shortestTourIndex = i;
            }
        }

        // lay pheromone based on shortestTour
        Ant shortestAnt = ants.get(shortestTourIndex);
        System.out.println("shortestTour: " + shortestTour);
        System.out.println("avgTour: " + tourSum/tspInstance.getSize());
        // go through path of ant and lay down pheromone onto TSP
        for(int i = 0; i < shortestAnt.getPath().length; i++) {
            // if we are the last node, then we get edge back to start
            if (i == shortestAnt.getPath().length - 1) {
                int currentNode = shortestAnt.getPath()[i];
                int nextNode = shortestAnt.getPath()[0];
                double currentPher = tspInstance.getNodePheromone()[currentNode][nextNode];  // grab pheromone
                currentPher = currentPher + (1/shortestTour); // update pheromone
                tspInstance.getNodePheromone()[currentNode][nextNode] = currentPher;
            } else {
                int currentNode = shortestAnt.getPath()[i];
                int nextNode = shortestAnt.getPath()[i+1];
                double currentPher = tspInstance.getNodePheromone()[currentNode][nextNode];  // grab pheromone
                currentPher = currentPher + (1/shortestTour); // update pheromone
                tspInstance.getNodePheromone()[currentNode][nextNode] = currentPher;
            }
        }

        // apply evaporation to the tsp
        evaporate(tspInstance, 0.08f);
    }

    public static void main(String[] args) throws IOException {
        // initialize
        TSP tspInstance = readInData();

        // set the starting pheromone for all edges
        for(int i = 0; i < tspInstance.getSize(); i++) {
            for(int j = 0; j < tspInstance.getSize(); j++) {
                tspInstance.getNodePheromone()[i][j] = 1.0f/13.0f;
            }
        }

        // iterate solutions
        for(int i = 0; i < 50; i++) {
            cycle(tspInstance);
        }
    }
}
