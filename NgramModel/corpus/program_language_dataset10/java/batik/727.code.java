package org.apache.batik.ext.awt.font;
import java.awt.Shape;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import org.apache.batik.ext.awt.geom.PathLength;
public class TextPathLayout {
    public static final int ALIGN_START = 0;
    public static final int ALIGN_MIDDLE = 1;
    public static final int ALIGN_END = 2;
    public static final int ADJUST_SPACING = 0;
    public static final int ADJUST_GLYPHS = 1;
    public static Shape layoutGlyphVector(GlyphVector glyphs,
                                          Shape path, int align,
                                          float startOffset,
                                          float textLength,
                                          int lengthAdjustMode) {
        GeneralPath newPath = new GeneralPath();
        PathLength pl = new PathLength(path);
        float pathLength = pl.lengthOfPath();
        if ( glyphs == null ){
            return newPath;
        }
        float glyphsLength = (float) glyphs.getVisualBounds().getWidth();
        if (path == null ||
            glyphs.getNumGlyphs() == 0 ||
            pl.lengthOfPath() == 0f ||
            glyphsLength == 0f) {
            return newPath;
        }
        float lengthRatio = textLength / glyphsLength;
        float currentPosition = startOffset;
        if (align == ALIGN_END) {
            currentPosition += pathLength - textLength;
        } else if (align == ALIGN_MIDDLE) {
            currentPosition += (pathLength - textLength) / 2;
        }
        for (int i = 0; i < glyphs.getNumGlyphs(); i++) {
            GlyphMetrics gm = glyphs.getGlyphMetrics(i);
            float charAdvance = gm.getAdvance();
            Shape glyph = glyphs.getGlyphOutline(i);
            if (lengthAdjustMode == ADJUST_GLYPHS) {
                AffineTransform scale = AffineTransform.getScaleInstance(lengthRatio, 1.0f);
                glyph = scale.createTransformedShape(glyph);
                charAdvance *= lengthRatio;
            }
            float glyphWidth = (float) glyph.getBounds2D().getWidth();
            float charMidPos = currentPosition + glyphWidth / 2f;
            Point2D charMidPoint = pl.pointAtLength(charMidPos);
            if (charMidPoint != null) {
                float angle = pl.angleAtLength(charMidPos);
                AffineTransform glyphTrans = new AffineTransform();
                glyphTrans.translate(charMidPoint.getX(), charMidPoint.getY());
                glyphTrans.rotate(angle);
                glyphTrans.translate(charAdvance / -2f, 0f);
                glyph = glyphTrans.createTransformedShape(glyph);
                newPath.append(glyph, false);
            }
            if (lengthAdjustMode == ADJUST_SPACING) {
                currentPosition += (charAdvance * lengthRatio);
            } else {
                currentPosition += charAdvance;
            }
        }
        return newPath;
    }
    public static Shape layoutGlyphVector(GlyphVector glyphs,
                                          Shape path, int align) {
        return layoutGlyphVector(glyphs, path, align, 0f,
                                 (float) glyphs.getVisualBounds().getWidth(),
                                 ADJUST_SPACING);
    }
    public static Shape layoutGlyphVector(GlyphVector glyphs,
                                          Shape path) {
        return layoutGlyphVector(glyphs, path, ALIGN_START);
    }
} 
