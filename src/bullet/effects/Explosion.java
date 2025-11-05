package bullet.effects;


import entity.Moveable;
import entity.Rendered;
import game.GamePanel;

import javax.swing.*;
import java.awt.*;

public class Explosion implements Moveable, Rendered {
    private final int cx;
    private final int cy;
    private int tick = 0;
    private final int maxTicks = 36;

    public Explosion(int cx, int cy) {
        this.cx = cx;
        this.cy = cy;
    }

    @Override
    public void move() {
        tick++;
        if (tick >= maxTicks) {
            GamePanel.removeMoveable(this);
            GamePanel.removeRendered(this);
        }
    }

    @Override
    public void render(Graphics graphics, JPanel observer) {
        float progress = Math.min(1f, tick / (float) maxTicks);
        int maxR = 80;
        int r = (int) (progress * maxR);
        int alpha = (int) ((1f - progress) * 200);
        alpha = Math.max(0, Math.min(255, alpha));

        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 255f));
        g2.setColor(Color.ORANGE);
        g2.fillOval(cx - r / 2, cy - r / 2, r, r);
        g2.setColor(Color.YELLOW);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(cx - r / 2, cy - r / 2, r, r);
        g2.dispose();
    }
}
