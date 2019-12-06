import java.util.ArrayList;

// AntQuantity applies pheromone locally (each edge traversal) based on the distance of the edge (inversely proportional to distance)
public class AntQuantity extends AntSystem {
    float evaporationaParam;
    double pherParam;

    // constructor
    public AntQuantity(TSP tsp, float alpha, float beta, float evaporationaParam, double pherParam) {
        super(tsp, alpha, beta);
        this.evaporationaParam = evaporationaParam;
        this.pherParam = pherParam;
    }

    @Override // we override the antSystem version of stepAnts, implement pheromone laying during each step
    protected void stepAnts(TSP tspInstance, ArrayList<Ant> ants) {
        // lets just pick the greedy choice for each ant to start with
        for(int i = 0; i < ants.size(); i++) {
            Ant currentAnt = ants.get(i);
            int currentChoice = chooseEdge(tspInstance, currentAnt); // next node to move to from the currentNode (index in edge list)
            int currentNodeAntIndex = currentAnt.getCurrentNode(); // the currentNode index before stepping to next node

            // set the new currentNode and update path and visited and iterate count
            currentAnt.setCurrentNode(currentChoice);
            currentAnt.getVisited()[currentChoice] = 1;
            currentAnt.getPath()[currentAnt.getCount()] = currentChoice;
            currentAnt.setCount(currentAnt.getCount() + 1);

            // lay fixed pheromone amount down on the edge traversed by ant
            double distOfEdge = tspInstance.getNodeDistances()[currentNodeAntIndex][currentChoice]; // this is the distance of the edge
            tspInstance.getNodePheromone()[currentNodeAntIndex][currentChoice] += pherParam/distOfEdge; //value inversely proportional to the distance of the edge
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
        // initialize new ants for each cycle
        ArrayList<Ant> ants = initializeAnts(tspInstance);

        // for finding the shortestTour of the cycle
        double shortestTour = 1000000;
        int shortestTourIndex = 0;

        // run solver
        findSolutions(tspInstance, ants); // find tours for all ants

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

        Ant shortestAnt = ants.get(shortestTourIndex);
        // after every cycle we evaporate some amount of pheromone off the TSP
        evaporate(tspInstance, evaporationaParam); // 0.03 was the val

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
