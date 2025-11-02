package game;

import boss.Boss;
import bullet.attackstrategy.AttackStrategy;
import bullet.bullets.Arrow;
import bullet.bullets.Cigarette;
import bullet.bullets.MagicThing;
import entity.Moveable;
import entity.Rendered;
import hero.Hero;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {
    private Hero player1;
    private Hero player2;
    private Boss boss;
    private Timer timer;
    private String winnerMessage = "";

    private Image hero1Image;
    private Image hero2Image;
    private Image bossImage;

    private static final List<Moveable> moveables = new ArrayList<>();
    private static final List<Rendered> rendereds = new ArrayList<>();

    public GamePanel() {
        setBackground(Color.CYAN);
        setPreferredSize(new Dimension(1000, 600));
        setFocusable(true);

        hero1Image = loadImage("Hero1.png");
        hero2Image = loadImage("Hero2.png");
        bossImage = loadImage("Boss.png");

        timer = new Timer(1000 / 60, e -> {
            moveMovables();
            if (player1 != null) player1.updatePosition();
            if (player2 != null) player2.updatePosition();
            if (boss != null) boss.updatePosition();
            checkWinConditions();
            repaint();
        });

        setupRestartKeyBinding();
        restartGame();
    }
    // java
// Add this method to `src/game/GamePanel.java`
    private int[] showCharacterSelection() {
        String[] options = {"1. Archer", "2. Mage", "3. Warrior"};
        int selection = JOptionPane.showOptionDialog(
                this,
                "Choose your character for Player 1:",
                "Character Selection",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        int selection2 = JOptionPane.showOptionDialog(
                this,
                "Choose your character for Player 2:",
                "Character Selection",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (selection < 0) selection = 0;
        if (selection2 < 0) selection2 = 0;
        return new int[]{selection, selection2};
    }

    private void setupRestartKeyBinding() {
        InputMap im = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = this.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "restartGame");
        am.put("restartGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!winnerMessage.isEmpty() || !timer.isRunning()) {
                    restartGame();
                }
            }
        });
    }

    private Image loadImage(String filename) {
        try {
            BufferedImage image = ImageIO.read(getClass().getResource("/" + filename));
            return image.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Не удалось загрузить изображение: " + filename);
            return null;
        }
    }

    public void restartGame() {
        timer.stop();
        synchronized (moveables) { moveables.clear(); }
        synchronized (rendereds) { rendereds.clear(); }
        removeKeyListeners();

        winnerMessage = "";

        int[] selections = showCharacterSelection();
        int s1 = selections[0];
        int s2 = selections[1];

        // create players
        player1 = new Hero("Player 1", 100, hero1Image, 100, 250,
                KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_S,
                6, null, KeyEvent.VK_SPACE);

        player2 = new Hero("Player 2", 100, hero2Image, 750, 250,
                KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN,
                6, null, KeyEvent.VK_ENTER);

        // create boss (players exist already)
        boss = new Boss(420, 120, bossImage, player1, player2);

        // players target the boss
        AttackStrategy<?> strat1 = switch (s1) {
            case 1 -> new AttackStrategy<>(() -> new MagicThing(boss));
            case 2 -> new AttackStrategy<>(() -> new Cigarette(boss));
            default -> new AttackStrategy<>(() -> new Arrow(boss));
        };

        AttackStrategy<?> strat2 = switch (s2) {
            case 1 -> new AttackStrategy<>(() -> new MagicThing(boss));
            case 2 -> new AttackStrategy<>(() -> new Cigarette(boss));
            default -> new AttackStrategy<>(() -> new Arrow(boss));
        };

        player1.setAttackStrategy(strat1);
        player2.setAttackStrategy(strat2);

        addKeyListener(player1);
        addKeyListener(player2);
        setFocusable(true);
        requestFocusInWindow();

        addRendered(player1);
        addRendered(player2);
        addRendered(boss);

        timer.start();
    }

    private void removeKeyListeners() {
        for (KeyListener kl : this.getKeyListeners()) {
            this.removeKeyListener(kl);
        }
    }

    // ... showCharacterSelection unchanged ...

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        GradientPaint gradient = new GradientPaint(0, 0, Color.BLUE, 0, getHeight(), Color.CYAN);
        ((Graphics2D) g).setPaint(gradient);
        g.fillRect(0, 0, getWidth(), getHeight());

        for (Rendered rendered : new ArrayList<>(rendereds)) {
            rendered.render(g, this);
        }

        // HUD: players and boss health
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        if (player1 != null) {
            g.drawString(player1.getName() + " HP: " + player1.getHealth(), 20, 30);
            drawHealthBar(g, 20, 40, 200, 20, player1.getHealth());
        }
        if (player2 != null) {
            g.drawString(player2.getName() + " HP: " + player2.getHealth(), getWidth() - 220, 30);
            drawHealthBar(g, getWidth() - 220, 40, 200, 20, player2.getHealth());
        }
        if (boss != null) {
            g.drawString("BOSS HP: " + boss.getHealth(), getWidth()/2 - 70, 30);
            drawHealthBar(g, getWidth()/2 - 100, 40, 200, 20, boss.getHealth());
        }

        if (!winnerMessage.isEmpty()) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString(winnerMessage, getWidth() / 2 - 200, getHeight() / 2 - 20);
            g.setFont(new Font("Arial", Font.PLAIN, 24));
            g.drawString("Press R to Restart", getWidth() / 2 - 120, getHeight() / 2 + 30);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("P1: WASD move, SPACE attack", 20, getHeight() - 40);
        g.drawString("P2: Arrow keys move, ENTER attack", 20, getHeight() - 20);
    }

    private void drawHealthBar(Graphics g, int x, int y, int w, int h, int hp) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y, w, h);
        int val = Math.max(0, Math.min(100, hp));
        g.setColor(Color.GREEN);
        g.fillRect(x + 2, y + 2, (int) ((w - 4) * (val / 100.0)), h - 4);
        g.setColor(Color.WHITE);
        g.drawRect(x, y, w, h);
    }

    private void moveMovables() {
        for (Moveable moveable : new ArrayList<>(moveables)) {
            moveable.move();
        }
    }

    private void checkWinConditions() {
        // players win if boss dead
        if (boss != null && boss.getHealth() <= 0) {
            winnerMessage = "Players Win!";
            timer.stop();
            return;
        }
        // boss wins if both players dead
        boolean p1dead = player1 == null || player1.getHealth() <= 0;
        boolean p2dead = player2 == null || player2.getHealth() <= 0;
        if (p1dead && p2dead) {
            winnerMessage = "Boss Wins!";
            timer.stop();
        }
    }

    // static registration methods used by bullets & heroes
    public static void addMoveable(Moveable moveable) {
        synchronized (moveables) { moveables.add(moveable); }
    }

    public static void addRendered(Rendered rendered) {
        synchronized (rendereds) { rendereds.add(rendered); }
    }

    public static void removeMoveable(Moveable moveable) {
        synchronized (moveables) { moveables.remove(moveable); }
    }

    public static void removeRendered(Rendered rendered) {
        synchronized (rendereds) { rendereds.remove(rendered); }
    }

    public Hero getPlayer1() { return player1; }
    public Hero getPlayer2() { return player2; }
}