package org.apache.batik.svggen;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
public class Font1 implements Painter {
    public void paint(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
        Color labelColor = new Color(0x666699);
        Color fontColor = Color.black;
        java.awt.geom.AffineTransform defaultTransform = g.getTransform();
        java.awt.Font defaultFont = new java.awt.Font("Arial", java.awt.Font.BOLD, 16);
        g.setFont(defaultFont);
        FontRenderContext frc = g.getFontRenderContext();
        g.setPaint(labelColor);
        g.drawString("Font size", 10, 30);
        g.setPaint(fontColor);
        g.translate(0, 20);
        int[] fontSizes = { 6, 8, 10, 12, 18, 36, 48 };
        for(int i=0; i<fontSizes.length; i++){
            java.awt.Font font = new java.awt.Font(defaultFont.getFamily(),
                                 java.awt.Font.PLAIN,
                                 fontSizes[i]);
            g.setFont(font);
            g.drawString("aA", 10, 40);
            double width = font.createGlyphVector(frc, "aA").getVisualBounds().getWidth();
            g.translate(width*1.2, 0);
        }
        g.setTransform(defaultTransform);
        g.translate(0, 60);
        int[] fontStyles = { java.awt.Font.PLAIN,
                             java.awt.Font.BOLD,
                             java.awt.Font.ITALIC,
                             java.awt.Font.BOLD | java.awt.Font.ITALIC };
        String[] fontStyleStrings = { "Plain", "Bold", "Italic", "Bold Italic" };
        g.setFont(defaultFont);
        g.setPaint(labelColor);
        g.drawString("Font Styles", 10, 30);
        g.translate(0, 20);
        g.setPaint(fontColor);
        for(int i=0; i<fontStyles.length; i++){
            java.awt.Font font = new java.awt.Font(defaultFont.getFamily(),
                                 fontStyles[i], 20);
            g.setFont(font);
            g.drawString(fontStyleStrings[i], 10, 40);
            double width = font.createGlyphVector(frc, fontStyleStrings[i]).getVisualBounds().getWidth();
            g.translate(width*1.2, 0);
        }
        g.setTransform(defaultTransform);
        g.translate(0, 120);
        String[] fontFamilies = { "Arial",
                                  "Times New Roman",
                                  "Courier New",
                                  "Verdana" };
        g.setFont(defaultFont);
        g.setPaint(labelColor);
        g.drawString("Font Families", 10, 30);
        g.setPaint(fontColor);
        for(int i=0; i<fontFamilies.length; i++){
            java.awt.Font font = new java.awt.Font(fontFamilies[i], java.awt.Font.PLAIN, 18);
            g.setFont(font);
            double height = font.createGlyphVector(frc, fontFamilies[i]).getVisualBounds().getHeight();
            g.translate(0, height*1.4);
            g.drawString(fontFamilies[i], 10, 40);
        }
        Font[] logicalFonts = { new java.awt.Font("dialog", java.awt.Font.PLAIN, 14),
                                new java.awt.Font("dialoginput", java.awt.Font.BOLD, 14),
                                new java.awt.Font("monospaced", java.awt.Font.ITALIC, 14),
                                new java.awt.Font("serif", java.awt.Font.PLAIN, 14),
                                new java.awt.Font("sansserif", java.awt.Font.BOLD, 14)};
        g.translate(0, 70);
        g.setFont(defaultFont);
        g.setPaint(labelColor);
        g.drawString("Logical Fonts", 10, 0);
        g.setPaint(fontColor);
        for(int i=0; i<logicalFonts.length; i++){
            Font font = logicalFonts[i];
            g.setFont(font);
            double height = font.createGlyphVector(frc, font.getName()).getVisualBounds().getHeight();
            g.translate(0, height*1.4);
            g.drawString(font.getName(), 10, 0);
        }
    }
}
