package org.apache.batik.svggen;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
public class IdentityTest implements Painter {
    public void paint(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(Color.black); 
        g.translate(10,10);        
        g.scale(2, 2);        
        g.scale(0.5, 0.5);
        g.translate(20,40);
        g.rotate(0);
        g.translate(-30,-50);
        g.fillRect(10,10, 100,80);
    }
}
