package org.apache.batik.svggen;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
public class ATransform implements Painter {
    public void paint(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(Color.black); 
        int legendX = 10, legendY = 12;
        g.translate(0, 30);
        java.awt.geom.AffineTransform defaultTransform = g.getTransform();
        Rectangle rect = new Rectangle(10, 20, 50, 30);
        g.drawString("Default transform", legendX, legendY);
        g.fill(rect);
        g.translate(0, 90);
        g.drawString("Translate applied", legendX, legendY);
        g.fill(rect);
        g.translate(0, 90);
        g.rotate(Math.PI/2, 35, 35);
        g.drawString("Rotate about center", legendX, legendY);
        g.fill(rect);
        g.setTransform(defaultTransform);
        g.translate(150, 0);
        g.drawString("Scale (sx=2, sy=1)", legendX, legendY);
        g.scale(2, 1);
        g.fill(rect);
        g.setTransform(defaultTransform);
        g.translate(150, 90);
        g.drawString("Shear", legendX, legendY);
        g.shear(.2, 1);
        g.fill(rect);
        java.awt.geom.AffineTransform txf = g.getTransform();
        g.setTransform(new java.awt.geom.AffineTransform());
        Shape shearBounds = txf.createTransformedShape(rect).getBounds();
        g.setPaint(new Color(0, 0, 0, 128));
        g.fill(shearBounds);
    }
}
