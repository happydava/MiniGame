package hero;

import bullet.Bullet;
import bullet.attackstrategy.AttackStrategy;
import bullet.bullets.Arrow;
import entity.Rendered;
import game.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Hero implements KeyListener, Rendered {
    private String name;
    private int health;
    private int speed;

    private int x, y;
    private Image image;

    private int xInput = 0;
    private int yInput = 0;

    private int leftBind;
    private int rightBind;
    private int upBind;
    private int downBind;

    private final AttackStrategy attackStrategy;
    private int attackBind;

    public Hero(
            String name,
            int health,
            Image image,
            int x,
            int y,

            int leftBind,
            int rightBind,
            int upBind,
            int downBind,

            int speed,

            AttackStrategy attackStrategy,
            int attackBind
    ) {
        this.name = name;
        this.health = health;
        this.image = image;
        this.x = x;
        this.y = y;
        this.leftBind = leftBind;
        this.rightBind = rightBind;
        this.upBind = upBind;
        this.downBind = downBind;
        this.speed = speed;
        this.attackStrategy = attackStrategy;
        this.attackBind = attackBind;

        GamePanel.addRendered(this);
    }

    public String getName() {
        return name;
    }

    public int getHealth() {
        return health;
    }

    public void decreaseHealth(int damage) {
        health -= damage;
        if (health < 0) health = 0;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void move(int dx, int dy) {
        x += dx * speed;
        y += dy * speed;

        x = Math.clamp(x, -10, 800);
        y = Math.clamp(y, 0, 400);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 100, 100); // Размер героя
    }

    public Image getImage() {
        return image;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }


    @Override
    public void keyPressed(KeyEvent e) {
        // Управление только для соответствующего игрока
        if (e.getKeyCode() == leftBind) xInput = -1; // Сдвиг влево
        if (e.getKeyCode() == rightBind) xInput = 1; // Сдвиг вправо
        if (e.getKeyCode() == upBind) yInput = -1; // Сдвиг вверх
        if (e.getKeyCode() == downBind) yInput = 1; // Сдвиг вниз

        if (e.getKeyCode() == attackBind) attack();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Останавливаем движение при отпускании клавиши
        if (e.getKeyCode() == leftBind || e.getKeyCode() == rightBind) xInput = 0;
        if (e.getKeyCode() == upBind || e.getKeyCode() == downBind) yInput = 0;
    }

    public void updatePosition() {
        move(xInput, yInput);  // Обновляем позицию персонажа
    }

    private void attack() {
        Bullet bullet = attackStrategy.bullet;

        bullet.createBullet(x, y, 1, 0);
    }

    @Override
    public void render(Graphics graphics, JPanel observer) {
        graphics.drawImage(this.image, this.x, this.y, observer);
    }
}
