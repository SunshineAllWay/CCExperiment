package org.apache.batik.svggen;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
public class AttributedCharacterIterator implements Painter {
    public void paint(Graphics2D g) {
        String fontName = "Arial";
        int fontSize = 15;
        String text = "Attributed Strings are fun !";
        AttributedString styledText = new AttributedString(text);
        Font font = new Font(fontName, Font.PLAIN, fontSize);
        styledText.addAttribute(TextAttribute.FAMILY, font.getFamily());
        styledText.addAttribute(TextAttribute.SIZE, new Float(font.getSize()));
        styledText.addAttribute(TextAttribute.FOREGROUND, Color.black);
        styledText.addAttribute(TextAttribute.WEIGHT, 
                                TextAttribute.WEIGHT_BOLD, 0, 10);
        styledText.addAttribute(TextAttribute.POSTURE, 
                                TextAttribute.POSTURE_OBLIQUE, 11, 18);
        styledText.addAttribute(TextAttribute.UNDERLINE, 
                                TextAttribute.UNDERLINE_ON, 23, 28);
        styledText.addAttribute(TextAttribute.STRIKETHROUGH, 
                                TextAttribute.STRIKETHROUGH_ON, 23, 28);
        styledText.addAttribute(TextAttribute.FOREGROUND, 
                                new Color(128, 0, 0), 0, 10);
        styledText.addAttribute(TextAttribute.FOREGROUND, 
                                new Color(70, 107, 132), 11, 18);
        styledText.addAttribute(TextAttribute.FOREGROUND, 
                                new Color(236, 214, 70), 23, 28);
        java.text.AttributedCharacterIterator iter = styledText.getIterator();
        g.drawString(iter, 10, 100);
        styledText.addAttribute(TextAttribute.BACKGROUND, 
                                new Color(70, 107, 132), 23, 28);
        iter = styledText.getIterator();
        g.drawString(iter, 10, 130);
    }
}
