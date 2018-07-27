package org.apache.batik.svggen;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
public class Paints implements Painter {
    public void paint(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        Paint defaultPaint = Color.black;
        g.setPaint(defaultPaint);
        g.translate(0, 30);
        Rectangle rect = new Rectangle(10, 20, 100, 60);
        Color fillColor = new Color(255, 255, 0, 128);
        g.drawString("Semi transparent black", 10, 10);
        g.drawString("Behind Rectangle", 40, 60);
        g.setPaint(fillColor);
        g.fill(rect);
        g.translate(0, 90);
        GradientPaint fillGradient = new GradientPaint(10, 20, Color.red,
                                                       110, 80, Color.yellow);
        g.setPaint(defaultPaint);
        g.drawString("Red to Yellow linear gradient", 10, 10);
        g.setPaint(fillGradient);
        g.fill(rect);
        g.translate(0, 90);
        BufferedImage buf = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
        Graphics2D bg = buf.createGraphics();
        bg.setPaint(Color.red);
        bg.fillRect(0, 0, 10, 10);
        bg.setPaint(Color.yellow);
        bg.fillRect(10, 10, 10, 10);
        bg.dispose();
        TexturePaint fillTexture = new TexturePaint(buf, new Rectangle(10, 20, 20, 20));
        g.setPaint(defaultPaint);
        g.drawString("Texture Paint", 10, 10);
        g.setPaint(fillTexture);
        g.fill(rect);
    }
}
