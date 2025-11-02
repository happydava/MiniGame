// java
package bullet.attackstrategy;

import bullet.Bullet;
import java.util.function.Supplier;

public class AttackStrategy<T extends Bullet> {
    private final Supplier<T> factory;

    public AttackStrategy(Supplier<T> factory) {
        this.factory = factory;
    }

    public T createBullet() {
        return factory.get();
    }
}
