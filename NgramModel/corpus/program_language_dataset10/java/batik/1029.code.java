package org.apache.batik.gvt.font;
import java.text.AttributedCharacterIterator;
import java.util.Map;
public class UnresolvedFontFamily implements GVTFontFamily {
    protected GVTFontFace fontFace;
    public UnresolvedFontFamily(GVTFontFace fontFace) {
        this.fontFace = fontFace;
    }
    public UnresolvedFontFamily(String familyName) {
        this(new GVTFontFace(familyName));
    }
    public GVTFontFace getFontFace() {
        return fontFace;
    }
    public String getFamilyName() {
        return fontFace.getFamilyName();
    }
    public GVTFont deriveFont(float size, AttributedCharacterIterator aci) {
       return null;
    }
    public GVTFont deriveFont(float size, Map attrs) { return null; }
}
