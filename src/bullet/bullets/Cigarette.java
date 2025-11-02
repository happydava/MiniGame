// java
package bullet.bullets;

import bullet.Bullet;
import hero.Hero;

public class Cigarette extends Bullet {
    public Cigarette(Hero enemy) {
        super("cigarette.png", 25, enemy);
    }
}
