package org.apache.batik.svggen;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
public class TextSpacePreserve implements Painter {
    public void paint(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(Color.black); 
        int legendX = 10, legendY = 12;
        g.translate(0, 30);
        g.drawString("     space before.", legendX, legendY);
        g.drawString("Multiple spaces between A and B: A    B", legendX, legendY + 20);
        g.drawString("This is a first line\n     and this is a second line starting with spaces", 
                     legendX, legendY + 40);
        g.drawString("Should have no trailing spaces", legendX, legendY + 60);
    }
}
