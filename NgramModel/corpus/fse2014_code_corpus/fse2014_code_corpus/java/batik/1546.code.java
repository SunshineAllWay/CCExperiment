package org.apache.batik.svggen;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
public class GVector implements Painter {
    public void paint(Graphics2D g) {
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
         Font font = new Font("Arial", Font.BOLD, 15);
         g.setFont(font);
         Color labelColor = new Color(0x666699);
         g.setPaint(labelColor);
         String text = "This is a GlyphVector";
         java.awt.font.GlyphVector gv = font.createGlyphVector(g.getFontRenderContext(),
                                                               text);
         g.drawGlyphVector(gv, 30, 30);
    }
}
