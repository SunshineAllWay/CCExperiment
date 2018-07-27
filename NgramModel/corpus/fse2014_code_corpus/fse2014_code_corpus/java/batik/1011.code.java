package org.apache.batik.gvt.font;
import java.awt.font.FontRenderContext;
import java.text.AttributedCharacterIterator;
public interface AltGlyphHandler {
    GVTGlyphVector createGlyphVector(FontRenderContext frc, float fontSize,
                                     AttributedCharacterIterator aci);
}
