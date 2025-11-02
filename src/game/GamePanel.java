package game;

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
    private Timer timer;
    private String winnerMessage = "";
    private AttackStrategy player1AttackStrategy;
    private AttackStrategy player2AttackStrategy;
    private Image hero1Image;
    private Image hero2Image;

    private static List<Moveable> moveables = new ArrayList<>();
    private static List<Rendered> rendereds = new ArrayList<>();

    public GamePanel() {
        setBackground(Color.CYAN);
        setPreferredSize(new Dimension(1000, 600));
        setFocusable(true);

        // Загрузка изображений героев
        hero1Image = loadImage("Hero1.png");
        hero2Image = loadImage("Hero2.png");

        // Инициализация таймера
        timer = new Timer(1000 / 60, e -> {
            moveMovables();
            checkWinConditions();
            player1.updatePosition();  // Обновляем позицию игрока 1
            player2.updatePosition();  // Обновляем позицию игрока 2
            repaint();
        });

        // Инициализация игры
        restartGame();
    }

    private Image loadImage(String filename) {
        try {
            BufferedImage image = ImageIO.read(getClass().getResource("/" + filename));
            return image.getScaledInstance(150, 150, Image.SCALE_SMOOTH); // 50x50 для героев
        } catch (IOException e) {
            System.err.println("Не удалось загрузить изображение: " + filename);
            return null;
        }
    }

    public void restartGame() {
        // Покажите экран выбора персонажа
        showCharacterSelection();
        // Создание игроков с привязанными клавишами
        player2 = new Hero("Player 1", 100, hero1Image, 100, 300, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN, 10, player1AttackStrategy, KeyEvent.VK_SPACE);
        player1 = new Hero("Player 2", 100, hero2Image, 500, 300, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_S, 10, player2AttackStrategy, KeyEvent.VK_ENTER);
        addKeyListener(player1); // Добавляем KeyListener для игрока 1
        addKeyListener(player2); // Добавляем KeyListener для игрока 2
        setFocusable(true); // Убедитесь, что панель фокусируется для обработки ввода

        winnerMessage = "";

        // Перезапуск таймера
        if (!timer.isRunning()) {
            timer.start();
        }
    }
    private void showCharacterSelection() {
        // Выбор для первого игрока
        String[] options = {"1. Archer", "2. Mage", "3. Warrior"};
        int selection = JOptionPane.showOptionDialog(this,
                "Choose your character for Player 1:",
                "Character Selection",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);

        switch (selection) {
            case 0: // Archer
                player1AttackStrategy = new AttackStrategy(new Arrow(player2));
                break;
            case 1: // Mage
                player1AttackStrategy = new AttackStrategy(new MagicThing(player2));
                break;
            case 2: // Warrior
                player1AttackStrategy = new AttackStrategy(new Cigarette(player2));
                break;
            default:
                player1AttackStrategy = new AttackStrategy(new Arrow(player2));
        }

        // Выбор для второго игрока
        int selection2 = JOptionPane.showOptionDialog(this,
                "Choose your character for Player 2:",
                "Character Selection",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);

        switch (selection2) {
            case 0: // Archer
                player2AttackStrategy = new AttackStrategy(new Arrow(player2));
                break;
            case 1: // Mage
                player2AttackStrategy = new AttackStrategy(new MagicThing(player2));
                break;
            case 2: // Warrior
                player2AttackStrategy = new AttackStrategy(new Cigarette(player2));
                break;
            default:
                player2AttackStrategy = new AttackStrategy(new Arrow(player2));
        }

        // Теперь у каждого игрока есть своя стратегия атаки
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Отображение здоровья
        g.setColor(Color.WHITE);
        g.drawString("Player 1 Health: " + player1.getHealth(), 20, 50);
        g.drawString("Player 2 Health: " + player2.getHealth(), 20, 100);
        GradientPaint gradient = new GradientPaint(0, 0, Color.BLUE, 0, getHeight(), Color.CYAN);
        ((Graphics2D) g).setPaint(gradient);
        g.fillRect(0, 0, getWidth(), getHeight());
        for (Rendered rendered : rendereds) {
            rendered.render(g, this);
        }

        // Рисуем героев
//        if (player1.getImage() != null) {
//            g.drawImage(player1.getImage(), player1.getX(), player1.getY(), this);
//        }
//        if (player2.getImage() != null) {
//            g.drawImage(player2.getImage(), player2.getX(), player2.getY(), this);
//        }
//
//        // Рисуем пули
//        for (Bullet bullet : bulletsPlayer1) {
//            if (bullet.getBulletImage() != null) {
//                g.drawImage(bullet.getBulletImage(), bullet.getX(), bullet.getY(), this);
//            }
//        }
//        for (Bullet bullet : bulletsPlayer2) {
//            if (bullet.getBulletImage() != null) {
//                g.drawImage(bullet.getBulletImage(), bullet.getX(), bullet.getY(), this);
//            }
//        }

        // Сообщение о победе
        if (!winnerMessage.isEmpty()) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString(winnerMessage, 300, 300);
            g.drawString("Press R to Restart", 300, 350);
        }

        // Информация о кнопках
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Press 1 for Melee Attack", 20, 500);
        g.drawString("Press 2 for Magic Attack", 20, 530);
        g.drawString("Press 3 for Ranged Attack", 20, 560);
    }

    private void moveMovables() {
        for (Moveable moveable : moveables) {
            moveable.move();
        }
    }

    private void checkWinConditions() {
        if (player1.getHealth() <= 0) {
            winnerMessage = "Player 2 Wins!";
            timer.stop();
        } else if (player2.getHealth() <= 0) {
            winnerMessage = "Player 1 Wins!";
            timer.stop();
        }
    }

    public void stopGame() {
        timer.stop();
    }

    public static void addMoveable(Moveable moveable) {
        moveables.add(moveable);
        System.out.println("asdasd");
    }

    public static void addRendered(Rendered rendered) {
        rendereds.add(rendered);
    }

    // Методы для доступа к героям
    public Hero getPlayer1() {
        return player1;
    }

    public Hero getPlayer2() {
        return player2;
    }
}
