import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

@SuppressWarnings("SpellCheckingInspection")
class Main {

    public static void main(String[] args) {
        String mazeImageFolder = "input";
        String mazeImageFile = "test2.bmp";
        String outputImageFolder = "output";
        String outputImageFile = mazeImageFile.split("\\.")[0] + ".png";
        BufferedImage mazeImage;
        Maze maze;

        System.out.println("Maze Solver by AnonymousKraken.\n");
        long startTime = System.currentTimeMillis();

        // Read image
        System.out.println("Reading image...");

        try {
            mazeImage = ImageIO.read(new File(mazeImageFolder + "/" + mazeImageFile));
            Color pathColour = Color.BLACK;
            maze = new Maze(mazeImage, pathColour, 'N', 'S');
            System.out.println("Read image.");
        } catch (IOException e) {
            System.out.println("Failed to read image.");
            throw new RuntimeException();
        }

        System.out.println("TIME: READ IMAGE IN " + (System.currentTimeMillis()-startTime) + "ms.\n");

        // Find nodes
        System.out.println("Finding nodes...");

        maze.findNodes();

        System.out.println("Found nodes.");
        System.out.println("TIME: FOUND NODES IN " + (System.currentTimeMillis()-startTime) + "ms.\n");

        // Get start and end nodes
        System.out.println("Updating start and end nodes...");

        maze.updateStartEndNodes();
        System.out.println("The maze starts at " + maze.getStart().getPosition());
        System.out.println("The maze ends at " + maze.getEnd().getPosition());

        System.out.println("Updated start and end.");
        System.out.println("TIME: UPDATED START AND END IN " + (System.currentTimeMillis()-startTime) + "ms.\n");
        // Solve maze
        System.out.println("Starting solve...");

        Dijkstra solver = new Dijkstra(maze);
        ArrayList<Maze.MazeNode> solution = solver.solve();

        System.out.println("Finished solve.");
        System.out.println("TIME: FINISHED SOLVE IN " + (System.currentTimeMillis()-startTime) + "ms.\n");

        // Output maze image
        System.out.println("Outputting image...");

        BufferedImage solvedMaze = maze.drawSolution(solution, new Color(0, 255, 0), new Color(255, 0, 0));
        try {
            ImageIO.write(solvedMaze, "png", new File(outputImageFolder + "/" + outputImageFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Output saved.");
        System.out.println("TIME: PROGRAM RAN IN " + (System.currentTimeMillis()-startTime) + "ms.\n");
        // solution.forEach(step -> System.out.println(step.getPosition().x + ", " + step.getPosition().y));

    }
}