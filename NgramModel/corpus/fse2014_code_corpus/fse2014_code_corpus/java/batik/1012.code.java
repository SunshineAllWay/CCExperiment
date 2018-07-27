package org.apache.batik.gvt.font;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.util.HashMap;
import java.util.Map;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
public class AWTFontFamily implements GVTFontFamily {
    public static final 
        AttributedCharacterIterator.Attribute TEXT_COMPOUND_DELIMITER =
        GVTAttributedCharacterIterator.TextAttribute.TEXT_COMPOUND_DELIMITER;
    protected GVTFontFace fontFace;
    protected Font   font;
    public AWTFontFamily(GVTFontFace fontFace) {
        this.fontFace = fontFace;
    }
    public AWTFontFamily(String familyName) {
        this(new GVTFontFace(familyName));
    }
    public AWTFontFamily(GVTFontFace fontFace, Font font) {
        this.fontFace = fontFace;
        this.font     = font;
    }
    public String getFamilyName() {
        return fontFace.getFamilyName();
    }
    public GVTFontFace getFontFace() {
        return fontFace;
    }
    public GVTFont deriveFont(float size, AttributedCharacterIterator aci) {
        if (font != null)
            return new AWTGVTFont(font, size);
        return deriveFont(size, aci.getAttributes());
    }
    public GVTFont deriveFont(float size, Map attrs) {
        if (font != null)
            return new AWTGVTFont(font, size);
        Map fontAttributes = new HashMap(attrs);
        fontAttributes.put(TextAttribute.SIZE, new Float(size));
        fontAttributes.put(TextAttribute.FAMILY, fontFace.getFamilyName());
        fontAttributes.remove(TEXT_COMPOUND_DELIMITER);
        return new AWTGVTFont(fontAttributes);
    }
}
