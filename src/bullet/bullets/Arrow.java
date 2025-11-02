// java
package bullet.bullets;

import bullet.Bullet;
import hero.Hero;

public class Arrow extends Bullet {
    public Arrow(Hero enemy) {
        super("bullet.png", 10, enemy);
    }
}
