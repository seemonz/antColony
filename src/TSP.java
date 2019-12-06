// Class for holding the Traveling Salesman Problem
public class TSP {
    private float[][] nodeLocations; // 2d array of x, y positions of each node -- [node][x, y]
    private double[][] nodeDistances; // 2d array of the distance of each node to each. ie [node][each distance to every other node]
    private double[][] nodePheromone; // 2d array of pheromone on each edge -- [node][each edge pheromone
    private int size; // size is number of cities in the TSP - ie number of nodes in the graph

    // constructor
    TSP(float[][] input) {
        nodeLocations = new float[input.length][2];
        nodeDistances = new double[input.length][input.length];
        nodePheromone = new double[input.length][input.length];
        this.size = input.length;

        // feed in coordinates into nodeLocations
        for(int i = 0; i < input.length; i++) {
           nodeLocations[i][0] = input[i][0];
           nodeLocations[i][1] = input[i][1];
        }

        // initialize the distances between all nodes ie. the edge weights
        for(int i = 0; i < nodeLocations.length; i++) {
            for(int j = 0; j < nodeLocations.length; j++) {
               if (j == i) { // we are comparing the same node, distance is 0
                  nodeDistances[i][j] = 0;
               } else {
                   double distance = euclidieanDistance(nodeLocations[i][0], nodeLocations[i][1], nodeLocations[j][0], nodeLocations[j][1]);
                   nodeDistances[i][j] = distance;
               }
            }
        }
    }

    // size getter
    public int getSize() {
        return size;
    }

    // for calculating the distance between nodes
    private double euclidieanDistance(float x1, float y1, float x2, float y2) {
       float distance = 0;
       distance += Math.pow(x2 - x1, 2.0);
       distance += Math.pow(y2 - y1, 2.0);
       return Math.sqrt(distance);
    }

    // returns the pheromone array
    public double[][] getNodePheromone() {
        return nodePheromone;
    }

    // returns the nodeLocations array
    public float[][] getNodeLocations() {
        return nodeLocations;
    }

    // returns the edge distances array
    public double[][] getNodeDistances() {
        return nodeDistances;
    }

}
