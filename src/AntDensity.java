import java.util.ArrayList;

// a variation of the antSystem -- it lays down a fixed amount of pheromone locally (at each edge traversal)
public class AntDensity extends AntSystem {
    float evaporationParam;
    double fixedPheromone; // the fixed amount of pheromone that is layed down on each edge traversal

    // constructor
    public AntDensity(TSP tsp, float alpha, float beta, float evaporationParam, double fixedPheromone) {
        super(tsp, alpha, beta);
        this.evaporationParam = evaporationParam;
        this.fixedPheromone = fixedPheromone;
    }

    @Override // we override the antSystem version of stepAnts, we are going to implement pheromone laying during each step
    protected void stepAnts(TSP tspInstance, ArrayList<Ant> ants) {
        // lets just pick the greedy choice for each ant to start with
        for(int i = 0; i < ants.size(); i++) {
            // get currentAnt and get greedy choice for it
            Ant currentAnt = ants.get(i);
            int currentChoice = chooseEdge(tspInstance, currentAnt); // next node to move to from the currentNode (index in edge list)
            int currentNodeAntIndex = currentAnt.getCurrentNode(); // the currentNode index before stepping to next node

            // set the new currentNode and update path and visited and iterate count
            currentAnt.setCurrentNode(currentChoice);
            currentAnt.getVisited()[currentChoice] = 1;
            currentAnt.getPath()[currentAnt.getCount()] = currentChoice;
            currentAnt.setCount(currentAnt.getCount() + 1);

            // lay fixed pheromone amount down on the edge traversed by ant
            tspInstance.getNodePheromone()[currentNodeAntIndex][currentChoice] += fixedPheromone;
        }
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

    private double cyclePrivate() {
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

        // lay pheromone based on shortestTour
        Ant shortestAnt = ants.get(shortestTourIndex);
        // DEBUGGING
//        System.out.println("=================== ANT-DENSITY ===================");
//        System.out.println("shortestTour: " + shortestTour);
//        System.out.println("avgTour: " + tourSum/tspInstance.getSize());
        evaporate(tspInstance, evaporationParam);
        return shortestTour;
    }


    public double cycle(int numOfCycles) {
        double bestSoFarTour = 100000000;

        for(int i = 0; i < numOfCycles; i ++) {
            double thisTour = cyclePrivate();
            if(bestSoFarTour > thisTour) {
                bestSoFarTour = thisTour;
            }
        }

        // DEBUGGING
//        System.out.println("=================== ANT-DENSITY ===================");
//        System.out.println("Number of iterations: " + numOfCycles);
//        System.out.println("Best Tour found: " + bestSoFarTour);

        return bestSoFarTour;
    }
}
