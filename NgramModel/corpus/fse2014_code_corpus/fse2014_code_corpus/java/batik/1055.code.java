package org.apache.batik.gvt.text;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.apache.batik.gvt.font.GVTGlyphMetrics;
import org.apache.batik.gvt.font.GVTGlyphVector;
import org.apache.batik.gvt.font.GVTLineMetrics;
public interface TextSpanLayout {
    int DECORATION_UNDERLINE = 0x1;
    int DECORATION_STRIKETHROUGH = 0x2;
    int DECORATION_OVERLINE = 0x4;
    int DECORATION_ALL = DECORATION_UNDERLINE |
                                DECORATION_OVERLINE |
                                DECORATION_STRIKETHROUGH;
    void draw(Graphics2D g2d);
    Shape getDecorationOutline(int decorationType);
    Rectangle2D getBounds2D();
    Rectangle2D getGeometricBounds();
    Shape getOutline();
    Point2D getAdvance2D();
    float [] getGlyphAdvances();
    GVTGlyphMetrics getGlyphMetrics(int glyphIndex);
    GVTLineMetrics getLineMetrics();
    Point2D getTextPathAdvance();
    Point2D getOffset();
    void setScale(float xScale, float yScale, boolean adjSpacing);
    void setOffset(Point2D offset);
    Shape getHighlightShape(int beginCharIndex, int endCharIndex);
    TextHit hitTestChar(float x, float y);
    boolean isVertical();
    boolean isOnATextPath();
    int getGlyphCount();
    int getCharacterCount(int startGlyphIndex, int endGlyphIndex);
    int getGlyphIndex(int charIndex);
    boolean isLeftToRight();
    boolean hasCharacterIndex(int index);
    GVTGlyphVector getGlyphVector();
    double getComputedOrientationAngle(int index);
    boolean isAltGlyph();
}
