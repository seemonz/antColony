import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// the base class that runs the basic system -- everything else is an extension of this
abstract class antSystem {
    TSP tspInstance;

    // constructor
    public antSystem(TSP tsp) {
        this.tspInstance = tsp;
    }

    // if we set either to 1 then that one isn't considered in the probability function
    private static float alpha = 1.0f; // parameter for weighting the pheromone
    private static float beta = 1.0f; // parameter for weighting the distance heuristic

//    // takes in data from .tsp file and constructs an instance of the TSP class with said data
//    protected TSP readInData() throws IOException {
//        // file input vars
//        File file = new File("./data/oliver30.tsp");
//        BufferedReader br = new BufferedReader(new FileReader(file));
//        String st;
//        int lineCount = 0;
//        int tspDimension = 0;
//
//        // matrix for TSP constructor -- [nodes][Xcoord, Ycoord]
//        float[][] input = new float[0][0];
//
//        while ((st = br.readLine()) != null) {
//            lineCount++;
//            // begin reading in nodes into matrix
//
//
//            // grab the dimension of the tsp
//            if (lineCount == 4) {
//                Pattern p = Pattern.compile("(\\d+)");
//                Matcher m = p.matcher(st);
//                m.find();
//                tspDimension = Integer.parseInt(m.group(1));
//
//                // set input dimension size
//                input = new float[tspDimension][2];
//            }
//
//            // works for burma14.tsp and oliver30.tsp and likely any of the tsps with the correct coordinate values
//            if (lineCount > 8 && lineCount < (9 + tspDimension)) {
//                // grab nodeNumber
//                String nodeNum = st;
//                nodeNum = nodeNum.replaceAll("(\\d+).+", "$1");
//
//                // grab the two float coordinates
//                Pattern p = Pattern.compile("(\\d+.\\d\\d)");
//                Matcher m = p.matcher(st);
//                m.find();
//                String xCoord = m.group(1);
//                m.find();
//                String yCoord = m.group(1);
//
//                // take three strings and put into input matrix
//                int node = Integer.parseInt(nodeNum.trim()); // for correct position in matrix
//                input[node - 1][0] = Float.parseFloat(xCoord.trim()); // x
//                input[node - 1][1] = Float.parseFloat(yCoord.trim()); // y
//            }
//        }
//        TSP tspInstance = new TSP(input); // our TSP object
//
//        float size = (float) input.length;
//        // set the starting pheromone for all edges
//        for(int i = 0; i < tspInstance.getSize(); i++) {
//            for(int j = 0; j < tspInstance.getSize(); j++) {
//                tspInstance.getNodePheromone()[i][j] = 1.0f/size;
//            }
//        }
//
//        return tspInstance;
//    }

    // sets up ants on each node of the TSP -- currently we use as many ants as there are nodes in the tsp
    protected ArrayList<Ant> initializeAnts(TSP tspInstance) {
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
    protected void findSolutions(TSP tspInstance, ArrayList<Ant> ants) {
        // for the size of the tsp we iterate through the ants and set their nextStep -- ie choose the greedy node, no pheromone yet
        for(int i = 0; i < tspInstance.getSize() - 1; i++) {
            stepAnts(tspInstance, ants);
        }
    }

    // this sets the next node for ants to visit
    protected void stepAnts(TSP tspInstance, ArrayList<Ant> ants) {
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

    protected double pathLength(TSP tspInstance, Ant ant) {
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
    protected int greedyChoice(Ant currentAnt, TSP tspInstance) {
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

    // we do our probabilistic selection of the nextMove of the ant
    protected int chooseEdge(TSP tspInstance, Ant currentAnt) {
        int edgeChoice = 0;
        double[] initialProbs = new double[tspInstance.getSize()]; // this we store our intial probability values
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

                initialProbs[i] = currentProb; // set it in the array
            }
        }

        // we sum of the initial probabilities
        double sumOfProbs = 0;
        for(int i = 0; i < initialProbs.length; i++) {
            sumOfProbs += initialProbs[i];
        }

        // initial actual probabilities
        for(int i = 0; i < probablities.length; i++) {
            probablities[i] =  initialProbs[i]/sumOfProbs;
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
}