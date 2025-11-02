// java
package hero;

import bullet.Bullet;
import bullet.attackstrategy.AttackStrategy;
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

    private AttackStrategy<? extends Bullet> attackStrategy;
    private int attackBind;

    private long lastShot = 0;
    private long shotCooldownMs = 300;

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
            AttackStrategy<? extends Bullet> attackStrategy,
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

    public void setAttackStrategy(AttackStrategy<? extends Bullet> strategy) {
        this.attackStrategy = strategy;
    }

    public String getName() { return name; }
    public int getHealth() { return health; }

    public void decreaseHealth(int damage) {
        health -= damage;
        if (health < 0) health = 0;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public void move(int dx, int dy) {
        x += dx * speed;
        y += dy * speed;
        x = Math.max(0, Math.min(x, 900));
        y = Math.max(0, Math.min(y, 450));
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 100, 100);
    }

    public Image getImage() { return image; }

    @Override
    public void keyTyped(java.awt.event.KeyEvent e) {}

    @Override
    public void keyPressed(java.awt.event.KeyEvent e) {
        if (e.getKeyCode() == leftBind) xInput = -1;
        if (e.getKeyCode() == rightBind) xInput = 1;
        if (e.getKeyCode() == upBind) yInput = -1;
        if (e.getKeyCode() == downBind) yInput = 1;

        if (e.getKeyCode() == attackBind) attack();
    }

    @Override
    public void keyReleased(java.awt.event.KeyEvent e) {
        if (e.getKeyCode() == leftBind || e.getKeyCode() == rightBind) xInput = 0;
        if (e.getKeyCode() == upBind || e.getKeyCode() == downBind) yInput = 0;
    }

    public void updatePosition() {
        move(xInput, yInput);
    }

    private void attack() {
        if (attackStrategy == null) return;
        long now = System.currentTimeMillis();
        if (now - lastShot < shotCooldownMs) return;
        lastShot = now;

        Bullet b = attackStrategy.createBullet();
        // decide direction based on enemy position (shoot towards enemy)
        int dirX = 1;
        if (b.getEnemy() != null && b.getEnemy().getX() < this.x) dirX = -1;
        b.createBullet(this.x + 40, this.y + 40, dirX, 0);
    }

    @Override
    public void render(Graphics graphics, JPanel observer) {
        graphics.drawImage(this.image, this.x, this.y, observer);
    }
}
