import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Dijkstra {
    private final Maze maze;
    private PriorityQueue<Maze.MazeNode> priorityQueue;

    public Dijkstra(Maze maze) {
        this.priorityQueue = new PriorityQueue<Maze.MazeNode>(Comparator.comparing(Maze.MazeNode::getPathLength));
        this.priorityQueue.add(maze.getStart());
        this.maze = maze;
    }

    void updatePathways(Maze.MazeNode src) {
        src.getConnections().forEach(connection -> {
            // System.out.println("destNode: "+connection.getOtherNode(src));
            if (connection.dest.attemptSetPathLength(src.getPathLength()+connection.distance, src)) {
                if (!priorityQueue.contains(connection.dest)) {
                    priorityQueue.add(connection.dest);
                }
            }
        });
        // System.out.println(count.get()+" "+src.getPosition());
    }

    ArrayList<Maze.MazeNode> solve() {

        Maze.MazeNode min;
        updatePathways(maze.getStart());
        do {
            min = (Maze.MazeNode)priorityQueue.poll();
            updatePathways(min);
        } while (!min.equals(maze.getEnd()));

        ArrayList<Maze.MazeNode> solution = new ArrayList<>();
        Maze.MazeNode currentNode = maze.getEnd();
        while (!currentNode.equals(maze.getStart())) {
            solution.add(currentNode);
            currentNode = currentNode.getPreviousNode();
        }
        Collections.reverse(solution);

        return solution;
    }
}