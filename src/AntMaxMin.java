import java.util.ArrayList;

// MaxMin style ant system uses a variety of approaches to find a solution
public class AntMaxMin extends AntCycle {
    // constructor
    public AntMaxMin(TSP tsp, float alpha, float beta, float evaporationParam, double pherParam) {
        super(tsp, alpha, beta, evaporationParam, pherParam);
    }

    // two const control the range of the pheromone
    private static final double minPheromone = 0.001; // min value of pheromone
    private static final double maxPheromone = 0.5; // max value of pheromone

    // cycle runs ants on the TSP and uses the cycle-best tour to deposit pheromone
    private double maxMinCycle() {
        // initialize ants
        ArrayList<Ant> ants = initializeAnts(tspInstance);

        // init shortestTour measures
        double shortestTour = 1000000;
        int shortestTourIndex = 0;

        findSolutions(tspInstance, ants); // finds tours for all ants

        // iterate through ants list and find shortestTour
        for (int i = 0; i < ants.size(); i++) {
            Ant anty = ants.get(i);
            double pathLen = pathLength(tspInstance, anty);

            // find shortestTour
            if (pathLen < shortestTour) {
                shortestTour = pathLen;
                shortestTourIndex = i;
            }
        }

        Ant shortestAnt = ants.get(shortestTourIndex);

        // checks to see how many of the ants paths have converged, if they have then re-init the pheromone on the TSP
        if(stagnationChecker(tspInstance, ants)) {
            pheromoneInitializer();
        }

        // go through path of cycle-best Ant and deposit pheromone of it's path onto TSP
        for (int i = 0; i < shortestAnt.getPath().length; i++) {
            // if last node, then get edge that goes from currentNode to startingNode
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

        // apply evaporation to the TSP
        evaporate(tspInstance, evaporationParam);
        return shortestTour; // return the shortestPath Ant of this iteration
    }

    // initializes the pheromone to maxPheromone
    private void pheromoneInitializer() {
        for(int i = 0; i < tspInstance.getSize(); i++) {
            for(int k = 0; k < tspInstance.getSize(); k++) {
               tspInstance.getNodePheromone()[i][k] = maxPheromone;
            }
        }
    }

    // checks for min/max and sets pheromone value
    private void pheromoneSetter(TSP tspInstance, int currentNode, int nextNode, double tourLength) {
        double currentPher = tspInstance.getNodePheromone()[currentNode][nextNode];  // grab pheromone
        double setPher = 0;

        if(currentPher <= minPheromone) { // currentPheromone is at minPheromone, stay there
            setPher = minPheromone;
        }else if(currentPher >= maxPheromone) { // currentPheromone is at maxPheromone, stay there
            setPher = maxPheromone;
        }else{
            setPher = currentPher + (pherParam / tourLength); // somewhere between min/max, set the new pheromone
        }

        tspInstance.getNodePheromone()[currentNode][nextNode] = setPher; // set the pheromone
    }

    // checks if ants tours are converging too much and returns boolean value
    private boolean stagnationChecker(TSP tspInstance, ArrayList<Ant> ants) {
        boolean stagnated = false;
        int count = 0;  // tracks the number of antPaths that have converged
        // for ants check each pathLength against the next one
        for(int i = 0; i < ants.size() - 1; i++) {
            count  = 0;
            double currentLength = pathLength(this.tspInstance, ants.get(i));
            for(int j = 0; j < ants.size(); j++) {
                if( j != i) {
                    double nextLength = pathLength(this.tspInstance, ants.get(j));
                    double difference = Math.abs(currentLength - nextLength);

                    // if the two values almost equal (range -2 to +2) one another then the two antPaths that have converged
                    if(0 < difference && difference < 2) {
                        count++;
                    }

                    // if the count of converged ants is  > 0.4 of all ants then stagnation
                    if(count > 0.4*ants.size()) {
                        stagnated = true;
                        break;
                    }
                }
            }
        }

        return stagnated;
    }


    // public cycle function -- tracks the best-so-far tour and returns it
    // input: numOfCycles
    public double cycleMaxMin(int numOfCycles) {

        // finds a long tour that can be fed into the elitistCycle
        ArrayList<Ant> ants = initializeAnts(tspInstance);

        // init pheromone lvls on the tspInstance
        pheromoneInitializer();

        double longestTour = 1;
        int longestTourIndex = 0;

        findSolutions(tspInstance, ants); // finds tours for all ants

        // iterate through ants tours and set shortest
        for(int i = 0; i < ants.size(); i++) {
            Ant anty = ants.get(i);
            double pathLen = pathLength(tspInstance, anty);

            // find shortestTour
            if(pathLen > longestTour) {
                longestTour = pathLen;
                longestTourIndex = i;
            }
        }


        // init the cycle with an ant with no tour
        double shortestTour = 1000000000;
        for(int i = 0; i < numOfCycles; i++) {
            double currentTour = maxMinCycle();
            if(currentTour < shortestTour) {
                shortestTour = currentTour;
            }
        }

        return shortestTour;
    }
}
