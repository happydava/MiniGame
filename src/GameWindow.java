
import game.GamePanel;

import javax.swing.*;

public class GameWindow {
    public GameWindow() {
        JFrame frame = new JFrame("Hero Battle Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);

        GamePanel gamePanel = new GamePanel();
        frame.add(gamePanel);

        // открываем в максимизированном окне с рамкой (можно закрыть)
        frame.setUndecorated(false);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameWindow::new);
    }
}
