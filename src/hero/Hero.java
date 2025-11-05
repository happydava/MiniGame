package hero;


import bullet.Bullet;
import bullet.attackstrategy.AttackStrategy;
import bullet.effects.Explosion;
import entity.Rendered;
import game.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Hero implements Rendered, KeyListener {
    private final String name;
    private int health;
    private Image image;
    private int x, y;
    private final int leftKey, rightKey, upKey, downKey;
    private final int speed;
    private AttackStrategy<?> attackStrategy;
    private final int attackKey;

    private boolean leftPressed, rightPressed, upPressed, downPressed, attackPressed;
    private long lastAttack = 0;
    private final long attackCooldown = 300;

    private final int width = 150;
    private final int height = 150;

    // Физика движения
    protected double velX = 0;
    protected double velY = 0;
    protected double accel = 1.2;
    protected double friction = 0.82;
    protected double maxSpeed;

    private boolean dead = false;

    public Hero(String name, int health, Image image, int x, int y,
                int leftKey, int rightKey, int upKey, int downKey,
                int speed, AttackStrategy<?> attackStrategy, int attackKey) {
        this.name = name;
        this.health = health;
        this.image = image;
        this.x = x;
        this.y = y;
        this.leftKey = leftKey;
        this.rightKey = rightKey;
        this.upKey = upKey;
        this.downKey = downKey;
        this.speed = speed;
        this.attackStrategy = attackStrategy;
        this.attackKey = attackKey;

        this.maxSpeed = speed;
    }

    public void updatePosition() {
        if (dead || health <= 0) return;

        // управление через флаги клавиш: задаём ускорение
        int ax = 0, ay = 0;
        if (leftPressed) ax -= 1;
        if (rightPressed) ax += 1;
        if (upPressed) ay -= 1;
        if (downPressed) ay += 1;

        move(ax, ay); // изменяет скорость

        // применяем трение
        velX *= friction;
        velY *= friction;

        // ограничиваем скорость
        double mag = Math.hypot(velX, velY);
        if (mag > maxSpeed) {
            velX = velX / mag * maxSpeed;
            velY = velY / mag * maxSpeed;
        }

        // обновляем позицию по скорости
        this.x += (int) Math.round(velX);
        this.y += (int) Math.round(velY);

        // ограничиваем положение в пределах панели
        int gw = GamePanel.getGameWidth();
        int gh = GamePanel.getGameHeight();
        this.x = Math.max(0, Math.min(gw - width, this.x));
        this.y = Math.max(0, Math.min(gh - height, this.y));

        // атака — пуля получает одноразово направление к цели и летит по прямой
        if (attackPressed && attackStrategy != null) {
            long now = System.currentTimeMillis();
            if (now - lastAttack > attackCooldown) {
                lastAttack = now;
                Bullet b = (Bullet) attackStrategy.createBullet();
                if (b != null) {
                    double startX = this.x + width / 2.0;
                    double startY = this.y + height / 2.0;

                    // цель, переданная в фабрику пули (может быть boss)
                    Hero target = b.getEnemy();
                    double dirX = 1.0;
                    double dirY = 0.0;

                    if (target != null) {
                        double targetX = target.getX() + target.getWidth() / 2.0;
                        double targetY = target.getY() + target.getHeight() / 2.0;
                        double dx = targetX - startX;
                        double dy = targetY - startY;
                        double len = Math.hypot(dx, dy);
                        if (len == 0) { // защита от деления на ноль
                            dirX = 1.0; dirY = 0.0;
                        } else {
                            dirX = dx / len;
                            dirY = dy / len;
                        }
                    } else {
                        // если цель не задана — стреляем в сторону движения
                        double len = Math.hypot(velX, velY);
                        if (len > 0.1) {
                            dirX = velX / len;
                            dirY = velY / len;
                        } else {
                            dirX = 1.0; dirY = 0.0;
                        }
                    }

                    // создаём пулю с фиксированным направлением (пуля не будет корректировать путь)
                    b.createBullet(startX, startY, dirX, dirY);
                }
            }
        }
    }

    // теперь move меняет скорость (используется и игроком и боссом)
    public void move(int dirX, int dirY) {
        this.velX += dirX * accel;
        this.velY += dirY * accel;
    }

    public void setPosition(int nx, int ny) {
        this.x = nx;
        this.y = ny;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public String getName() { return name; }
    public int getHealth() { return health; }

    public void decreaseHealth(int d) {
        if (dead) return;
        this.health = Math.max(0, this.health - d);
        if (this.health == 0 && !dead) {
            die();
        }
    }

    private void die() {
        dead = true;
        // отключаем возможность атаки
        this.attackStrategy = null;

        // запускаем эффект взрыва в месте героя
        Explosion exp = new Explosion(this.x + width/2, this.y + height/2);
        GamePanel.addRendered(exp);
        GamePanel.addMoveable(exp);

        // удаляем героя из списка отрисовки
        GamePanel.removeRendered(this);
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public Rectangle getBounds() {
        if (dead) return new Rectangle(0,0,0,0);
        return new Rectangle(x, y, width, height);
    }

    public void setAttackStrategy(AttackStrategy<?> strat) {
        this.attackStrategy = strat;
    }

    @Override
    public void render(Graphics graphics, JPanel observer) {
        if (dead) return;
        if (image != null) {
            graphics.drawImage(image, x, y, observer);
        } else {
            graphics.setColor(Color.MAGENTA);
            graphics.fillRect(x, y, width, height);
        }
    }

    @Override
    public void keyTyped(java.awt.event.KeyEvent e) { }

    @Override
    public void keyPressed(java.awt.event.KeyEvent e) {
        if (dead) return;
        int kc = e.getKeyCode();
        if (kc == leftKey) leftPressed = true;
        if (kc == rightKey) rightPressed = true;
        if (kc == upKey) upPressed = true;
        if (kc == downKey) downPressed = true;
        if (kc == attackKey) attackPressed = true;
    }

    @Override
    public void keyReleased(java.awt.event.KeyEvent e) {
        if (dead) return;
        int kc = e.getKeyCode();
        if (kc == leftKey) leftPressed = false;
        if (kc == rightKey) rightPressed = false;
        if (kc == upKey) upPressed = false;
        if (kc == downKey) downPressed = false;
        if (kc == attackKey) attackPressed = false;
    }
}
