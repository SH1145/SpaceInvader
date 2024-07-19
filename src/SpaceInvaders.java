import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class SpaceInvaders extends JPanel implements ActionListener, KeyListener {
    // Board dimensions and tile size
    int tileSize = 32;
    int rows = 16;
    int columns = 16;

    int boardWidth = tileSize * columns; // 32 * 16
    int boardHeight = tileSize * rows; // 32 * 16

    Image shipImg;
    Image alienImg;
    Image alienCyanImg;
    Image alienMagentaImg;
    Image alienYellowImg;
    ArrayList<Image> alienImgArray;

    class Block {
        int x;
        int y;
        int width;
        int height;
        Image img;
        boolean alive = true;
        boolean used = false;
        int hp = 1; // Health points for aliens
        boolean isAlienBullet = false; // True if this block is an alien bullet

        // Constructor
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
    int shipWidth = tileSize * 2;
    int shipHeight = tileSize;
    int shipX = tileSize * columns / 2 - tileSize;
    int shipY = tileSize * rows - tileSize * 2;
    int shipVelocityX = tileSize;
    Block ship;

    // Aliens
    ArrayList<Block> alienArray;
    int alienWidth = tileSize * 2;
    int alienHeight = tileSize;
    int alienX = tileSize;
    int alienY = tileSize;

    int alienRows = 2;
    int alienColumns = 3;
    int alienCount = 0;
    int alienVelocityX = 1;

    // Bullets
    ArrayList<Block> bulletArray;
    ArrayList<Block> alienBulletArray;
    int bulletWidth = tileSize / 8;
    int bulletHeight = tileSize / 2;
    int bulletVelocityY = -10;
    int alienBulletVelocityY = 10; // Alien bullets move downwards

    Timer gameLoop;
    boolean gameOver = false;
    int score = 0;
    int highScore = 0;

    SpaceInvaders() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.black);
        setFocusable(true);
        addKeyListener(this);

        // Load images
        shipImg = new ImageIcon(getClass().getResource("./ship.png")).getImage();
        alienImg = new ImageIcon(getClass().getResource("./alien.png")).getImage();
        alienCyanImg = new ImageIcon(getClass().getResource("./alien-cyan.png")).getImage();
        alienMagentaImg = new ImageIcon(getClass().getResource("./alien-magenta.png")).getImage();
        alienYellowImg = new ImageIcon(getClass().getResource("./alien-yellow.png")).getImage();

        alienImgArray = new ArrayList<Image>();
        alienImgArray.add(alienImg);
        alienImgArray.add(alienCyanImg);
        alienImgArray.add(alienMagentaImg);
        alienImgArray.add(alienYellowImg);

        ship = new Block(shipX, shipY, shipWidth, shipHeight, shipImg);

        alienArray = new ArrayList<Block>();
        bulletArray = new ArrayList<Block>();
        alienBulletArray = new ArrayList<Block>();

        // Game timer
        gameLoop = new Timer(1000 / 60, this);
        createAliens();
        gameLoop.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Ship
        g.drawImage(ship.img, ship.x, ship.y, ship.width, ship.height, null);

        // Aliens
        for (Block alien : alienArray) {
            if (alien.alive) {
                g.drawImage(alien.img, alien.x, alien.y, alien.width, alien.height, null);
            }
        }

        // Bullets
        g.setColor(Color.white);
        for (Block bullet : bulletArray) {
            if (!bullet.used) {
                g.fillRect(bullet.x, bullet.y, bullet.width, bullet.height);
            }
        }

        // Alien bullets
        g.setColor(Color.red);
        for (Block alienBullet : alienBulletArray) {
            if (!alienBullet.used) {
                g.fillRect(alienBullet.x, alienBullet.y, alienBullet.width, alienBullet.height);
            }
        }

        // Score
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        g.drawString("Score: " + score, 10, 35);
        g.drawString("High Score: " + highScore, 10, 70);

        if (gameOver) {
            g.drawString("Game Over", boardWidth / 2 - 80, boardHeight / 2);
        }
    }

    public void move() {
        // Alien movement
        for (Block alien : alienArray) {
            if (alien.alive) {
                alien.x += alienVelocityX;

                if (alien.x + alien.width >= boardWidth || alien.x <= 0) {
                    alienVelocityX *= -1;
                    alien.x += alienVelocityX * 2;

                    // Move all aliens up by one row
                    for (Block a : alienArray) {
                        a.y += alienHeight;
                    }
                }

                if (alien.y >= ship.y) {
                    gameOver = true;
                }
            }
        }

        // Bullet movement
        for (Block bullet : bulletArray) {
            bullet.y += bulletVelocityY;

            // Bullet collision with aliens
            for (Block alien : alienArray) {
                if (!bullet.used && alien.alive && detectCollision(bullet, alien)) {
                    bullet.used = true;
                    alien.hp--;
                    if (alien.hp <= 0) {
                        alien.alive = false;
                        alienCount--;
                        score += 100;
                    }
                }
            }
        }

        // Alien bullet movement
        for (Block alienBullet : alienBulletArray) {
            alienBullet.y += alienBulletVelocityY;

            // Alien bullet collision with ship
            if (!alienBullet.used && detectCollision(alienBullet, ship)) {
                gameOver = true;
            }
        }

        // Clear bullets
        bulletArray.removeIf(bullet -> bullet.used || bullet.y < 0);
        alienBulletArray.removeIf(alienBullet -> alienBullet.used || alienBullet.y > boardHeight);

        // Alien shooting
        Random random = new Random();
        if (random.nextInt(100) < 2) { // Adjust probability as needed
            shootAlienBullet();
        }

        // Next level
        if (alienCount == 0) {
            score += alienColumns * alienRows * 100; // Bonus points
            alienColumns = Math.min(alienColumns + 1, columns / 2 - 2);
            alienRows = Math.min(alienRows + 1, rows - 6);
            alienArray.clear();
            bulletArray.clear();
            alienBulletArray.clear();
            createAliens();
        }
    }

    public void shootAlienBullet() {
        Random random = new Random();
        int index = random.nextInt(alienArray.size());
        Block alien = alienArray.get(index);

        if (alien.alive) {
            Block alienBullet = new Block(
                alien.x + alien.width / 2 - bulletWidth / 2,
                alien.y + alien.height,
                bulletWidth,
                bulletHeight,
                null
            );
            alienBullet.isAlienBullet = true;
            alienBulletArray.add(alienBullet);
        }
    }

    public void createAliens() {
        Random random = new Random();
        for (int c = 0; c < alienColumns; c++) {
            for (int r = 0; r < alienRows; r++) {
                int randomImgIndex = random.nextInt(alienImgArray.size());
                Block alien = new Block(
                    alienX + c * alienWidth,
                    alienY + r * alienHeight,
                    alienWidth,
                    alienHeight,
                    alienImgArray.get(randomImgIndex)
                );
                alien.hp = 2; // Set initial HP for aliens
                alienArray.add(alien);
            }
        }
        alienCount = alienArray.size();
    }

    public boolean detectCollision(Block a, Block b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
            highScore = Math.max(score, highScore);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) {
            ship.x = shipX;
            bulletArray.clear();
            alienArray.clear();
            alienBulletArray.clear();
            gameOver = false;
            score = 0;
            alienColumns = 3;
            alienRows = 2;
            alienVelocityX = 1;
            createAliens();
            gameLoop.start();
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT && ship.x - shipVelocityX >= 0) {
            ship.x -= shipVelocityX;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && ship.x + shipVelocityX + ship.width <= boardWidth) {
            ship.x += shipVelocityX;
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            Block bullet = new Block(ship.x + shipWidth * 15 / 32, ship.y, bulletWidth, bulletHeight, null);
            bulletArray.add(bullet);
        }
    }
}
