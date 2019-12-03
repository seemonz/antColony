import java.util.ArrayList;

// MaxMin style ant system uses a variety of approaches to find a solution
public class AntMaxMin extends AntCycle {
    // constructor
    public AntMaxMin(TSP tsp) {
        super(tsp);
    }

    private static final double pherParam = 2; // param for pheromone laying equation
    private static final double minPheromone = 0.001; // min value of pheromone
    private static final double maxPheromone = 0.5; // max value of pheromone

    // cycle run ants on the tsp and update pheromone with solutions
    // we use the tours shortest solution to lay the pheromone down on
    private double maxMinCycle() {
        // initialize ants
        ArrayList<Ant> ants = new ArrayList<>();
        ants = initializeAnts(tspInstance);

        // init shortestTour measures
        double shortestTour = 1000000;
        int shortestTourIndex = 0;
        double tourSum = 0;

        // run solver
        findSolutions(tspInstance, ants);
        for (int i = 0; i < ants.size(); i++) {
            Ant anty = ants.get(i);
            double pathLen = pathLength(tspInstance, anty);
            tourSum += pathLen;
            // DEBUGGING
//            System.out.println(pathLen);

            // find shortestTour
            if (pathLen < shortestTour) {
                shortestTour = pathLen;
                shortestTourIndex = i;
            }
        }

        Ant shortestAnt = ants.get(shortestTourIndex);

        // DEBUGGING
//        System.out.println("shortestTourPath: " + shortestTour);
//        System.out.println("bestSoFarTourPath: " + bestSoFarLength);
//        System.out.println("avgTour: " + tourSum/tspInstance.getSize());

        // go through path of ant and lay down pheromone onto TSP
        for (int i = 0; i < shortestAnt.getPath().length; i++) {
            // if we are the last node, then we get edge back to start
            if (i == shortestAnt.getPath().length - 1) {
                int currentNode = shortestAnt.getPath()[i];
                int nextNode = shortestAnt.getPath()[0];
                pheromoneSetter(tspInstance, currentNode, nextNode, shortestTour);
            } else {
                int currentNode = shortestAnt.getPath()[i];
                int nextNode = shortestAnt.getPath()[i + 1];
                pheromoneSetter(tspInstance, currentNode, nextNode, shortestTour);
            }
        }

        // apply evaporation to the tsp
        evaporate(tspInstance, 0.05f);
        return shortestTour; // we return the shortestPath Ant of this iteration
    }

    // initializes the pheromone to maxPheromone
    private void pheromoneInitializer() {
        for(int i = 0; i < tspInstance.getSize(); i++) {
            for(int k = 0; k < tspInstance.getSize(); k++) {
               tspInstance.getNodePheromone()[i][k] = maxPheromone;
            }
        }
    }

    // this checks for min/max and sets pheromone value
    private static void pheromoneSetter(TSP tspInstance, int currentNode, int nextNode, double tourLength) {
        double currentPher = tspInstance.getNodePheromone()[currentNode][nextNode];  // grab pheromone
        double setPher = 0;

        if(currentPher <= minPheromone) { // we are at minPheromone, stay there
            setPher = minPheromone;
        }else if(currentPher >= maxPheromone) { // we are at maxPheromone, stay there
            setPher = maxPheromone;
        }else{
            setPher = currentPher + (pherParam / tourLength); // somewhere between min/max, set the new pheromone
        }

        tspInstance.getNodePheromone()[currentNode][nextNode] = setPher;
    }

    public void maxMinCycler(int numOfCycles) {

        // finds a long tour we can feed into the elitistCycle
        ArrayList<Ant> ants = new ArrayList<>();
        ants = initializeAnts(tspInstance);

        // init pheromone lvls on the tspInstance
        pheromoneInitializer();

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
        double shortestTour = 1000000000;
        for(int i = 0; i < numOfCycles; i++) {
            double currentTour = maxMinCycle();
            if(currentTour < shortestTour) {
                shortestTour = currentTour;
            }
        }
        System.out.println("=================== MAX-MIN ===================");
        System.out.println("Number of iterations: " + numOfCycles);
        System.out.println("BestSoFar : " + shortestTour);
    }
}
