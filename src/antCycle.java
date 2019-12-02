import java.util.ArrayList;

// one of three working types of antSystems
// this one lays down pheromone after the whole tour has been done
// there are subClasses within this class where the pheromone laying is done differently
public class antCycle extends antSystem{

    public antCycle(TSP tsp) {
        super(tsp);
    }

    private static final double pherParam = 1; // param for pheromone laying equation

    // evaporate some amount of the pheromone off all edges of the tsp, rate is a percentage ie 0.05 is 5%
    protected void evaporate(TSP tspInstance, float rate) {
        // decrease all pheromone on all edges by rate amount
        for(int i = 0; i < tspInstance.getSize(); i++) {
            for(int j = 0; j < tspInstance.getSize(); j++) {
                tspInstance.getNodePheromone()[i][j] = tspInstance.getNodePheromone()[i][j]*(1.0f - rate);
            }
        }
    }

    // cycle run ants on the tsp and update pheromone with solutions
    public void cycle() {
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

        // we lay down pheromone for each ant solution found
        for(int i = 0; i < ants.size(); i++) {
            Ant currentAnt = ants.get(i);
            double currentTourLength = pathLength(tspInstance, currentAnt);

            // go through path of ant and lay down pheromone onto TSP pheromone = Q/lengthOfPath
            for(int k = 0; k < currentAnt.getPath().length; k++) {
                // if we are the last node, then we get edge back to start
                if (k == currentAnt.getPath().length - 1) {
                    int currentNode = currentAnt.getPath()[k];
                    int nextNode = currentAnt.getPath()[0];
                    double currentPher = tspInstance.getNodePheromone()[currentNode][nextNode];  // grab pheromone
                    currentPher = currentPher + (pherParam/currentTourLength); // update pheromone
                    tspInstance.getNodePheromone()[currentNode][nextNode] = currentPher;
                } else {
                    int currentNode = currentAnt.getPath()[k];
                    int nextNode = currentAnt.getPath()[k+1];
                    double currentPher = tspInstance.getNodePheromone()[currentNode][nextNode];  // grab pheromone
                    currentPher = currentPher + (pherParam/currentTourLength); // update pheromone
                    tspInstance.getNodePheromone()[currentNode][nextNode] = currentPher;
                }
            }
        }

        // apply evaporation to the tsp
        evaporate(tspInstance, 0.08f);
    }
}
