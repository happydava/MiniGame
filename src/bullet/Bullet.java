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
    private int speed = 10;
    public int damage;
    private String bulletImageFilename;
    private Image bulletImage;
    protected Hero enemy;

    public Bullet(String bulletImageFilename, int damage, Hero enemy) {
        this.enemy = enemy;

        this.damage = damage;

        GamePanel.addRendered(this);
        GamePanel.addMoveable(this);

        this.bulletImageFilename = bulletImageFilename;
    }

    public Bullet createBullet(int x, int y, int directionX, int directionY) {
        this.x = x;
        this.y = y;
        this.directionX = directionX;
        this.directionY = directionY;

        this.bulletImage = loadImage(this.bulletImageFilename);
        return this;
    }

    protected Image loadImage(String filename) {
        try {
            BufferedImage image = ImageIO.read(getClass().getResource("/" + filename));
            return image.getScaledInstance(20, 20, Image.SCALE_SMOOTH); // 20x20 пикселей
        } catch (IOException e) {
            System.err.println("Не удалось загрузить изображение: " + filename);
            return null;
        }
    }

    @Override
    public void move() {
        x += directionX * speed;
        y += directionY * speed;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 20, 20); // Размер пули
    }

    public Image getBulletImage() {
        return bulletImage;
    }

    public void onHit() {
        if (this.getBounds().intersects(enemy.getBounds())) {
            enemy.decreaseHealth(damage);
        }
    }

//    if (this.getBounds().intersects(enemy.getBounds())) {
//        enemy.decreaseHealth(damage);
//    }

    @Override
    public void render(Graphics graphics, JPanel observer) {
        graphics.drawImage(this.bulletImage, this.x, this.y, observer);
    }
}
