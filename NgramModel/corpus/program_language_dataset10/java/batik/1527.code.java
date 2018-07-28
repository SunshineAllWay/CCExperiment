package org.apache.batik.svggen;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
public class BEExample implements Painter {
    public void paint(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(new Color(103, 103, 152));
        g.fillRect(10, 10, 200, 50);
        g.setPaint(Color.white);
        g.setFont(new Font("SunSansCondensed-Heavy", Font.PLAIN, 20));
        g.drawString("Hello Java 2D to SVG", 40, 40);
    }
}
