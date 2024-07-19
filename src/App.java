import javax.swing.*;

public class App {
    public static void main(String[] args) throws Exception {
        // Define the size of each tile in pixels
        int tileSize = 32;

        // Define the number of rows and columns on the board
        int rows = 16;
        int columns = 16;

        // Calculate the width and height of the game board based on the tile size and number of rows/columns
        int boardWidth = tileSize * columns; // 32 * 16 = 512px
        int boardHeight = tileSize * rows; // 32 * 16 = 512px

        // Create a new JFrame to serve as the main window for the game
        JFrame frame = new JFrame("Space Invaders");

        // Set the size of the window to the calculated width and height
        frame.setSize(boardWidth, boardHeight);

        // Center the window on the screen
        frame.setLocationRelativeTo(null);

        // Disable the ability to resize the window
        frame.setResizable(false);

        // Set the default operation to exit the application when the window is closed
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create an instance of the SpaceInvaders class, which should be a JPanel or similar component containing the game logic and graphics
        SpaceInvaders spaceInvaders = new SpaceInvaders();

        // Add the SpaceInvaders component to the JFrame
        frame.add(spaceInvaders);

        // Pack the frame to fit the preferred size of its components
        frame.pack();

        // Request focus for the SpaceInvaders component, ensuring it can receive keyboard input
        spaceInvaders.requestFocus();

        // Make the window visible on the screen
        frame.setVisible(true);
    }
}
