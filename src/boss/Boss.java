package boss;

import bullet.bullets.BossShot;
import game.GamePanel;
import hero.Hero;

import java.awt.*;
import java.util.Random;

public class Boss extends Hero {
    private final Hero player1;
    private final Hero player2;
    private final Random rnd = new Random();

    private long lastMoveChange = 0;
    private final long moveChangeInterval = 600; // чаще меняет направление
    private int dirX = 0, dirY = 0;

    private long lastShot = 0;
    private final long shotCooldownMs = 700; // реже стреляет (увеличен интервал)

    public Boss(int x, int y, Image image, Hero player1, Hero player2) {
        // pass dummy key bindings and null attackStrategy; Boss will control itself
        super("Boss", 300, image, x, y, -1, -1, -1, -1, 5, null, -1);
        this.player1 = player1;
        this.player2 = player2;
        GamePanel.addRendered(this);
    }

    @Override
    public void updatePosition() {
        long now = System.currentTimeMillis();

        // random movement direction change
        if (now - lastMoveChange > moveChangeInterval) {
            lastMoveChange = now;
            dirX = rnd.nextInt(3) - 1; // -1,0,1
            dirY = rnd.nextInt(3) - 1;
        }

        // используем наследуемый move (изменяет скорость)
        move(dirX, dirY);

        // физика: трение и перемещение (аналогично героям)
        // (velX/velY и другие поля доступны, т.к. они protected в Hero)
        // применяем трение
        velX *= friction;
        velY *= friction;

        // ограничиваем скорость по maxSpeed (наследуется)
        double mag = Math.hypot(velX, velY);
        if (mag > maxSpeed) {
            velX = velX / mag * maxSpeed;
            velY = velY / mag * maxSpeed;
        }

        // обновляем позицию
        this.setPosition(Math.max(0, Math.min(GamePanel.getGameWidth() - getWidth(), getX() + (int)Math.round(velX))),
                Math.max(0, Math.min(GamePanel.getGameHeight() - getHeight(), getY() + (int)Math.round(velY))));

        // стрельба — одиночный выстрел, с увеличенным интервалом (более редкие выстрелы)
        if (now - lastShot > shotCooldownMs) {
            lastShot = now;
            Hero target = pickTarget();
            if (target != null) {
                int baseDirX = target.getX() < this.getX() ? -1 : 1;
                int baseDirY = Integer.compare(target.getY(), this.getY());

                BossShot center = new BossShot(target);
                center.createBullet(this.getX() + 40, this.getY() + 40, baseDirX, baseDirY);
            }
        }
    }

    private Hero pickTarget() {
        boolean p1alive = player1 != null && player1.getHealth() > 0;
        boolean p2alive = player2 != null && player2.getHealth() > 0;
        if (!p1alive && !p2alive) return null;
        if (p1alive && !p2alive) return player1;
        if (!p1alive && p2alive) return player2;
        return rnd.nextBoolean() ? player1 : player2;
    }
}