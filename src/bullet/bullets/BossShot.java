
package bullet.bullets;

import bullet.Bullet;
import hero.Hero;

public class BossShot extends Bullet {
    public BossShot(Hero enemy) {
        super("boss_shot.png", 15, enemy);
    }
}
