import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class SpaceInvaders extends JPanel implements ActionListener, KeyListener {
    // Board dimensions and tile size
    int tileSize = 32; // Size of each tile
    int rows = 16; // Number of rows on the board
    int columns = 16; // Number of columns on the board
    int boardWidth = tileSize * columns; // Width of the board
    int boardHeight = tileSize * rows; // Height of the board

    // Images for different game elements
    Image shipImg; // Image for the ship
    Image alienImg; // Image for the standard alien
    Image alienCyanImg; // Image for the cyan alien
    Image alienMagentaImg; // Image for the magenta alien
    Image alienYellowImg; // Image for the yellow alien
    ArrayList<Image> alienImgArray; // ArrayList to hold alien images

    // Block class to represent game objects like ship, aliens, and bullets
    class Block {
        int x; // X-coordinate
        int y; // Y-coordinate
        int width; // Width of the block
        int height; // Height of the block
        Image img; // Image representing the block
        boolean alive = true; // Indicates if the block is alive
        boolean used = false; // Indicates if the block has been used (e.g., bullet)
        int hp = 1; // Health points for aliens
        boolean isAlienBullet = false; // True if this block is an alien bullet

        // Constructor to initialize block attributes
        Block(int x, int y, int width, int height, Image img) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.img = img;
        }

        // Default constructor
        Block() {}
    }

    // Ship attributes
    int shipWidth = tileSize * 2; // Width of the ship
    int shipHeight = tileSize; // Height of the ship
    int shipX = tileSize * columns / 2 - tileSize; // Initial X position of the ship
    int shipY = tileSize * rows - tileSize * 2; // Initial Y position of the ship
    int shipVelocityX = tileSize; // Ship movement speed
    Block ship; // Instance of Block representing the ship

    // Aliens
    ArrayList<Block> alienArray; // ArrayList to hold alien blocks
    int alienWidth = tileSize * 2; // Width of the alien
    int alienHeight = tileSize; // Height of the alien
    int alienX = tileSize; // X position of the first alien
    int alienY = tileSize; // Y position of the first alien
    int alienRows = 2; // Number of rows of aliens
    int alienColumns = 3; // Number of columns of aliens
    int alienCount = 0; // Total number of aliens
    int alienVelocityX = 1; // Speed at which aliens move horizontally

    // Bullets
    ArrayList<Block> bulletArray; // ArrayList to hold player bullets
    ArrayList<Block> alienBulletArray; // ArrayList to hold alien bullets
    int bulletWidth = tileSize / 8; // Width of the bullet
    int bulletHeight = tileSize / 2; // Height of the bullet
    int bulletVelocityY = -10; // Speed at which player bullets move vertically
    int alienBulletVelocityY = 10; // Speed at which alien bullets move vertically

    Timer gameLoop; // Timer to control the game loop
    boolean gameOver = false; // Flag to check if the game is over
    int score = 0; // Player score
    int highScore = 0; // Highest score achieved

    // Constructor to initialize game settings
    SpaceInvaders() {
        setPreferredSize(new Dimension(boardWidth, boardHeight)); // Set panel size
        setBackground(Color.black); // Set background color
        setFocusable(true); // Make panel focusable to receive key events
        addKeyListener(this); // Add key listener for keyboard input

        // Load images for ship and aliens
        shipImg = new ImageIcon(getClass().getResource("./ship.png")).getImage();
        alienImg = new ImageIcon(getClass().getResource("./alien.png")).getImage();
        alienCyanImg = new ImageIcon(getClass().getResource("./alien-cyan.png")).getImage();
        alienMagentaImg = new ImageIcon(getClass().getResource("./alien-magenta.png")).getImage();
        alienYellowImg = new ImageIcon(getClass().getResource("./alien-yellow.png")).getImage();

        // Add alien images to the ArrayList
        alienImgArray = new ArrayList<Image>();
        alienImgArray.add(alienImg);
        alienImgArray.add(alienCyanImg);
        alienImgArray.add(alienMagentaImg);
        alienImgArray.add(alienYellowImg);

        // Initialize ship block
        ship = new Block(shipX, shipY, shipWidth, shipHeight, shipImg);

        // Initialize ArrayLists for aliens and bullets
        alienArray = new ArrayList<Block>();
        bulletArray = new ArrayList<Block>();
        alienBulletArray = new ArrayList<Block>();

        // Set up the game loop timer (60 FPS)
        gameLoop = new Timer(1000 / 60, this);
        createAliens(); // Create initial set of aliens
        gameLoop.start(); // Start the game loop
    }

    // Paint component to draw game elements
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g); // Call the superclass method
        draw(g); // Draw the game elements
    }

    // Draw game elements on the screen
    public void draw(Graphics g) {
        // Draw the ship
        g.drawImage(ship.img, ship.x, ship.y, ship.width, ship.height, null);

        // Draw aliens
        for (Block alien : alienArray) {
            if (alien.alive) {
                g.drawImage(alien.img, alien.x, alien.y, alien.width, alien.height, null);
            }
        }

        // Draw player bullets
        g.setColor(Color.white);
        for (Block bullet : bulletArray) {
            if (!bullet.used) {
                g.fillRect(bullet.x, bullet.y, bullet.width, bullet.height);
            }
        }

        // Draw alien bullets
        g.setColor(Color.red);
        for (Block alienBullet : alienBulletArray) {
            if (!alienBullet.used) {
                g.fillRect(alienBullet.x, alienBullet.y, alienBullet.width, alienBullet.height);
            }
        }

        // Draw score and high score
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        g.drawString("Score: " + score, 10, 35);
        g.drawString("High Score: " + highScore, 10, 70);

        // Display game over message if the game is over
        if (gameOver) {
            g.drawString("Game Over", boardWidth / 2 - 80, boardHeight / 2);
        }
    }

    // Move game elements and handle game logic
    public void move() {
        // Move aliens
        for (Block alien : alienArray) {
            if (alien.alive) {
                alien.x += alienVelocityX;

                // Change direction if aliens hit the board edges
                if (alien.x + alien.width >= boardWidth || alien.x <= 0) {
                    alienVelocityX *= -1;
                    alien.x += alienVelocityX * 2;

                    // Move all aliens up by one row
                    for (Block a : alienArray) {
                        a.y += alienHeight;
                    }
                }

                // Check if aliens have reached the ship
                if (alien.y >= ship.y) {
                    gameOver = true;
                }
            }
        }

        // Move player bullets
        for (Block bullet : bulletArray) {
            bullet.y += bulletVelocityY;

            // Check collision with aliens
            for (Block alien : alienArray) {
                if (!bullet.used && alien.alive && detectCollision(bullet, alien)) {
                    bullet.used = true;
                    alien.hp--;
                    if (alien.hp <= 0) {
                        alien.alive = false;
                        alienCount--;
                        score += 100; // Award points for destroying an alien
                    }
                }
            }
        }

        // Move alien bullets
        for (Block alienBullet : alienBulletArray) {
            alienBullet.y += alienBulletVelocityY;

            // Check collision with the ship
            if (!alienBullet.used && detectCollision(alienBullet, ship)) {
                gameOver = true;
            }
        }

        // Remove used or off-screen bullets
        bulletArray.removeIf(bullet -> bullet.used || bullet.y < 0);
        alienBulletArray.removeIf(alienBullet -> alienBullet.used || alienBullet.y > boardHeight);

        // Alien shooting logic
        Random random = new Random();
        if (random.nextInt(100) < 2) { // Adjust probability as needed
            shootAlienBullet();
        }

        // Check if all aliens are destroyed to proceed to the next level
        if (alienCount == 0) {
            score += alienColumns * alienRows * 100; // Bonus points
            alienColumns = Math.min(alienColumns + 1, columns - 1);
            alienRows = Math.min(alienRows + 1, rows - 6);
            alienArray.clear();
            bulletArray.clear();
            alienBulletArray.clear();
            createAliens();
        }
    }

    // Shoot a bullet from a random alien
    public void shootAlienBullet() {
        Random random = new Random();
        int index = random.nextInt(alienArray.size());
        Block alien = alienArray.get(index);

        if (alien.alive) {
            Block alienBullet = new Block(
                alien.x + alien.width / 2 - bulletWidth / 2, // X position of the bullet
                alien.y + alien.height, // Y position of the bullet
                bulletWidth, // Width of the bullet
                bulletHeight, // Height of the bullet
                null // Image for the bullet (not used in this case)
            );
            alienBullet.isAlienBullet = true;
            alienBulletArray.add(alienBullet); // Add bullet to the list
        }
    }

    // Create initial set of aliens
    public void createAliens() {
        alienCount = 0;
        for (int i = 0; i < alienRows; i++) {
            for (int j = 0; j < alienColumns; j++) {
                Block alien = new Block(
                    alienX + j * (alienWidth + 10), // X position of the alien
                    alienY + i * (alienHeight + 10), // Y position of the alien
                    alienWidth, // Width of the alien
                    alienHeight, // Height of the alien
                    alienImgArray.get(new Random().nextInt(alienImgArray.size())) // Random image for the alien
                );
                alienArray.add(alien);
                alienCount++;
            }
        }
    }

    // Detect collision between two blocks
    public boolean detectCollision(Block a, Block b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    // Handle key events
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_LEFT) {
            ship.x -= shipVelocityX; // Move ship left
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            ship.x += shipVelocityX; // Move ship right
        } else if (keyCode == KeyEvent.VK_SPACE) {
            shootBullet(); // Shoot a bullet
        }
    }

    // Shoot a bullet from the ship
    public void shootBullet() {
        Block bullet = new Block(
            ship.x + ship.width / 2 - bulletWidth / 2, // X position of the bullet
            ship.y - bulletHeight, // Y position of the bullet
            bulletWidth, // Width of the bullet
            bulletHeight, // Height of the bullet
            null // Image for the bullet (not used in this case)
        );
        bulletArray.add(bullet); // Add bullet to the list
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            move(); // Move game elements and handle game logic
            repaint(); // Repaint the panel to update the display
        }
    }

    // Main method to create and run the game
    public static void main(String[] args) {
        JFrame frame = new JFrame("Space Invaders"); // Create a new JFrame
        SpaceInvaders game = new SpaceInvaders(); // Create a new game instance
        frame.add(game); // Add the game panel to the frame
        frame.pack(); // Pack the frame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Exit on close
        frame.setVisible(true); // Make the frame visible
    }
}
