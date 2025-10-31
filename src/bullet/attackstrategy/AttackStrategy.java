package bullet.attackstrategy;

import bullet.Bullet;

public class AttackStrategy<T extends Bullet> {
    public T bullet;

    public AttackStrategy(T bullet) {
        this.bullet = bullet;
    }
}
