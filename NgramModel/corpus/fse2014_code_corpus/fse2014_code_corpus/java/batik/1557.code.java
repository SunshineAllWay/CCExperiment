package org.apache.batik.svggen;
import java.awt.*;
import java.awt.geom.*;
public class ShearTest implements Painter {
    public void paint(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        Ellipse2D circle = new Ellipse2D.Float(0, 0, 50, 60);
        BasicStroke stroke = new BasicStroke(15);
        g.shear(0, 1);
        g.translate(100, 100);
        g.setStroke(stroke);
        g.setPaint(Color.gray);
        g.draw(circle);
        java.awt.geom.AffineTransform txf = g.getTransform();
        Shape ellipse = txf.createTransformedShape(circle);
        g.setTransform(new java.awt.geom.AffineTransform());
        g.translate(0, -150);
        g.draw(ellipse);
    }
}
