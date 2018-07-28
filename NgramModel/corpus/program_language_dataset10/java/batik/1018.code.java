package org.apache.batik.gvt.font;
import java.awt.font.FontRenderContext;
import java.text.CharacterIterator;
public interface GVTFont {
    boolean canDisplay(char c);
    int canDisplayUpTo(char[] text, int start, int limit);
    int canDisplayUpTo(CharacterIterator iter, int start, int limit);
    int canDisplayUpTo(String str);
    GVTGlyphVector createGlyphVector(FontRenderContext frc,
                                            char[] chars);
    GVTGlyphVector createGlyphVector(FontRenderContext frc,
                                            CharacterIterator ci);
    GVTGlyphVector createGlyphVector(FontRenderContext frc,
                                            int[] glyphCodes,
                                            CharacterIterator ci);
    GVTGlyphVector createGlyphVector(FontRenderContext frc,
                                            String str);
    GVTFont deriveFont(float size);
    String getFamilyName();
    GVTLineMetrics getLineMetrics(char[] chars, int beginIndex,
                                         int limit, FontRenderContext frc);
    GVTLineMetrics getLineMetrics(CharacterIterator ci, int beginIndex,
                                         int limit, FontRenderContext frc);
    GVTLineMetrics getLineMetrics(String str, FontRenderContext frc);
    GVTLineMetrics getLineMetrics(String str, int beginIndex, int limit,
                                         FontRenderContext frc);
    float getSize();
    float getVKern(int glyphCode1, int glyphCode2);
    float getHKern(int glyphCode1, int glyphCode2);
    String toString();
}
