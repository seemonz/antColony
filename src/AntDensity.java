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

    @Override // override the antSystem version of stepAnts, implement pheromone laying during each step
    protected void stepAnts(TSP tspInstance, ArrayList<Ant> ants) {
        for(int i = 0; i < ants.size(); i++) {
            // get currentAnt and get currentChoice for it
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

    // evaporate some amount of the pheromone off all edges of the TSP, rate is a percentage ie 0.05 is 5%
    protected void evaporate(TSP tspInstance, float rate) {
        // decrease all pheromone on all edges by rate amount
        for(int i = 0; i < tspInstance.getSize(); i++) {
            for(int j = 0; j < tspInstance.getSize(); j++) {
                tspInstance.getNodePheromone()[i][j] = tspInstance.getNodePheromone()[i][j]*(1.0f - rate);
            }
        }
    }

   // cycle will iterate through all nodes of the TSP for each ant in ants
   private double cyclePrivate() {
        // initialize ants in list
        ArrayList<Ant> ants = initializeAnts(tspInstance);

       // for finding the shortestTour of the cycle
       double shortestTour = 1000000;
       int shortestTourIndex = 0;

        // run solver
        findSolutions(tspInstance, ants); // find tours for all ants

        // find the shortestTour from the cycle
        for(int i = 0; i < ants.size(); i++) {
            Ant anty = ants.get(i);
            double pathLen = pathLength(tspInstance, anty);

            // find shortestTour
            if(pathLen < shortestTour) {
                shortestTour = pathLen;
                shortestTourIndex = i;
            }
        }

        // evaporate pheromone at the end of cycle
        evaporate(tspInstance, evaporationParam);

        return shortestTour;
    }

    // public version of the cycle -- it will return the best-so-far tour found of however many cycles performed
    // takes in numOfCycles as input
    public double cycle(int numOfCycles) {
        double bestSoFarTour = 100000000; // init as a high value

        for(int i = 0; i < numOfCycles; i ++) {
            double thisTour = cyclePrivate();
            // if thisTour is shorter than the best-so-far then set new best-so-far
            if(bestSoFarTour > thisTour) {
                bestSoFarTour = thisTour;
            }
        }

        return bestSoFarTour;
    }
}
