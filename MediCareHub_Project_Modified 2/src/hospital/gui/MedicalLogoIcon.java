package hospital.gui;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

final class MedicalLogoIcon implements Icon {
    private static final int SIZE = 46;

    @Override
    public int getIconWidth() {
        return SIZE;
    }

    @Override
    public int getIconHeight() {
        return SIZE;
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(x, y);
        g2.scale(SIZE / 48.0, SIZE / 48.0);

        Path2D shield = new Path2D.Float();
        shield.moveTo(24, 3);
        shield.curveTo(18, 7, 11, 9, 5, 11);
        shield.curveTo(4, 23, 6, 33, 12, 39);
        shield.curveTo(16, 43, 20, 45, 24, 47);
        shield.curveTo(30, 44, 36, 40, 40, 34);
        shield.curveTo(44, 27, 44, 17, 43, 11);
        shield.curveTo(37, 9, 30, 7, 24, 3);
        shield.closePath();

        g2.setColor(Color.WHITE);
        g2.fill(shield);
        g2.setColor(Theme.PRIMARY_LIGHT);
        g2.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        g2.draw(shield);

        int[][] nodes = {
                {24, 11}, {33, 16}, {37, 24}, {32, 33},
                {24, 38}, {16, 33}, {11, 24}, {16, 16}
        };
        g2.setColor(Theme.ACCENT);
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        for (int[] node : nodes) {
            g2.drawLine(24, 24, node[0], node[1]);
        }
        for (int[] node : nodes) {
            g2.fillOval(node[0] - 2, node[1] - 2, 4, 4);
        }

        Path2D heart = new Path2D.Float();
        heart.moveTo(24, 31);
        heart.curveTo(22, 29, 17, 26, 17, 22);
        heart.curveTo(17, 17, 23, 16, 24, 20);
        heart.curveTo(26, 16, 32, 17, 32, 22);
        heart.curveTo(32, 26, 27, 29, 24, 31);
        heart.closePath();
        g2.setColor(Theme.ACCENT);
        g2.fill(heart);
        g2.dispose();
    }
}
