package org.apache.batik.svggen;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.geom.AffineTransform;
public class Bug4945 implements Painter {
    public void paint(Graphics2D g){
        Font origFont = g.getFont(); 
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                           java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        Font font = origFont.deriveFont(AffineTransform.getScaleInstance(1.5, 3));
        g.setFont(font);
        g.drawString("Scaled Font", 20, 40);
        font = origFont.deriveFont(AffineTransform.getTranslateInstance(50, 20));
        g.setFont(font);
        g.drawString("Translated Font", 20, 80);
        g.drawLine(20, 80, 120, 80);
        font = origFont.deriveFont(AffineTransform.getShearInstance(.5, .5));
        g.setFont(font);
        g.drawString("Sheared Font", 20, 120);
        font = origFont.deriveFont(AffineTransform.getRotateInstance(Math.PI/4));
        g.setFont(font);
        g.drawString("Rotated Font", 220, 120);
    }
}
