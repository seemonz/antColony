import java.util.ArrayList;

// elitist extends the cycle -- deposit pheromone down after tour
// save the best-found-path so far and lay down pheromone on the edges of that path after each cycle
public class AntElitist extends AntCycle {
    // constructor
    public AntElitist(TSP tsp, float alpha, float beta, float evaporationParam, double pherParam) {
        super(tsp, alpha, beta, evaporationParam, pherParam);
    }

    // cycle runs ants on the TSP and updates pheromone with solutions/tours
    private Ant elitistCycle(Ant bestSoFar) {
        // initialize ants
        ArrayList<Ant> ants = initializeAnts(tspInstance);

        // tracking shortestTour of the cycle
        double shortestTour = 1000000;
        int shortestTourIndex = 0;

        findSolutions(tspInstance, ants); // finds tours for all ants

        // iterate through ants list and find shortestTour
        for(int i = 0; i < ants.size(); i++) {
            Ant anty = ants.get(i);
            double pathLen = pathLength(tspInstance, anty);

            // find shortestTour
            if(pathLen < shortestTour) {
                shortestTour = pathLen;
                shortestTourIndex = i;
            }
        }

        // check the shortestAnt against the bestSoFarAnt
        Ant shortestAnt = ants.get(shortestTourIndex);
        double bestSoFarLength = pathLength(tspInstance, bestSoFar);
        // if shortestAnt < bestSoFar then we set it to bestSoFar
        if(shortestTour < bestSoFarLength) {
            bestSoFar = shortestAnt;
        }

        // go through path of bestSoFarAnt and deposit pheromone of it's path onto TSP
        for(int i = 0; i < bestSoFar.getPath().length; i++) {
            // if last node, then get edge that goes from currentNode to startingNode
            if (i == bestSoFar.getPath().length - 1) {
                int currentNode = bestSoFar.getPath()[i];
                int nextNode = bestSoFar.getPath()[0];
                double currentPher = tspInstance.getNodePheromone()[currentNode][nextNode];  // grab pheromone
                currentPher = currentPher + (pherParam/bestSoFarLength); // update pheromone
                tspInstance.getNodePheromone()[currentNode][nextNode] = currentPher;
            } else {
                int currentNode = bestSoFar.getPath()[i];
                int nextNode = bestSoFar.getPath()[i+1];
                double currentPher = tspInstance.getNodePheromone()[currentNode][nextNode];  // grab pheromone
                currentPher = currentPher + (pherParam/bestSoFarLength); // update pheromone
                tspInstance.getNodePheromone()[currentNode][nextNode] = currentPher;
            }
        }

        // apply evaporation to the tsp
        evaporate(tspInstance, evaporationParam);

        return bestSoFar; // return the shortestPath Ant of this iteration
    }

    // public cycle: init. the ants and then tracks the best-so-far ant and feeds it into the cycle
    public double cycleElite(int numOfCycles) {

        // finds a long tour we can feed into the elitistCycle
        ArrayList<Ant> ants = initializeAnts(tspInstance);

        // find a longTour to start the system out with
        double longestTour = 1;
        int longestTourIndex = 0;

        findSolutions(tspInstance, ants); // find tours for all the ants

        // iterates through the ant tours and find shortest one
        for(int i = 0; i < ants.size(); i++) {
            Ant anty = ants.get(i);
            double pathLen = pathLength(tspInstance, anty);

            // find shortestTour
            if(pathLen > longestTour) {
                longestTour = pathLen;
                longestTourIndex = i;
            }
        }

        // we init the cycle with an ant with no tour
        Ant initBestAnt = ants.get(longestTourIndex);
        Ant nextBestAnt = elitistCycle(initBestAnt);

        // iterate numOfCycles from input and update with the best-so-far ant
        for(int i = 0; i < numOfCycles; i++) {
            nextBestAnt = elitistCycle(nextBestAnt);
        }

        return pathLength(tspInstance, nextBestAnt);
    }
}
