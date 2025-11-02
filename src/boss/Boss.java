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
    private final long moveChangeInterval = 1000;
    private int dirX = 0, dirY = 0;

    private long lastShot = 0;
    private final long shotCooldownMs = 900;

    public Boss(int x, int y, Image image, Hero player1, Hero player2) {
        // pass dummy key bindings and null attackStrategy; Boss will control itself
        super("Boss", 300, image, x, y, -1, -1, -1, -1, 3, null, -1);
        this.player1 = player1;
        this.player2 = player2;
        // ensure boss is rendered (Hero constructor already registers it), but keep explicit if needed:
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
        move(dirX, dirY); // uses Hero.move(dx,dy) with speed

        // random shooting at alive player
        if (now - lastShot > shotCooldownMs) {
            lastShot = now;
            Hero target = pickTarget();
            if (target != null) {
                BossShot b = new BossShot(target);
                int dir = target.getX() < this.getX() ? -1 : 1;
                b.createBullet(this.getX() + 40, this.getY() + 40, dir, 0);
            }
        }
    }

    private Hero pickTarget() {
        // prefer alive players; if one alive pick it; otherwise null
        boolean p1alive = player1 != null && player1.getHealth() > 0;
        boolean p2alive = player2 != null && player2.getHealth() > 0;
        if (!p1alive && !p2alive) return null;
        if (p1alive && !p2alive) return player1;
        if (!p1alive && p2alive) return player2;
        // both alive -> random
        return rnd.nextBoolean() ? player1 : player2;
    }
}
