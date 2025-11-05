// src/bullet/Bullet.java
package bullet;

import entity.Moveable;
import entity.Rendered;
import game.GamePanel;
import hero.Hero;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public abstract class Bullet implements Moveable, Rendered {
    // теперь позиция и направление с плавающей точкой для точного прицеливания
    private double x, y;
    private double directionX, directionY;
    private double speed = 12.0;
    private boolean alive = true;

    protected Hero enemy;
    public int damage;
    private String bulletImageFilename;
    private Image bulletImage;

    public Bullet(String bulletImageFilename, int damage, Hero enemy) {
        this.enemy = enemy;
        this.damage = damage;
        this.bulletImageFilename = bulletImageFilename;
    }

    // initialize position & register to game lists
    // принимаем double направления/координаты (int автоматически приводится)
    public Bullet createBullet(double x, double y, double directionX, double directionY) {
        this.x = x;
        this.y = y;
        this.directionX = directionX;
        this.directionY = directionY;
        this.bulletImage = loadImage(this.bulletImageFilename);
        GamePanel.addRendered(this);
        GamePanel.addMoveable(this);
        return this;
    }

    protected Image loadImage(String filename) {
        try {
            BufferedImage image = ImageIO.read(getClass().getResource("/" + filename));
            return image.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Не удалось загрузить изображение: " + filename);
            return null;
        }
    }

    @Override
    public void move() {
        if (!alive) return;

        x += directionX * speed;
        y += directionY * speed;

        // check bounds against actual game panel size (0..width/height)
        int gw = GamePanel.getGameWidth();
        int gh = GamePanel.getGameHeight();
        int bulletW = 24;
        int bulletH = 24;
        if (getX() < 0 || getX() + bulletW > gw || getY() < 0 || getY() + bulletH > gh) {
            destroy();
            return;
        }

        onHit();
    }

    public int getX() { return (int) Math.round(x); }
    public int getY() { return (int) Math.round(y); }
    public Hero getEnemy() { return enemy; }

    public Rectangle getBounds() {
        return new Rectangle(getX(), getY(), 24, 24);
    }

    public Image getBulletImage() {
        return bulletImage;
    }

    public void onHit() {
        if (enemy != null && alive && this.getBounds().intersects(enemy.getBounds())) {
            enemy.decreaseHealth(damage);
            destroy();
        }
    }

    public void destroy() {
        if (!alive) return;
        alive = false;
        GamePanel.removeMoveable(this);
        GamePanel.removeRendered(this);
    }

    @Override
    public void render(Graphics graphics, JPanel observer) {
        if (bulletImage != null && alive) {
            graphics.drawImage(this.bulletImage, this.getX(), this.getY(), observer);
        }
    }
}
