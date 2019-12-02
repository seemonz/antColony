import java.util.ArrayList;

// elitist extends the cycle -- we lay down pheromone down after tour
// we save the best-found-path so far and lay down pheromone on the edges of that path
public class AntElitist extends AntCycle {
    public AntElitist(TSP tsp) {
        super(tsp);
    }

    private static final double pherParam = 1; // param for pheromone laying equation

    // cycle run ants on the tsp and update pheromone with solutions
    private Ant elitistCycle(Ant bestSoFar) {
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
            // DEBUGGING
//            System.out.println(pathLen);

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

        // DEBUGGING
//        System.out.println("shortestTourPath: " + shortestTour);
//        System.out.println("bestSoFarTourPath: " + bestSoFarLength);
//        System.out.println("avgTour: " + tourSum/tspInstance.getSize());

        // go through path of ant and lay down pheromone onto TSP
        for(int i = 0; i < shortestAnt.getPath().length; i++) {
            // if we are the last node, then we get edge back to start
            if (i == shortestAnt.getPath().length - 1) {
                int currentNode = shortestAnt.getPath()[i];
                int nextNode = shortestAnt.getPath()[0];
                double currentPher = tspInstance.getNodePheromone()[currentNode][nextNode];  // grab pheromone
                currentPher = currentPher + (pherParam/shortestTour); // update pheromone
                tspInstance.getNodePheromone()[currentNode][nextNode] = currentPher;
            } else {
                int currentNode = shortestAnt.getPath()[i];
                int nextNode = shortestAnt.getPath()[i+1];
                double currentPher = tspInstance.getNodePheromone()[currentNode][nextNode];  // grab pheromone
                currentPher = currentPher + (pherParam/shortestTour); // update pheromone
                tspInstance.getNodePheromone()[currentNode][nextNode] = currentPher;
            }
        }

        // apply evaporation to the tsp
        evaporate(tspInstance, 0.08f);
        return bestSoFar; // we return the shortestPath Ant of this iteration
    }

    public void elitistCycleHelper(int numOfCycles) {

        // finds a long tour we can feed into the elitistCycle
        ArrayList<Ant> ants = new ArrayList<>();
        ants = initializeAnts(tspInstance);

        double longestTour = 1;
        int longestTourIndex = 0;
        double tourSum = 0;
        // run solver
        findSolutions(tspInstance, ants);
        for(int i = 0; i < ants.size(); i++) {
            Ant anty = ants.get(i);
            double pathLen = pathLength(tspInstance, anty);
            tourSum += pathLen;
            // DEBUGGING
//            System.out.println(pathLen);

            // find shortestTour
            if(pathLen > longestTour) {
                longestTour = pathLen;
                longestTourIndex = i;
            }
        }

        // we init the cycle with an ant with no tour
        Ant initBestAnt = ants.get(longestTourIndex);
        Ant nextBestAnt = elitistCycle(initBestAnt);
        for(int i = 0; i < numOfCycles; i++) {
            nextBestAnt = elitistCycle(nextBestAnt);
        }
        System.out.println("=================== ELITIST ===================");
        System.out.println("Number of iterations: " + numOfCycles);
        System.out.println("BestSoFar : " + pathLength(tspInstance, nextBestAnt));
    }
}
