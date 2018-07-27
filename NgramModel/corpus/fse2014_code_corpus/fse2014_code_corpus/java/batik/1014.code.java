package org.apache.batik.gvt.font;
import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.HashMap;
import java.util.Map;
import org.apache.batik.gvt.text.ArabicTextHandler;
public class AWTGVTFont implements GVTFont {
    protected Font   awtFont;
    protected double size;
    protected double scale;
    public AWTGVTFont(Font font) {
        this.size = font.getSize2D();
        this.awtFont = font.deriveFont(FONT_SIZE);
        this.scale = size/awtFont.getSize2D();
        initializeFontCache(awtFont);
    }
    public AWTGVTFont(Font font, double scale) {
        this.size = font.getSize2D()*scale;
        this.awtFont = font.deriveFont(FONT_SIZE);
        this.scale = size/awtFont.getSize2D();
        initializeFontCache(awtFont);
    }
    public AWTGVTFont(Map attributes) {
        Float sz = (Float)attributes.get(TextAttribute.SIZE);
        if (sz != null) {
            this.size = sz.floatValue();
            attributes.put(TextAttribute.SIZE, new Float(FONT_SIZE));
            this.awtFont = new Font(attributes);
        } else {
            this.awtFont = new Font(attributes);
            this.size = awtFont.getSize2D();
        }
        this.scale = size/awtFont.getSize2D();
        initializeFontCache(awtFont);
    }
    public AWTGVTFont(String name, int style, int size) {
        this.awtFont = new Font(name, style, (int)FONT_SIZE);
        this.size  = size;
        this.scale = size/awtFont.getSize2D();
        initializeFontCache(awtFont);
    }
    public boolean canDisplay(char c) {
        return awtFont.canDisplay(c);
    }
    public int canDisplayUpTo(char[] text, int start, int limit) {
        return awtFont.canDisplayUpTo(text, start, limit);
    }
    public int canDisplayUpTo(CharacterIterator iter, int start, int limit) {
        return awtFont.canDisplayUpTo(iter, start, limit);
    }
    public int canDisplayUpTo(String str) {
        return awtFont.canDisplayUpTo(str);
    }
    public GVTGlyphVector createGlyphVector(FontRenderContext frc,
                                            char[] chars) {
        StringCharacterIterator sci =
            new StringCharacterIterator(new String(chars));
        GlyphVector gv = awtFont.createGlyphVector(frc, chars);
        return new AWTGVTGlyphVector(gv, this, scale, sci);
    }
    public GVTGlyphVector createGlyphVector(FontRenderContext frc,
                                            CharacterIterator ci) {
        if (ci instanceof AttributedCharacterIterator) {
            AttributedCharacterIterator aci = (AttributedCharacterIterator)ci;
            if (ArabicTextHandler.containsArabic(aci)) {
                String str = ArabicTextHandler.createSubstituteString(aci);
                return createGlyphVector(frc, str);
            }
        }
        GlyphVector gv = awtFont.createGlyphVector(frc, ci);
        return new AWTGVTGlyphVector(gv, this, scale, ci);
    }
    public GVTGlyphVector createGlyphVector(FontRenderContext frc,
                                            int[] glyphCodes,
                                            CharacterIterator ci) {
        return new AWTGVTGlyphVector
            (awtFont.createGlyphVector(frc, glyphCodes),
             this, scale, ci);
    }
    public GVTGlyphVector createGlyphVector(FontRenderContext frc, String str)
    {
        StringCharacterIterator sci = new StringCharacterIterator(str);
        return new AWTGVTGlyphVector
            (awtFont.createGlyphVector(frc, str), this, scale, sci);
    }
    public GVTFont deriveFont(float size) {
        return new AWTGVTFont(awtFont, size/this.size);
    }
    public String getFamilyName() {
        return awtFont.getFamily();
    }
    public GVTLineMetrics getLineMetrics(char[] chars,
                                         int beginIndex,
                                         int limit,
                                         FontRenderContext frc) {
        return new GVTLineMetrics
            (awtFont.getLineMetrics(chars, beginIndex, limit, frc), (float)scale);
    }
    public GVTLineMetrics getLineMetrics(CharacterIterator ci,
                                         int beginIndex,
                                         int limit,
                                         FontRenderContext frc) {
        return new GVTLineMetrics
            (awtFont.getLineMetrics(ci, beginIndex, limit, frc), (float)scale);
    }
    public GVTLineMetrics getLineMetrics(String str, FontRenderContext frc) {
        return new GVTLineMetrics(awtFont.getLineMetrics(str, frc), (float)scale);
    }
    public GVTLineMetrics getLineMetrics(String str,
                                         int beginIndex,
                                         int limit,
                                         FontRenderContext frc) {
        return new GVTLineMetrics
            (awtFont.getLineMetrics(str, beginIndex, limit, frc), (float)scale);
    }
    public float getSize() {
        return (float)size;
    }
    public float getHKern(int glyphCode1, int glyphCode2) {
        return 0.0f;
    }
    public float getVKern(int glyphCode1, int glyphCode2) {
        return 0.0f;
    }
    public static final float FONT_SIZE = 48.0f;
    public static
        AWTGlyphGeometryCache.Value getGlyphGeometry(AWTGVTFont font,
                                                     char c,
                                                     GlyphVector gv,
                                                     int glyphIndex,
                                                     Point2D glyphPos) {
        AWTGlyphGeometryCache glyphCache =
            (AWTGlyphGeometryCache)fontCache.get(font.awtFont);
        AWTGlyphGeometryCache.Value v = glyphCache.get(c);
        if (v == null) {
            Shape outline = gv.getGlyphOutline(glyphIndex);
            GlyphMetrics metrics = gv.getGlyphMetrics(glyphIndex);
            Rectangle2D gmB = metrics.getBounds2D();
            if (AWTGVTGlyphVector.outlinesPositioned()) {
                AffineTransform tr = AffineTransform.getTranslateInstance
                    (-glyphPos.getX(), -glyphPos.getY());
                outline = tr.createTransformedShape(outline);
            }
            v = new AWTGlyphGeometryCache.Value(outline, gmB);
            glyphCache.put(c, v);
        }
        return v;
    }
    static Map fontCache = new HashMap(11);
    static void initializeFontCache(Font awtFont) {
        if (!fontCache.containsKey(awtFont)) {
            fontCache.put(awtFont, new AWTGlyphGeometryCache());
        }
    }
    static void putAWTGVTFont(AWTGVTFont font) {
        fontCache.put(font.awtFont, font);
    }
    static AWTGVTFont getAWTGVTFont(Font awtFont) {
        return (AWTGVTFont)fontCache.get(awtFont);
    }
}
