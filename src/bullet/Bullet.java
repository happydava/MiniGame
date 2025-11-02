// java
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
    private int x, y;
    private int directionX, directionY;
    private int speed = 12;
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
    public Bullet createBullet(int x, int y, int directionX, int directionY) {
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

        // check bounds
        if (x < -50 || x > 1100 || y < -50 || y > 800) {
            destroy();
            return;
        }

        onHit();
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public Hero getEnemy() { return enemy; }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 24, 24);
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
            graphics.drawImage(this.bulletImage, this.x, this.y, observer);
        }
    }
}
