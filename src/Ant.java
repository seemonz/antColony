// each Ant will work through the TSP instance until finding a hamilton cycle(one visit to each and every node)
public class Ant {
    private int[] path; // ant path through the TSP space
    private int[] visited; // the visited nodes of the ant, we put a 1 if it's visited those
    private float pheromone = 0.1f; // some value for the pheromone
    private int startNode;
    private int count = 0; // counts the number of nodes visited so far
    private int order = 0; // the number corresponds to the order visited
    private int currentNode;

    //constructor
    Ant(int size, int startNode) { // size is the size of the input list of nodes, the number of nodes
       path = new int[size];
       visited = new int[size];
       this.startNode = startNode;
       this.currentNode = startNode;
    }

    // visit the next node
    public void visitNode(int node) {
    }

    public int getStartNode() {
        return startNode;
    }

    public int[] getVisited() {
        return visited;
    }

    public void setVisited(int[] visited) {
        this.visited = visited;
    }

    public int[] getPath() {
        return path;
    }

    public void setPath(int[] path) {
        this.path = path;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(int currentNode) {
        this.currentNode = currentNode;
    }
}
