import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.util.*;

public class Maze {
    private final int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private final BufferedImage originalMazeImage;
    private final Point startPoint, endPoint;
    private MazeNode start;
    private MazeNode end;
    private final ArrayList<MazeNode> nodes = new ArrayList<>();
    private boolean[][] mazeGrid;

    public Maze(BufferedImage image, Color pathColour) {
        this.originalMazeImage = image;

        this.mazeGrid = convertImageToArray(image, pathColour);

        this.startPoint = findPoint(mazeGrid, 'N');
        this.endPoint = findPoint(mazeGrid, 'S');

    }

    public Maze(BufferedImage image, Color pathColour, char startSide, char endSide) {
        this.originalMazeImage = image;

        this.mazeGrid = convertImageToArray(image, pathColour);

        this.startPoint = findPoint(mazeGrid, startSide);
        this.endPoint = findPoint(mazeGrid, endSide);

    }

    public Maze(BufferedImage image, Color pathColour, int startX, Point startPoint, Point endPoint) {
        this.originalMazeImage = image;

        this.mazeGrid = convertImageToArray(image, pathColour);

        this.startPoint = startPoint;
        this.endPoint = endPoint;

    }

    MazeNode getStart() {
        return start;
    }

    MazeNode getEnd() {
        return end;
    }

    ArrayList<MazeNode> getNodeMap() {
        return (ArrayList<MazeNode>) nodes.clone();
    }

    private MazeNode getNode(Point pos) {
        return nodes.stream().filter(node -> node.getPosition().equals(pos)).findAny().get();
    }

    boolean[][] convertImageToArray(BufferedImage image, Color pathColour) {
        mazeGrid = new boolean[image.getWidth()][image.getHeight()];

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                int pixel = image.getRGB(i, j);
                Color pixelRGB = new Color(pixel & 0xff, (pixel & 0xff00) >> 8, (pixel & 0xff0000) >> 16);
                mazeGrid[i][j] = pixelRGB.equals(pathColour);
            }
        }

        return mazeGrid;
    }

    void findNodes() {
        MazeNode[][] nodeGrid = new MazeNode[mazeGrid.length][mazeGrid[0].length];

        for (int i = 0; i < mazeGrid.length; i++) {
            for (int j = 0; j < mazeGrid[0].length; j++) {
                if (mazeGrid[i][j]) {

                    boolean[] neighbours = new boolean[4];
                    for (int k = 0; k < 4; k++) {
                        try {
                            neighbours[k] = mazeGrid[i + dirs[k][0]][j + dirs[k][1]];
                        } catch (IndexOutOfBoundsException e) {
                            neighbours[k] = false;
                        }
                    }

                    if (!(
                            Arrays.equals(neighbours, new boolean[]{false, false, true, true})
                                    || Arrays.equals(neighbours, new boolean[]{true, true, false, false})
                    )) {

                        MazeNode newNode = new MazeNode(i, j);
                        nodeGrid[i][j] = newNode;
                        nodes.add(newNode);

                        if (neighbours[1]) {
                            connectNearest(nodeGrid, newNode, dirs[1][0], dirs[1][1]);
                        }
                        if (neighbours[3]) {
                            connectNearest(nodeGrid, newNode, dirs[3][0], dirs[3][1]);
                        }

                    }
                }
            }
        }
        nodeGrid = null;
        System.gc();
    }

    void updateStartEndNodes() {
        start = getNode(startPoint);
        end = getNode(endPoint);

        start.attemptSetPathLength(0, null);
    }

    private void connectNearest(MazeNode[][] grid, MazeNode node1, int speedX, int speedY) {
        for (int i = 1; true; i++) {
            if (grid[node1.getPosition().x + i * speedX][node1.getPosition().y + i * speedY] != null) {
                MazeNode node2 = grid[node1.getPosition().x + i * speedX][node1.getPosition().y + i * speedY];
                node1.addConnection(node2, i);
                node2.addConnection(node1, i);
                break;
            }
        }
    }

    private int[] walk(boolean[][] grid, int startX, int startY, int speedX, int speedY) {
        try {
            for (int i = 0; true; i++) {
                if (grid[startX + i * speedX][startY + i * speedY]) {
                    return new int[]{startX + i * speedX, startY + i * speedY, i};
                }
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("This shouldn't happen. Really, it shouldn't.");
            return null;
        }
    }

    Point findPoint(boolean[][] mazeGrid, char edge) {
        int[] walkData = new int[3];
        switch (edge) {
            case 'N':
                walkData = walk(mazeGrid, 0, 0, 1, 0);
                break;
            case 'S':
                walkData = walk(mazeGrid, 0, mazeGrid.length - 1, 1, 0);
                break;
            case 'W':
                walkData = walk(mazeGrid, 0, 0, 0, 1);
                break;
            case 'E':
                walkData = walk(mazeGrid, mazeGrid[0].length - 1, 0, 0, 1);
                break;

        }
        assert walkData != null;
        return new Point(walkData[0], walkData[1]);
    }

    BufferedImage drawSolution(ArrayList<MazeNode> solution, Color solutionColour, Color nodeColour) {
        ColorConvertOp cco = new ColorConvertOp(null);
        BufferedImage solutionImage = new BufferedImage(
                originalMazeImage.getWidth(),
                originalMazeImage.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );
        cco.filter(originalMazeImage, solutionImage);

        Graphics solutionGraphics = solutionImage.createGraphics();

        solutionGraphics.setColor(nodeColour);
        nodes.forEach(node -> solutionGraphics.drawLine(node.getPosition().x, node.getPosition().y, node.getPosition().x, node.getPosition().y));

        solutionGraphics.setColor(solutionColour);
        solution.forEach(step -> {
            MazeNode previous = step.getPreviousNode();
            solutionGraphics.drawLine(step.getPosition().x, step.getPosition().y, previous.getPosition().x, previous.getPosition().y);
        });

        return solutionImage;
    }

    static class MazeNode {
        private final ArrayList<Connection> connections = new ArrayList<>();

        private Point pos = new Point(0, 0);
        private int pathLength;
        private MazeNode previousNode;

        public MazeNode(int x, int y) {
            this.getPosition().x = x;
            this.getPosition().y = y;
            this.pathLength = Integer.MAX_VALUE;
        }

        ArrayList<Connection> getConnections() {
            return connections;
        }

        Point getPosition() {
            return pos;
        }

        int getPathLength() {
            return pathLength;
        }

        MazeNode getPreviousNode() {
            return previousNode;
        }

        void addConnection(MazeNode node, int distance) {
            connections.add(new Connection(node, distance));
        }

        boolean attemptSetPathLength(int newPathLength, MazeNode newPreviousNode) {
            assert newPathLength >= 0;
            if (newPathLength < pathLength) {
                pathLength = newPathLength;
                previousNode = newPreviousNode;
                return true;
            }
            return false;
        }

    }

    static class Connection {
        MazeNode dest;
        int distance;

        public Connection(MazeNode dest, int distance) {
            this.dest = dest;
            this.distance = distance;
        }
    }
}