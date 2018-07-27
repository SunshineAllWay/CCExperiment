package org.apache.batik.gvt.font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphJustificationInfo;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
public interface GVTGlyphVector {
    GVTFont getFont();
    FontRenderContext getFontRenderContext();
    int getGlyphCode(int glyphIndex);
    int[] getGlyphCodes(int beginGlyphIndex, int numEntries, int[] codeReturn);
    GlyphJustificationInfo getGlyphJustificationInfo(int glyphIndex);
    Shape getGlyphLogicalBounds(int glyphIndex);
    GVTGlyphMetrics getGlyphMetrics(int glyphIndex);
    Shape getGlyphOutline(int glyphIndex);
    Rectangle2D getGlyphCellBounds(int glyphIndex);
    Point2D getGlyphPosition(int glyphIndex);
    float[] getGlyphPositions(int beginGlyphIndex,
                              int numEntries,
                              float[] positionReturn);
    AffineTransform getGlyphTransform(int glyphIndex);
    Shape getGlyphVisualBounds(int glyphIndex);
    Rectangle2D getLogicalBounds();
    int getNumGlyphs();
    Shape getOutline();
    Shape getOutline(float x, float y);
    Rectangle2D getGeometricBounds();
    Rectangle2D getBounds2D(AttributedCharacterIterator aci);
    void performDefaultLayout();
    void setGlyphPosition(int glyphIndex, Point2D newPos);
    void setGlyphTransform(int glyphIndex, AffineTransform newTX);
    void setGlyphVisible(int glyphIndex, boolean visible);
    boolean isGlyphVisible(int glyphIndex);
    int getCharacterCount(int startGlyphIndex, int endGlyphIndex);
    void draw(Graphics2D graphics2D,
              AttributedCharacterIterator aci);
}
