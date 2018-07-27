package org.apache.batik.gvt.font;
import java.text.AttributedCharacterIterator;
import java.util.Map;
public interface GVTFontFamily {
    String getFamilyName();
    GVTFontFace getFontFace();
    GVTFont deriveFont(float size, AttributedCharacterIterator aci);
    GVTFont deriveFont(float size, Map attrs);
}
