package org.apache.batik.svggen;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
public class Color2 implements Painter {
    public void paint(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        Color blue = Color.blue;
        Color green = Color.green;
        Color transparentBlue = new Color(0, 0, 255, 128);
        Color transparentGreen = new Color(0, 255, 0, 128);
        AlphaComposite srcOver = AlphaComposite.SrcOver;
        AlphaComposite srcOverTransparent = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f);
        Rectangle rect = new Rectangle(10, 40, 100, 50);
        BasicStroke thickStroke = new BasicStroke(5);
        g.setPaint(Color.black);
        g.drawString("Opaque Colors, Half Transparent AlphaComposite", 10, 30);
        g.setComposite(srcOverTransparent);
        g.setStroke(thickStroke);
        g.setPaint(blue);
        g.fill(rect);
        g.setPaint(green);
        g.draw(rect);
        g.setPaint(Color.black);
        g.fill(rect);
        g.translate(0, 90);
        g.setPaint(Color.black);
        g.setComposite(srcOver);
        g.drawString("Transparent Colors, Opaque AlphaComposite SrcOver", 10, 30);
        g.setPaint(transparentBlue);
        g.fill(rect);
        g.setPaint(transparentGreen);
        g.draw(rect);
    }
}
