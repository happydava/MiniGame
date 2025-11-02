// java
package bullet.bullets;

import bullet.Bullet;
import hero.Hero;

public class MagicThing extends Bullet {
    public MagicThing(Hero enemy) {
        super("magic.png", 18, enemy);
    }
}
