import java.util.ArrayList;

// one of three working types of antSystems
// this one deposits pheromone after the whole tour has been completed
// there are subClasses within this class where the pheromone laying is done differently: elitist, MaxMin
public class AntCycle extends AntSystem{
    float evaporationParam;
    double pherParam; // param for pheromone laying equation -- 4 works well

    //constructor
    public AntCycle(TSP tsp, float alpha, float beta, float evaporationParam, double pherParam) {
        super(tsp, alpha, beta);
        this.evaporationParam = evaporationParam;
        this.pherParam = pherParam;
    }

    // evaporate some amount of the pheromone off all edges of the TSP, rate is a percentage ie 0.05 is 5%
    protected void evaporate(TSP tspInstance, float rate) {
        // decrease all pheromone on all edges by rate amount
        for(int i = 0; i < tspInstance.getSize(); i++) {
            for(int j = 0; j < tspInstance.getSize(); j++) {
                tspInstance.getNodePheromone()[i][j] = tspInstance.getNodePheromone()[i][j]*(1.0f - rate);
            }
        }
    }

    // cycle runs ants on the TSP and updates pheromone with solutions/tours
    private double cyclePrivate() {
        // initialize ants
        ArrayList<Ant> ants = initializeAnts(tspInstance);

        // tracks shortestTour of the cycle
        double shortestTour = 1000000;
        int shortestTourIndex = 0;

        // run solver
        findSolutions(tspInstance, ants); // find tours of all ants

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

        // lay pheromone based on shortestTour
        Ant shortestAnt = ants.get(shortestTourIndex);

        // we lay down pheromone for each ant solution found
        for(int i = 0; i < ants.size(); i++) {
            Ant currentAnt = ants.get(i); // grab ant form ants
            double currentTourLength = pathLength(tspInstance, currentAnt); // get the tourLength of the currentAnt

            // go through path of ant and lay down pheromone onto TSP pheromone = pherParam/lengthOfPath for all antTours
            for(int k = 0; k < currentAnt.getPath().length; k++) {
                // if last node, then get edge that goes from currentNode to startingNode
                if (k == currentAnt.getPath().length - 1) {
                    int currentNode = currentAnt.getPath()[k];
                    int nextNode = currentAnt.getPath()[0];
                    double currentPher = tspInstance.getNodePheromone()[currentNode][nextNode];  // grab pheromone
                    currentPher = currentPher + (pherParam/currentTourLength); // update pheromone
                    tspInstance.getNodePheromone()[currentNode][nextNode] = currentPher;
                } else { // grab nextNode in the path
                    int currentNode = currentAnt.getPath()[k];
                    int nextNode = currentAnt.getPath()[k+1];
                    double currentPher = tspInstance.getNodePheromone()[currentNode][nextNode];  // grab pheromone
                    currentPher = currentPher + (pherParam/currentTourLength); // update pheromone
                    tspInstance.getNodePheromone()[currentNode][nextNode] = currentPher;
                }
            }
        }

        // apply evaporation to the TSP
        evaporate(tspInstance, evaporationParam);
        return shortestTour;
    }

    // public cycle function -- tracks the best-so-far tour and returns it
    // input: numOfCycles
    public double cycle(int numOfCycles) {
        double bestSoFar = 1000000000;

        // run for numOfCycles specified and keep track of the best found solution
        for(int i = 0; i < numOfCycles; i++) {
            double currentShortestPath = cyclePrivate();
            if(bestSoFar > currentShortestPath) {
                bestSoFar = currentShortestPath;
            }
        }

        return bestSoFar;
    }
}
