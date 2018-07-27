package org.apache.batik.gvt.font;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphJustificationInfo;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import org.apache.batik.gvt.text.ArabicTextHandler;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.batik.util.Platform;
public class AWTGVTGlyphVector implements GVTGlyphVector {
    public static final AttributedCharacterIterator.Attribute PAINT_INFO
        = GVTAttributedCharacterIterator.TextAttribute.PAINT_INFO;
    private GlyphVector awtGlyphVector;
    private AWTGVTFont gvtFont;
    private CharacterIterator ci;
    private Point2D      [] defaultGlyphPositions;
    private Point2D.Float[] glyphPositions;
    private AffineTransform[] glyphTransforms;
    private Shape[] glyphOutlines;
    private Shape[] glyphVisualBounds;
    private Shape[] glyphLogicalBounds;
    private boolean[] glyphVisible;
    private GVTGlyphMetrics [] glyphMetrics;
    private GeneralPath outline;
    private Rectangle2D visualBounds;
    private Rectangle2D logicalBounds;
    private Rectangle2D bounds2D;
    private double scaleFactor;
    private float ascent;
    private float descent;
    private TextPaintInfo cacheTPI;
    public AWTGVTGlyphVector(GlyphVector glyphVector,
                             AWTGVTFont font,
                             double scaleFactor,
                             CharacterIterator ci) {
        this.awtGlyphVector = glyphVector;
        this.gvtFont = font;
        this.scaleFactor = scaleFactor;
        this.ci = ci;
        GVTLineMetrics lineMetrics = gvtFont.getLineMetrics
            ("By", awtGlyphVector.getFontRenderContext());
        ascent  = lineMetrics.getAscent();
        descent = lineMetrics.getDescent();
        outline       = null;
        visualBounds  = null;
        logicalBounds = null;
        bounds2D      = null;
        int numGlyphs = glyphVector.getNumGlyphs();
        glyphPositions     = new Point2D.Float  [numGlyphs+1];
        glyphTransforms    = new AffineTransform[numGlyphs];
        glyphOutlines      = new Shape          [numGlyphs];
        glyphVisualBounds  = new Shape          [numGlyphs];
        glyphLogicalBounds = new Shape          [numGlyphs];
        glyphVisible       = new boolean        [numGlyphs];
        glyphMetrics       = new GVTGlyphMetrics[numGlyphs];
        for (int i = 0; i < numGlyphs; i++) {
            glyphVisible[i] = true;
        }
    }
    public GVTFont getFont() {
        return gvtFont;
    }
    public FontRenderContext getFontRenderContext() {
        return awtGlyphVector.getFontRenderContext();
    }
    public int getGlyphCode(int glyphIndex) {
        return awtGlyphVector.getGlyphCode(glyphIndex);
    }
    public int[] getGlyphCodes(int beginGlyphIndex, int numEntries,
                               int[] codeReturn) {
        return awtGlyphVector.getGlyphCodes(beginGlyphIndex, numEntries,
                                            codeReturn);
    }
    public GlyphJustificationInfo getGlyphJustificationInfo(int glyphIndex) {
        return awtGlyphVector.getGlyphJustificationInfo(glyphIndex);
    }
    public Rectangle2D getBounds2D(AttributedCharacterIterator aci) {
        aci.first();
        TextPaintInfo tpi = (TextPaintInfo)aci.getAttribute(PAINT_INFO);
        if ((bounds2D != null) &&
            TextPaintInfo.equivilent(tpi, cacheTPI))
            return bounds2D;
        if (tpi == null)
            return null;
        if (!tpi.visible)
            return null;
        cacheTPI = new TextPaintInfo(tpi);
        Shape outline = null;
        if (tpi.fillPaint != null) {
            outline = getOutline();
            bounds2D = outline.getBounds2D();
        }
        Stroke stroke = tpi.strokeStroke;
        Paint  paint  = tpi.strokePaint;
        if ((stroke != null) && (paint != null)) {
            if (outline == null)
                outline = getOutline();
            Rectangle2D strokeBounds
                = stroke.createStrokedShape(outline).getBounds2D();
            if (bounds2D == null)
                bounds2D = strokeBounds;
            else
                bounds2D.add(strokeBounds);
        }
        if (bounds2D == null)
            return null;
        if ((bounds2D.getWidth()  == 0) ||
            (bounds2D.getHeight() == 0))
            bounds2D = null;
        return bounds2D;
    }
    public Rectangle2D getLogicalBounds() {
        if (logicalBounds == null) {
            computeGlyphLogicalBounds();
        }
        return logicalBounds;
    }
    public Shape getGlyphLogicalBounds(int glyphIndex) {
        if (glyphLogicalBounds[glyphIndex] == null &&
            glyphVisible[glyphIndex]) {
            computeGlyphLogicalBounds();
        }
        return glyphLogicalBounds[glyphIndex];
    }
    private void computeGlyphLogicalBounds() {
        Shape[] tempLogicalBounds = new Shape[getNumGlyphs()];
        boolean[] rotated  = new boolean[getNumGlyphs()];
        double maxWidth = -1.0;
        double maxHeight = -1.0;
        for (int i = 0; i < getNumGlyphs(); i++) {
            if (!glyphVisible[i]) {
                tempLogicalBounds[i] = null;
                continue;
            }
            AffineTransform glyphTransform = getGlyphTransform(i);
            GVTGlyphMetrics glyphMetrics   = getGlyphMetrics(i);
            float glyphX      = 0.0f;
            float glyphY      = (float)(-ascent/scaleFactor);
            float glyphWidth  = (float)(glyphMetrics.getHorizontalAdvance()/
                                 scaleFactor);
            float glyphHeight = (float)(glyphMetrics.getVerticalAdvance()/
                                 scaleFactor);
            Rectangle2D glyphBounds = new Rectangle2D.Double(glyphX,
                                                             glyphY,
                                                             glyphWidth,
                                                             glyphHeight);
            if (glyphBounds.isEmpty()) {
                if (i > 0) {
                    rotated [i] = rotated [i-1];
                } else {
                    rotated [i] = true;
                }
            } else {
                Point2D p1 = new Point2D.Double(glyphBounds.getMinX(),
                                                glyphBounds.getMinY());
                Point2D p2 = new Point2D.Double(glyphBounds.getMaxX(),
                                                glyphBounds.getMinY());
                Point2D p3 = new Point2D.Double(glyphBounds.getMinX(),
                                                glyphBounds.getMaxY());
                Point2D gpos = getGlyphPosition(i);
                AffineTransform tr = AffineTransform.getTranslateInstance
                    (gpos.getX(), gpos.getY());
                if (glyphTransform != null)
                    tr.concatenate(glyphTransform);
                tr.scale(scaleFactor, scaleFactor);
                tempLogicalBounds[i] = tr.createTransformedShape(glyphBounds);
                Point2D tp1 = new Point2D.Double();
                Point2D tp2 = new Point2D.Double();
                Point2D tp3 = new Point2D.Double();
                tr.transform(p1, tp1);
                tr.transform(p2, tp2);
                tr.transform(p3, tp3);
                double tdx12 = tp1.getX()-tp2.getX();
                double tdx13 = tp1.getX()-tp3.getX();
                double tdy12 = tp1.getY()-tp2.getY();
                double tdy13 = tp1.getY()-tp3.getY();
                if (((Math.abs(tdx12) < 0.001) && (Math.abs(tdy13) < 0.001)) ||
                    ((Math.abs(tdx13) < 0.001) && (Math.abs(tdy12) < 0.001))) {
                    rotated[i] = false;
                } else {
                    rotated [i] = true;
                }
                Rectangle2D rectBounds;
                rectBounds = tempLogicalBounds[i].getBounds2D();
                if (rectBounds.getWidth() > maxWidth)
                    maxWidth = rectBounds.getWidth();
                if (rectBounds.getHeight() > maxHeight)
                    maxHeight = rectBounds.getHeight();
            }
        }
        GeneralPath logicalBoundsPath = new GeneralPath();
        for (int i = 0; i < getNumGlyphs(); i++) {
            if (tempLogicalBounds[i] != null) {
                logicalBoundsPath.append(tempLogicalBounds[i], false);
            }
        }
        logicalBounds = logicalBoundsPath.getBounds2D();
        if (logicalBounds.getHeight() < maxHeight*1.5) {
            for (int i = 0; i < getNumGlyphs(); i++) {
                if (rotated[i]) continue;
                if (tempLogicalBounds[i] == null) continue;
                Rectangle2D glyphBounds = tempLogicalBounds[i].getBounds2D();
                double x     = glyphBounds.getMinX();
                double width = glyphBounds.getWidth();
                if ((i < getNumGlyphs()-1) &&
                    (tempLogicalBounds[i+1] != null)) {
                    Rectangle2D ngb = tempLogicalBounds[i+1].getBounds2D();
                    if (ngb.getX() > x) {
                        double nw = ngb.getX() - x;
                        if ((nw < width*1.15) && (nw > width*.85)) {
                            double delta = (nw-width)*.5;
                            width += delta;
                            ngb.setRect(ngb.getX()-delta, ngb.getY(),
                                        ngb.getWidth()+delta, ngb.getHeight());
                        }
                    }
                }
                tempLogicalBounds[i] = new Rectangle2D.Double
                    (x,     logicalBounds.getMinY(),
                     width, logicalBounds.getHeight());
            }
        } else if (logicalBounds.getWidth() < maxWidth*1.5) {
            for (int i = 0; i < getNumGlyphs(); i++) {
                if (rotated[i]) continue;
                if (tempLogicalBounds[i] == null) continue;
                Rectangle2D glyphBounds = tempLogicalBounds[i].getBounds2D();
                double      y           = glyphBounds.getMinY();
                double      height      = glyphBounds.getHeight();
                if ((i < getNumGlyphs()-1) &&
                    (tempLogicalBounds[i+1] != null)) {
                    Rectangle2D ngb = tempLogicalBounds[i+1].getBounds2D();
                    if (ngb.getY() > y) { 
                        double nh = ngb.getY() - y;
                        if ((nh < height*1.15) && (nh > height*.85)) {
                            double delta = (nh-height)*.5;
                            height += delta;
                            ngb.setRect(ngb.getX(), ngb.getY()-delta,
                                        ngb.getWidth(), ngb.getHeight()+delta);
                        }
                    }
                }
                tempLogicalBounds[i] = new Rectangle2D.Double
                    (logicalBounds.getMinX(),  y,
                     logicalBounds.getWidth(), height);
            }
        }
        System.arraycopy( tempLogicalBounds, 0, glyphLogicalBounds, 0, getNumGlyphs() );
    }
    public GVTGlyphMetrics getGlyphMetrics(int glyphIndex) {
        if (glyphMetrics[glyphIndex] != null)
            return glyphMetrics[glyphIndex];
        Point2D glyphPos = defaultGlyphPositions[glyphIndex];
        char c = ci.setIndex(ci.getBeginIndex()+glyphIndex);
        ci.setIndex(ci.getBeginIndex());
        AWTGlyphGeometryCache.Value v = AWTGVTFont.getGlyphGeometry
            (gvtFont, c, awtGlyphVector, glyphIndex, glyphPos);
        Rectangle2D gmB = v.getBounds2D();
        Rectangle2D bounds = new Rectangle2D.Double
            (gmB.getX()     * scaleFactor, gmB.getY()      * scaleFactor,
             gmB.getWidth() * scaleFactor, gmB.getHeight() * scaleFactor);
        float adv = (float)(defaultGlyphPositions[glyphIndex+1].getX()-
                            defaultGlyphPositions[glyphIndex]  .getX());
        glyphMetrics[glyphIndex] =  new GVTGlyphMetrics
            ((float)(adv*scaleFactor), (ascent+descent),
             bounds, GlyphMetrics.STANDARD);
        return glyphMetrics[glyphIndex];
    }
    public Shape getGlyphOutline(int glyphIndex) {
        if (glyphOutlines[glyphIndex] == null) {
            Point2D glyphPos = defaultGlyphPositions[glyphIndex];
            char c = ci.setIndex(ci.getBeginIndex()+glyphIndex);
            ci.setIndex(ci.getBeginIndex());
            AWTGlyphGeometryCache.Value v = AWTGVTFont.getGlyphGeometry
                (gvtFont, c, awtGlyphVector, glyphIndex, glyphPos);
            Shape glyphOutline = v.getOutline();
            AffineTransform tr = AffineTransform.getTranslateInstance
                (getGlyphPosition(glyphIndex).getX(),
                 getGlyphPosition(glyphIndex).getY());
            AffineTransform glyphTransform = getGlyphTransform(glyphIndex);
            if (glyphTransform != null) {
                tr.concatenate(glyphTransform);
            }
            tr.scale(scaleFactor, scaleFactor);
            glyphOutlines[glyphIndex]=tr.createTransformedShape(glyphOutline);
        }
        return glyphOutlines[glyphIndex];
    }
    private static final boolean outlinesPositioned;
    private static final boolean drawGlyphVectorWorks;
    private static final boolean glyphVectorTransformWorks;
    static {
        String s = System.getProperty("java.specification.version");
        if ("1.4".compareTo(s) <= 0) {
            outlinesPositioned = true;
            drawGlyphVectorWorks = true;
            glyphVectorTransformWorks = true;
        } else if (Platform.isOSX) {
            outlinesPositioned = true;
            drawGlyphVectorWorks = false;
            glyphVectorTransformWorks = false;
        } else {
            outlinesPositioned = false;
            drawGlyphVectorWorks = true;
            glyphVectorTransformWorks = false;
        }
    }
    static boolean outlinesPositioned() {
        return outlinesPositioned;
    }
    public Rectangle2D getGlyphCellBounds(int glyphIndex) {
        return getGlyphLogicalBounds(glyphIndex).getBounds2D();
    }
    public Point2D getGlyphPosition(int glyphIndex) {
        return glyphPositions[glyphIndex];
    }
    public float[] getGlyphPositions(int beginGlyphIndex,
                                     int numEntries,
                                     float[] positionReturn) {
        if (positionReturn == null) {
            positionReturn = new float[numEntries*2];
        }
        for (int i = beginGlyphIndex; i < (beginGlyphIndex+numEntries); i++) {
            Point2D glyphPos = getGlyphPosition(i);
            positionReturn[(i-beginGlyphIndex)*2] = (float)glyphPos.getX();
            positionReturn[(i-beginGlyphIndex)*2 + 1] = (float)glyphPos.getY();
        }
        return positionReturn;
    }
    public AffineTransform getGlyphTransform(int glyphIndex) {
        return glyphTransforms[glyphIndex];
    }
    public Shape getGlyphVisualBounds(int glyphIndex) {
        if (glyphVisualBounds[glyphIndex] == null) {
            Point2D glyphPos = defaultGlyphPositions[glyphIndex];
            char c = ci.setIndex(ci.getBeginIndex()+glyphIndex);
            ci.setIndex(ci.getBeginIndex());
            AWTGlyphGeometryCache.Value v = AWTGVTFont.getGlyphGeometry
                (gvtFont, c, awtGlyphVector, glyphIndex, glyphPos);
            Rectangle2D glyphBounds = v.getOutlineBounds2D();
            AffineTransform tr = AffineTransform.getTranslateInstance
                (getGlyphPosition(glyphIndex).getX(),
                 getGlyphPosition(glyphIndex).getY());
            AffineTransform glyphTransform = getGlyphTransform(glyphIndex);
            if (glyphTransform != null) {
                tr.concatenate(glyphTransform);
            }
            tr.scale(scaleFactor, scaleFactor);
            glyphVisualBounds[glyphIndex] =
                tr.createTransformedShape(glyphBounds);
        }
        return glyphVisualBounds[glyphIndex];
    }
    public int getNumGlyphs() {
        return awtGlyphVector.getNumGlyphs();
    }
    public Shape getOutline() {
        if (outline != null)
            return outline;
        outline = new GeneralPath();
        for (int i = 0; i < getNumGlyphs(); i++) {
            if (glyphVisible[i]) {
                Shape glyphOutline = getGlyphOutline(i);
                outline.append(glyphOutline, false);
            }
        }
        return outline;
    }
    public Shape getOutline(float x, float y) {
        Shape outline = getOutline();
        AffineTransform tr = AffineTransform.getTranslateInstance(x,y);
        outline = tr.createTransformedShape(outline);
        return outline;
    }
    public Rectangle2D getGeometricBounds() {
        if (visualBounds == null) {
            Shape outline = getOutline();
            visualBounds = outline.getBounds2D();
        }
        return visualBounds;
    }
    public void performDefaultLayout() {
        if (defaultGlyphPositions == null) {
            awtGlyphVector.performDefaultLayout();
            defaultGlyphPositions = new Point2D.Float[getNumGlyphs()+1];
            for (int i = 0; i <= getNumGlyphs(); i++)
                defaultGlyphPositions[i] = awtGlyphVector.getGlyphPosition(i);
        }
        outline       = null;
        visualBounds  = null;
        logicalBounds = null;
        bounds2D      = null;
        float shiftLeft = 0;
        int i=0;
        for (; i < getNumGlyphs(); i++) {
            glyphTransforms   [i] = null;
            glyphVisualBounds [i] = null;
            glyphLogicalBounds[i] = null;
            glyphOutlines     [i] = null;
            glyphMetrics      [i] = null;
            Point2D glyphPos = defaultGlyphPositions[i];
            float x = (float)((glyphPos.getX() * scaleFactor)-shiftLeft);
            float y = (float) (glyphPos.getY() * scaleFactor);
             ci.setIndex(i + ci.getBeginIndex());
                if (glyphPositions[i] == null) {
                    glyphPositions[i] = new Point2D.Float(x,y);
                } else {
                    glyphPositions[i].x = x;
                    glyphPositions[i].y = y;
                }
        }
        Point2D glyphPos = defaultGlyphPositions[i];
        glyphPositions[i] = new Point2D.Float
                ((float)((glyphPos.getX() * scaleFactor)-shiftLeft),
                 (float) (glyphPos.getY() * scaleFactor));
    }
    public void setGlyphPosition(int glyphIndex, Point2D newPos) {
        glyphPositions[glyphIndex].x = (float)newPos.getX();
        glyphPositions[glyphIndex].y = (float)newPos.getY();
        outline       = null;
        visualBounds  = null;
        logicalBounds = null;
        bounds2D      = null;
        if (glyphIndex != getNumGlyphs()) {
            glyphVisualBounds [glyphIndex] = null;
            glyphLogicalBounds[glyphIndex] = null;
            glyphOutlines     [glyphIndex] = null;
            glyphMetrics      [glyphIndex] = null;
        }
    }
    public void setGlyphTransform(int glyphIndex, AffineTransform newTX) {
        glyphTransforms[glyphIndex] = newTX;
        outline       = null;
        visualBounds  = null;
        logicalBounds = null;
        bounds2D      = null;
        glyphVisualBounds [glyphIndex] = null;
        glyphLogicalBounds[glyphIndex] = null;
        glyphOutlines     [glyphIndex] = null;
        glyphMetrics      [glyphIndex] = null;
    }
    public void setGlyphVisible(int glyphIndex, boolean visible) {
        if (visible == glyphVisible[glyphIndex])
            return;
        glyphVisible[glyphIndex] = visible;
        outline       = null;
        visualBounds  = null;
        logicalBounds = null;
        bounds2D      = null;
        glyphVisualBounds [glyphIndex] = null;
        glyphLogicalBounds[glyphIndex] = null;
        glyphOutlines     [glyphIndex] = null;
        glyphMetrics      [glyphIndex] = null;
    }
    public boolean isGlyphVisible(int glyphIndex) {
        return glyphVisible[glyphIndex];
    }
    public int getCharacterCount(int startGlyphIndex, int endGlyphIndex) {
        if (startGlyphIndex < 0) {
            startGlyphIndex = 0;
        }
        if (endGlyphIndex >= getNumGlyphs()) {
            endGlyphIndex = getNumGlyphs()-1;
        }
        int charCount = 0;
        int start = startGlyphIndex+ci.getBeginIndex();
        int end   = endGlyphIndex+ci.getBeginIndex();
        for (char c = ci.setIndex(start); ci.getIndex() <= end; c=ci.next()) {
            charCount += ArabicTextHandler.getNumChars(c);
        }
        return charCount;
    }
    public void draw(Graphics2D graphics2D,
                     AttributedCharacterIterator aci) {
        int numGlyphs = getNumGlyphs();
        aci.first();
        TextPaintInfo tpi = (TextPaintInfo)aci.getAttribute
            (GVTAttributedCharacterIterator.TextAttribute.PAINT_INFO);
        if (tpi == null) return;
        if (!tpi.visible) return;
        Paint  fillPaint   = tpi.fillPaint;
        Stroke stroke      = tpi.strokeStroke;
        Paint  strokePaint = tpi.strokePaint;
        if ((fillPaint == null) && ((strokePaint == null) ||
                                    (stroke == null)))
            return;
        boolean useHinting = drawGlyphVectorWorks;
        if (useHinting && (stroke != null) && (strokePaint != null))
            useHinting = false;
        if (useHinting &&
            (fillPaint != null) && !(fillPaint instanceof Color))
            useHinting = false;
        if (useHinting) {
            Object v1 = graphics2D.getRenderingHint
                (RenderingHints.KEY_TEXT_ANTIALIASING);
            Object v2 = graphics2D.getRenderingHint
                (RenderingHints.KEY_STROKE_CONTROL);
            if ((v1 == RenderingHints.VALUE_TEXT_ANTIALIAS_ON) &&
                (v2 == RenderingHints.VALUE_STROKE_PURE))
                useHinting = false;
        }
        final int typeGRot   = AffineTransform.TYPE_GENERAL_ROTATION;
        final int typeGTrans = AffineTransform.TYPE_GENERAL_TRANSFORM;
        if (useHinting) {
            AffineTransform at = graphics2D.getTransform();
            int type = at.getType();
            if (((type & typeGTrans) != 0) || ((type & typeGRot)  != 0))
                useHinting = false;
        }
        if (useHinting) {
            for (int i=0; i<numGlyphs; i++) {
                if (!glyphVisible[i]) {
                    useHinting = false;
                    break;
                }
                AffineTransform at = glyphTransforms[i];
                if (at != null) {
                    int type = at.getType();
                    if ((type & ~AffineTransform.TYPE_TRANSLATION) == 0) {
                    } else if (glyphVectorTransformWorks &&
                               ((type & typeGTrans) == 0) &&
                               ((type & typeGRot)   == 0)) {
                    } else {
                        useHinting = false;
                        break;
                    }
                }
            }
        }
        if (useHinting) {
            double sf = scaleFactor;
            double [] mat = new double[6];
            for (int i=0; i< numGlyphs; i++) {
                Point2D         pos = glyphPositions[i];
                double x = pos.getX();
                double y = pos.getY();
                AffineTransform at = glyphTransforms[i];
                if (at != null) {
                    at.getMatrix(mat);
                    x += mat[4];
                    y += mat[5];
                    if ((mat[0] != 1) || (mat[1] != 0) ||
                        (mat[2] != 0) || (mat[3] != 1)) {
                        mat[4] = 0; mat[5] = 0;
                        at = new AffineTransform(mat);
                    } else {
                        at = null;
                    }
                }
                pos = new Point2D.Double(x/sf, y/sf);
                awtGlyphVector.setGlyphPosition(i, pos);
                awtGlyphVector.setGlyphTransform(i, at);
            }
            graphics2D.scale(sf, sf);
            graphics2D.setPaint(fillPaint);
            graphics2D.drawGlyphVector(awtGlyphVector, 0.0f, 0.0f);
            graphics2D.scale(1.0/sf, 1.0/sf);
            for (int i=0; i< numGlyphs; i++) {
                Point2D         pos = defaultGlyphPositions[i];
                awtGlyphVector.setGlyphPosition(i, pos);
                awtGlyphVector.setGlyphTransform(i, null);
            }
        } else {
            Shape outline = getOutline();
            if (fillPaint != null) {
                graphics2D.setPaint(fillPaint);
                graphics2D.fill(outline);
            }
            if (stroke != null && strokePaint != null) {
                graphics2D.setStroke(stroke);
                graphics2D.setPaint(strokePaint);
                graphics2D.draw(outline);
            }
        }
    }
}
