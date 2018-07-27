package org.apache.batik.svggen;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
public class Bug17965 implements Painter {
    public void paint(Graphics2D g) {
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                           java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        Font font = new Font("Arial", Font.PLAIN, 30);
        g.setFont(font);
        g.setPaint(Color.blue);
        g.fillRect(0, 0, 50, 50);
        font = new Font("Helvetica", Font.PLAIN, 20);
        g.setFont(font);
        g.fillRect( 50, 50, 50, 50);
    }
}
