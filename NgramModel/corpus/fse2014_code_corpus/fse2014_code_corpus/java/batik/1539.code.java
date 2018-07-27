package org.apache.batik.svggen;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
public class DrawImage implements Painter {
    public void paint(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        BufferedImage image = new BufferedImage(100, 75, BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig = image.createGraphics();
        ig.scale(.5, .5);
        ig.setPaint(new Color(128,0,0));
        ig.fillRect(0, 0, 100, 50);
        ig.setPaint(Color.orange);
        ig.fillRect(100, 0, 100, 50);
        ig.setPaint(Color.yellow);
        ig.fillRect(0, 50, 100, 50);
        ig.setPaint(Color.red);
        ig.fillRect(100, 50, 100, 50);
        ig.setPaint(new Color(255, 127, 127));
        ig.fillRect(0, 100, 100, 50);
        ig.setPaint(Color.black);
        ig.draw(new Rectangle2D.Double(0.5, 0.5, 199, 149));
        ig.dispose();
        g.drawImage(image, 5, 10, Color.gray, null);
        g.translate(150, 0);
        g.drawImage(image, 5, 10, 50, 40, null);
        g.translate(-150, 80);
        g.drawImage(image, 5, 10, 45, 40, 50, 0, 100, 25, null);
        g.translate(150, 0);
        g.drawImage(image, 5, 10, 45, 40,   50, 50, 100, 75, Color.gray, null);
        g.translate(-150, 80);
        AffineTransform at = new AffineTransform();
        at.scale(.5, .3);
        at.translate(5, 10);
        g.drawImage(image, at, null);
        g.translate(150, 0);
        RescaleOp op = new RescaleOp(.5f, 0f, null);
        g.drawImage(image,op,5,10);
        g.translate(-150, 0);
        g.translate(0, 80);
        g.drawImage(image, 5, 10, 50, 40, Color.gray, null);
    }
}
