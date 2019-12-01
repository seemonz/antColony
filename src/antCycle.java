import java.util.ArrayList;

// one of three working types of antSystems
// this one lays down pheromone after the whole tour has been done
// there are subClasses within this class where the pheromone laying is done differently
public class antCycle extends antSystem{

    public antCycle(TSP tsp) {
        super(tsp);
    }

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
}
