package org.apache.batik.svggen;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
public class RHints implements Painter {
    public void paint(Graphics2D g) {
        java.awt.RenderingHints.Key antialiasKey = java.awt.RenderingHints.KEY_ANTIALIASING;
        Object antialiasOn= java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
        Object antialiasOff= java.awt.RenderingHints.VALUE_ANTIALIAS_OFF;
        java.awt.RenderingHints.Key textAntialiasKey = java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
        Object textAntialiasOn = java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
        Object textAntialiasOff = java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
        java.awt.RenderingHints.Key interpolationKey = java.awt.RenderingHints.KEY_INTERPOLATION;
        Object interpolationBicubic = java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC;
        Object interpolationNeighbor = java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
        Font defaultFont = g.getFont();
        java.awt.geom.AffineTransform defaultTransform = g.getTransform();
        Font textFont = new Font("Impact", Font.PLAIN, 25);
        g.setPaint(Color.black);
        g.setRenderingHint(antialiasKey, antialiasOn);
        g.drawString("Text antialiasing", 10, 20);
        g.setRenderingHint(antialiasKey, antialiasOff);
        g.setRenderingHint(textAntialiasKey, textAntialiasOn);
        g.setFont(textFont);
        g.drawString("HELLO antialiased", 30, 60);
        g.setRenderingHint(textAntialiasKey, textAntialiasOff);
        g.drawString("HELLO aliased", 30, 90);
        g.translate(0, 100);
        g.setRenderingHint(antialiasKey, antialiasOn);
                                g.setRenderingHint(textAntialiasKey, textAntialiasOn);
        g.setFont(defaultFont);
        g.drawString("Shape antialiasing", 10, 20);
        g.translate(30, 0);
        g.setRenderingHint(antialiasKey, antialiasOff);
        Ellipse2D ellipse = new Ellipse2D.Float(10, 30, 100, 30);
        g.fill(ellipse);
        g.translate(0, 40);
        g.setRenderingHint(antialiasKey, antialiasOn);
        g.fill(ellipse);
        g.setTransform(defaultTransform);
        g.translate(0, 200);
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D ig = image.createGraphics();
        ig.setPaint(Color.red);
        ig.fillRect(0, 0, 2, 2);
        ig.setPaint(Color.yellow);
        ig.fillRect(0, 0, 1, 1);
        ig.fillRect(1, 1, 2, 2);
        ig.dispose();
        g.setRenderingHint(interpolationKey, interpolationNeighbor);
        g.drawString("Interpolation Nearest Neighbor / Bicubic", 10, 30);
        g.drawImage(image, 10, 50, 40, 40, null);
        g.setRenderingHint(interpolationKey, interpolationBicubic);
        g.drawImage(image, 60, 50, 40, 40, null);
    }
}
