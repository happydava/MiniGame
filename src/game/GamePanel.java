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
    private String defeatMessage = "";

    private Image hero1Image;
    private Image hero2Image;
    private Image bossImage;

    private Image backgroundImage; // фон

    private static final List<Moveable> moveables = new ArrayList<>();
    private static final List<Rendered> rendereds = new ArrayList<>();

    // статические размеры игрового поля (актуализируются при изменении размера панели)
    private static volatile int gameWidth = 1000;
    private static volatile int gameHeight = 600;

    private volatile boolean paused = false;

    public GamePanel() {
        setBackground(Color.CYAN);
        setPreferredSize(new Dimension(1000, 600));
        setFocusable(true);

        hero1Image = loadImage("Hero1.png");
        hero2Image = loadImage("Hero2.png");
        bossImage = loadImage("Boss.png");

        // попытка загрузить фон (положите ваш файл resources/background.jpg)
        backgroundImage = loadBackgroundImage("background.jpg");

        // слушаем изменение размера, чтобы обновлять границы
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                gameWidth = getWidth();
                gameHeight = getHeight();
            }
        });

        // инициализируем статические значения
        gameWidth = getPreferredSize().width;
        gameHeight = getPreferredSize().height;

        timer = new Timer(1000 / 60, e -> {
            if (paused) return;

            moveMovables();
            if (player1 != null) player1.updatePosition();
            if (player2 != null) player2.updatePosition();
            if (boss != null) boss.updatePosition();

            // гарантируем, что игроки остаются в границах панели (на всякий случай)
            clampHeroToBounds(player1);
            clampHeroToBounds(player2);

            checkWinConditions();
            repaint();
        });

        setupRestartKeyBinding();
        setupEscKeyBinding();
        restartGame();
    }

    private void setupEscKeyBinding() {
        InputMap im = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = this.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "pauseMenu");
        am.put("pauseMenu", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // ставим на паузу и показываем диалог с вариантами
                paused = true;
                timer.stop();
                String[] options = {"Бороться до талого", "Уйти в деканат"};
                int res = JOptionPane.showOptionDialog(
                        GamePanel.this,
                        " Объявляется перекур",
                        "Перекур...",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        options,
                        options[0]
                );
                if (res == 1) {
                    // выход: корректно закрываем окно
                    java.awt.Window w = SwingUtilities.getWindowAncestor(GamePanel.this);
                    if (w != null) w.dispose();
                    System.exit(0);
                } else {
                    paused = false;
                    timer.start();
                    requestFocusInWindow();
                }
            }
        });
    }

    private void clampHeroToBounds(Hero h) {
        if (h == null) return;
        int x = h.getX();
        int y = h.getY();
        int w = h.getWidth();
        int hgt = h.getHeight();
        int nx = Math.max(0, Math.min(gameWidth - w, x));
        int ny = Math.max(0, Math.min(gameHeight - hgt, y));
        if (nx != x || ny != y) {
            h.setPosition(nx, ny);
        }
    }

    private Image loadBackgroundImage(String filename) {
        try {
            BufferedImage image = ImageIO.read(getClass().getResource("/" + filename));
            return image;
        } catch (IOException | IllegalArgumentException e) {
            // если фон не найден — будет градиент
            return null;
        }
    }

    // Позволяет программно установить фон в рантайме (например, после выбора пользователем)
    public void setBackgroundImage(Image img) {
        this.backgroundImage = img;
    }

    // Статические геттеры размеров игрового поля
    public static int getGameWidth() { return gameWidth; }
    public static int getGameHeight() { return gameHeight; }

    // ... остальной код без изменений, кроме paintComponent использования backgroundImage ...
    private int[] showCharacterSelection() {
        String[] options = {"1. Bullet", "2. Samsa", "3. Cigarette"};
        int selection = JOptionPane.showOptionDialog(
                this,
                "Какой сетап у Бахти :",
                "Preparation Selection",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        int selection2 = JOptionPane.showOptionDialog(
                this,
                "Какой сетап у Давида :",
                "Preparation Selection",
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
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "Пройти курс заново(");
        am.put("Пройти курс заново(", new AbstractAction() {
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
        paused = false;

        // Обновляем реальные размеры панели до создания игроков
        gameWidth = getWidth() > 0 ? getWidth() : getPreferredSize().width;
        gameHeight = getHeight() > 0 ? getHeight() : getPreferredSize().height;

        int[] selections = showCharacterSelection();
        int s1 = selections[0];
        int s2 = selections[1];

        // задаём стартовые позиции прямо при создании (чтобы не появлялись в углу)
        int p1x = Math.max(20, Math.min(gameWidth - 200, 100));
        int p1y = Math.max(20, Math.min(gameHeight - 200, gameHeight / 2 - 75));

        int p2x = Math.max(20, Math.min(gameWidth - 200, gameWidth - 250));
        int p2y = Math.max(20, Math.min(gameHeight - 200, gameHeight / 2 - 75));

        // create players
        player1 = new Hero("Бахти", 100, hero1Image, p1x, p1y,
                KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_S,
                10, null, KeyEvent.VK_SPACE);

        player2 = new Hero("Давид", 100, hero2Image, p2x, p2y,
                KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN,
                10, null, KeyEvent.VK_ENTER);

        // create boss (players exist already)
        boss = new Boss(gameWidth/2 - 75, 80, bossImage, player1, player2);

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

        // актуализируем размеры сразу
        gameWidth = getWidth();
        gameHeight = getHeight();
        winnerMessage = "";
        defeatMessage = "";

        timer.start();
    }

    private void removeKeyListeners() {
        for (KeyListener kl : this.getKeyListeners()) {
            this.removeKeyListener(kl);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            GradientPaint gradient = new GradientPaint(0, 0, Color.BLUE, 0, getHeight(), Color.CYAN);
            ((Graphics2D) g).setPaint(gradient);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

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
            g.drawString("Терпение препода: " + boss.getHealth(), getWidth()/2 - 70, 30);
            drawHealthBar(g, getWidth()/2 - 100, 40, 200, 20, boss.getHealth());
        }

        if (!winnerMessage.isEmpty() || !defeatMessage.isEmpty()) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 48));

            String msg = !winnerMessage.isEmpty() ? winnerMessage : defeatMessage;
            g.drawString(msg, getWidth() / 2 - 200, getHeight() / 2 - 20);

            g.setFont(new Font("Arial", Font.PLAIN, 24));

            String sub = !winnerMessage.isEmpty()
                    ? "Нажми R, чтобы пройти курс заново, тигры "
                    : "Нажми R, чтобы пересдать, мешки ";
            g.drawString(sub, getWidth() / 2 - 220, getHeight() / 2 + 30);
        }

        if (paused) {
            g.setColor(new Color(0,0,0,130));
            g.fillRect(0,0,getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Перекур", getWidth()/2 - 80, getHeight()/2);
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
            winnerMessage = "    + СТЕПА !";
            defeatMessage = "";
            timer.stop();
            return;
        }

        // boss wins if both players dead
        boolean p1dead = player1 == null || player1.getHealth() <= 0;
        boolean p2dead = player2 == null || player2.getHealth() <= 0;
        if (p1dead && p2dead) {
            defeatMessage = "    + ЛЕТНИК !";
            winnerMessage = "";
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

}
