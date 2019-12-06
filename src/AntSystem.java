import java.util.ArrayList;
import java.util.Random;

// the base class that runs the basic system -- everything else is an extension of this
abstract class AntSystem {
    TSP tspInstance; // the TSP object that the ants lay their pheromone down on
    float alpha; // parameter for weighting the relative importance of the pheromone in the probability function
    float beta; // parameter for weighting the relative importance of the visibility heuristic in the probability function

    // constructor
    public AntSystem(TSP tsp, float alpha, float beta) {
        this.tspInstance = tsp;
        this.alpha = alpha;
        this.beta = beta;
    }

    // sets up ants on each node of the TSP -- currently we use as many ants as there are nodes in the TSP
    protected ArrayList<Ant> initializeAnts(TSP tspInstance) {
        ArrayList<Ant> ants = new ArrayList<>(); // list storing the ants

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

    // this steps through the tspInstance one cycle and provides solutions/tours for all the ants on it
    protected void findSolutions(TSP tspInstance, ArrayList<Ant> ants) {
        // for the size of the tsp we iterate through the ants and set their nextStep -- ie choose the greedy node, no pheromone yet
        for(int i = 0; i < tspInstance.getSize() - 1; i++) {
            stepAnts(tspInstance, ants);
        }
    }

    // sets the next node for each ant to visit
    protected void stepAnts(TSP tspInstance, ArrayList<Ant> ants) {
        // for each ant in the list of ants
        for(int i = 0; i < ants.size(); i++) {
            // get currentAnt and get the currentChoice for said ant
            Ant currentAnt = ants.get(i);
            int currentChoice = chooseEdge(tspInstance, currentAnt); // our choice of node to move to based on probability function

            // set the new currentNode and update path and visited and iterate count
            currentAnt.setCurrentNode(currentChoice);
            currentAnt.getVisited()[currentChoice] = 1;
            currentAnt.getPath()[currentAnt.getCount()] = currentChoice;
            currentAnt.setCount(currentAnt.getCount() + 1);
        }
    }

    // returns the total path length of the ants tour/solution
    protected double pathLength(TSP tspInstance, Ant ant) {
        double pathLength = 0;

        // for each node in TSP we sum the edgeDist that the ant chose
        for(int i = 0; i < tspInstance.getSize(); i++) {
            double[][] dist = tspInstance.getNodeDistances();
            int[] path = ant.getPath();

            // add these edges in path to the pathLength
            if(i == tspInstance.getSize() - 1) { // the last value, connect to the first node in path, otherwise connect to path[i+1]
                pathLength += dist[path[i]][path[0]];
            } else {
                pathLength += dist[path[i]][path[i+1]];
            }
        }

        return pathLength;
    }

    // takes in an Ant and returns the next closest node to go to -- it's a basic greedy approach used in the naive solution
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

    // probabilistic selection of the nextMove of the ant
    protected int chooseEdge(TSP tspInstance, Ant currentAnt) {
        int edgeChoice = 0;
        double[] initialProbs = new double[tspInstance.getSize()]; // store the initial probability values
        double[] probablities = new double[tspInstance.getSize()];

        // for currentAnt's currentNode we compute the probabilities of all its potential moves
        // loop through every position of the TSP
        for(int i = 0; i < tspInstance.getSize(); i++) {
            boolean visited = false;

            // check if visited node
            if(currentAnt.getVisited()[i] == 1) {
                visited = true;
            }

            // if haven't visited i in currentAnt.path then compute its prob and save into initialProb
            if(!visited && currentAnt.getCurrentNode() != i) {
                double pheromoneProb = Math.pow(tspInstance.getNodePheromone()[currentAnt.getCurrentNode()][i], alpha); // pheromone
                double heurisiticProb = Math.pow(1/(tspInstance.getNodeDistances()[currentAnt.getCurrentNode()][i]), beta); // 1/distance
                double currentProb = pheromoneProb * heurisiticProb; // current probability of moving from currentNode to node i

                initialProbs[i] = currentProb; // set it in the array
            }
        }

        // sum of the initial probabilities
        double sumOfProbs = 0;
        for(int i = 0; i < initialProbs.length; i++) {
            sumOfProbs += initialProbs[i];
        }

        // initial actual probabilities
        for(int i = 0; i < probablities.length; i++) {
            probablities[i] =  initialProbs[i]/sumOfProbs;
        }

        // sample q from uniform distribution and apply probabilities
        Random rand = new Random();
        double sample = rand.nextDouble();

        double sum = 0;
        for(int i = 0; i < probablities.length; i++) {
            sum += probablities[i]; // add each probability as we go

            // if our sample lies inside this interval then choose i
            if(sample <= sum) {
                edgeChoice = i; // this is our probabilistic choice
                break; // exit,
            }
        }

        return edgeChoice;
    }
}
