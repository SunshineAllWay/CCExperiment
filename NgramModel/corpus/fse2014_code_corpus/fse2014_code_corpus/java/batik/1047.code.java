package org.apache.batik.gvt.text;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.util.HashSet;
import java.util.Set;
import org.apache.batik.gvt.font.AWTGVTFont;
import org.apache.batik.gvt.font.AltGlyphHandler;
import org.apache.batik.gvt.font.GVTFont;
import org.apache.batik.gvt.font.GVTGlyphMetrics;
import org.apache.batik.gvt.font.GVTGlyphVector;
import org.apache.batik.gvt.font.GVTLineMetrics;
public class GlyphLayout implements TextSpanLayout {
    private GVTGlyphVector gv;
    private GVTFont font;
    private GVTLineMetrics metrics;
    private AttributedCharacterIterator aci;
    private Point2D advance;
    private Point2D offset;
    private float   xScale=1;
    private float   yScale=1;
    private TextPath textPath;
    private Point2D textPathAdvance;
    private int []  charMap;
    private boolean vertical, adjSpacing=true;
    private float [] glyphAdvances;
    private boolean isAltGlyph; 
    private boolean layoutApplied = false;
    private boolean spacingApplied = false;
    private boolean pathApplied    = false;
    public static final AttributedCharacterIterator.Attribute FLOW_LINE_BREAK
        = GVTAttributedCharacterIterator.TextAttribute.FLOW_LINE_BREAK;
    public static final AttributedCharacterIterator.Attribute FLOW_PARAGRAPH
        = GVTAttributedCharacterIterator.TextAttribute.FLOW_PARAGRAPH;
    public static final AttributedCharacterIterator.Attribute
        FLOW_EMPTY_PARAGRAPH
        = GVTAttributedCharacterIterator.TextAttribute.FLOW_EMPTY_PARAGRAPH;
    public static final AttributedCharacterIterator.Attribute LINE_HEIGHT
        = GVTAttributedCharacterIterator.TextAttribute.LINE_HEIGHT;
    public static final AttributedCharacterIterator.Attribute
        VERTICAL_ORIENTATION
        = GVTAttributedCharacterIterator.TextAttribute.VERTICAL_ORIENTATION;
    public static final
        AttributedCharacterIterator.Attribute VERTICAL_ORIENTATION_ANGLE =
       GVTAttributedCharacterIterator.TextAttribute.VERTICAL_ORIENTATION_ANGLE;
    public static final
        AttributedCharacterIterator.Attribute HORIZONTAL_ORIENTATION_ANGLE =
     GVTAttributedCharacterIterator.TextAttribute.HORIZONTAL_ORIENTATION_ANGLE;
    private static final AttributedCharacterIterator.Attribute X
        = GVTAttributedCharacterIterator.TextAttribute.X;
    private static final AttributedCharacterIterator.Attribute Y
        = GVTAttributedCharacterIterator.TextAttribute.Y;
    private static final AttributedCharacterIterator.Attribute DX
        = GVTAttributedCharacterIterator.TextAttribute.DX;
    private static final AttributedCharacterIterator.Attribute DY
        = GVTAttributedCharacterIterator.TextAttribute.DY;
    private static final AttributedCharacterIterator.Attribute ROTATION
        = GVTAttributedCharacterIterator.TextAttribute.ROTATION;
    private static final AttributedCharacterIterator.Attribute BASELINE_SHIFT
        = GVTAttributedCharacterIterator.TextAttribute.BASELINE_SHIFT;
    private static final AttributedCharacterIterator.Attribute WRITING_MODE
        = GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE;
    private static final Integer WRITING_MODE_TTB
        = GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE_TTB;
    private static final Integer ORIENTATION_AUTO
        = GVTAttributedCharacterIterator.TextAttribute.ORIENTATION_AUTO;
    public static final AttributedCharacterIterator.Attribute GVT_FONT
        = GVTAttributedCharacterIterator.TextAttribute.GVT_FONT;
    protected static Set runAtts = new HashSet();
    static {
        runAtts.add(X);
        runAtts.add(Y);
        runAtts.add(DX);
        runAtts.add(DY);
        runAtts.add(ROTATION);
        runAtts.add(BASELINE_SHIFT);
    }
    protected static Set szAtts = new HashSet();
    static {
        szAtts.add(TextAttribute.SIZE);
        szAtts.add(GVT_FONT);
        szAtts.add(LINE_HEIGHT);
    }
    public GlyphLayout(AttributedCharacterIterator aci,
                       int [] charMap,
                       Point2D offset,
                       FontRenderContext frc) {
        this.aci = aci;
        this.offset = offset;
        this.font = getFont();
        this.charMap = charMap;
        this.metrics = font.getLineMetrics
            (aci, aci.getBeginIndex(), aci.getEndIndex(), frc);
        this.gv = null;
        this.aci.first();
        this.vertical = (aci.getAttribute(WRITING_MODE) == WRITING_MODE_TTB);
        this.textPath =  (TextPath) aci.getAttribute
            (GVTAttributedCharacterIterator.TextAttribute.TEXTPATH);
        AltGlyphHandler altGlyphHandler
            = (AltGlyphHandler)this.aci.getAttribute
            (GVTAttributedCharacterIterator.TextAttribute.ALT_GLYPH_HANDLER);
        if (altGlyphHandler != null) {
            this.gv = altGlyphHandler.createGlyphVector
                (frc, this.font.getSize(), this.aci);
            if ( this.gv != null ){
                this.isAltGlyph = true;
            }
        }
        if (this.gv == null) {
            this.gv = font.createGlyphVector(frc, this.aci);
        }
    }
    public GVTGlyphVector getGlyphVector() {
        return this.gv;
    }
    public Point2D getOffset() {
        return offset;
    }
    public void setScale(float xScale, float yScale, boolean adjSpacing) {
        if (vertical) xScale = 1;
        else          yScale = 1;
        if ((xScale != this.xScale) ||
            (yScale != this.yScale) ||
            (adjSpacing != this.adjSpacing)) {
            this.xScale = xScale;
            this.yScale = yScale;
            this.adjSpacing = adjSpacing;
            spacingApplied = false;
            glyphAdvances  = null;
            pathApplied    = false;
        }
    }
    public void setOffset(Point2D offset) {
        if ((offset.getX() != this.offset.getX()) ||
            (offset.getY() != this.offset.getY())) {
            if ((layoutApplied)||(spacingApplied)) {
                float dx = (float)(offset.getX()-this.offset.getX());
                float dy = (float)(offset.getY()-this.offset.getY());
                int numGlyphs = gv.getNumGlyphs();
                float [] gp = gv.getGlyphPositions(0, numGlyphs+1, null);
                Point2D.Float pos = new Point2D.Float();
                for (int i=0; i<=numGlyphs; i++) {
                    pos.x = gp[2*i  ]+dx;
                    pos.y = gp[2*i+1]+dy;
                    gv.setGlyphPosition(i, pos);
                }
            }
            this.offset = offset;
            pathApplied = false;
        }
    }
    public GVTGlyphMetrics getGlyphMetrics(int glyphIndex) {
        return gv.getGlyphMetrics(glyphIndex);
    }
    public GVTLineMetrics getLineMetrics() {
        return metrics;
    }
    public boolean isVertical() {
        return vertical;
    }
    public boolean isOnATextPath() {
        return (textPath != null);
    }
    public int getGlyphCount() {
        return gv.getNumGlyphs();
    }
    public int getCharacterCount(int startGlyphIndex, int endGlyphIndex) {
        return gv.getCharacterCount(startGlyphIndex, endGlyphIndex);
    }
    public boolean isLeftToRight() {
        aci.first();
        int bidiLevel =
            ((Integer)aci.getAttribute
             (GVTAttributedCharacterIterator.TextAttribute.BIDI_LEVEL))
            .intValue();
        return ((bidiLevel&0x01) == 0);
    }
    private final void syncLayout() {
        if (!pathApplied) {
            doPathLayout();
        }
    }
    public void draw(Graphics2D g2d) {
        syncLayout();
        gv.draw(g2d, aci);
    }
    public Point2D getAdvance2D() {
        adjustTextSpacing();
        return advance;
    }
    public Shape getOutline() {
        syncLayout();
        return gv.getOutline();
    }
    public float [] getGlyphAdvances() {
        if (glyphAdvances != null)
            return glyphAdvances;
        if (!spacingApplied)
            adjustTextSpacing();
        int numGlyphs = gv.getNumGlyphs();
        float [] glyphPos = gv.getGlyphPositions(0, numGlyphs+1, null);
        glyphAdvances = new float[numGlyphs+1];
        int off = 0;
        if (isVertical())
            off = 1;
        float start = glyphPos[off];
        for (int i=0; i<numGlyphs+1; i++) {
            glyphAdvances[i] = glyphPos[2*i+off]-start;
        }
        return glyphAdvances;
    }
    public Shape getDecorationOutline(int decorationType) {
        syncLayout();
        Shape g = new GeneralPath();
        if ((decorationType & DECORATION_UNDERLINE) != 0) {
             ((GeneralPath) g).append(getUnderlineShape(), false);
        }
        if ((decorationType & DECORATION_STRIKETHROUGH) != 0) {
             ((GeneralPath) g).append(getStrikethroughShape(), false);
        }
        if ((decorationType & DECORATION_OVERLINE) != 0) {
             ((GeneralPath) g).append(getOverlineShape(), false);
        }
        return g;
    }
    public Rectangle2D getBounds2D() {
        syncLayout();
        return gv.getBounds2D(aci);
    }
    public Rectangle2D getGeometricBounds() {
        syncLayout();
        Rectangle2D gvB, decB;
        gvB = gv.getGeometricBounds();
        decB = getDecorationOutline(DECORATION_ALL).getBounds2D();
        return gvB.createUnion(decB);
    }
    public Point2D getTextPathAdvance() {
        syncLayout();
        if (textPath != null) {
            return textPathAdvance;
        } else {
            return getAdvance2D();
        }
    }
    public int getGlyphIndex(int charIndex) {
        int numGlyphs = getGlyphCount();
        int j=0;
        for (int i = 0; i < numGlyphs; i++) {
            int count = getCharacterCount(i, i);
            for (int n=0; n<count; n++) {
                int glyphCharIndex = charMap[j++];
                if (charIndex == glyphCharIndex)
                    return i;
                if (j >= charMap.length)
                    return -1;
            }
        }
        return -1;
    }
    public int getLastGlyphIndex(int charIndex) {
        int numGlyphs = getGlyphCount();
        int j=charMap.length-1;
        for (int i = numGlyphs-1; i >= 0; --i) {
            int count = getCharacterCount(i, i);
            for (int n=0; n<count; n++) {
                int glyphCharIndex = charMap[j--];
                if (charIndex == glyphCharIndex) return i;
                if (j < 0)                       return -1;
            }
        }
        return -1;
    }
    public double getComputedOrientationAngle(int index){
        if ( isGlyphOrientationAuto() ){
            if (isVertical()) {
                char ch = aci.setIndex(index);
                if (isLatinChar(ch))
                    return 90.0;
                else
                    return 0.0;
            }
            return 0.0;
        }
        else{
            return getGlyphOrientationAngle();
        }
    }
    public Shape getHighlightShape(int beginCharIndex, int endCharIndex) {
        syncLayout();
        if (beginCharIndex > endCharIndex) {
            int temp = beginCharIndex;
            beginCharIndex = endCharIndex;
            endCharIndex = temp;
        }
        GeneralPath shape = null;
        int numGlyphs = getGlyphCount();
        Point2D.Float [] topPts = new Point2D.Float[2*numGlyphs];
        Point2D.Float [] botPts = new Point2D.Float[2*numGlyphs];
        int ptIdx = 0;
        int currentChar = 0;
        for (int i = 0; i < numGlyphs; i++) {
            int glyphCharIndex = charMap[currentChar];
            if ((glyphCharIndex >= beginCharIndex) &&
                (glyphCharIndex <= endCharIndex) &&
                gv.isGlyphVisible(i)) {
                Shape gbounds = gv.getGlyphLogicalBounds(i);
                if (gbounds != null) {
                    if (shape == null)
                        shape = new GeneralPath();
                    float [] pts = new float[6];
                    int count = 0;
                    int type = -1;
                    PathIterator pi = gbounds.getPathIterator(null);
                    Point2D.Float firstPt = null;
                    while (!pi.isDone()) {
                        type = pi.currentSegment(pts);
                        if ((type == PathIterator.SEG_MOVETO) ||
                            (type == PathIterator.SEG_LINETO)) {
                            if (count > 4) break; 
                            if (count == 4) {
                                if ((firstPt == null)     ||
                                    (firstPt.x != pts[0]) ||
                                    (firstPt.y != pts[1]))
                                    break;
                            } else {
                                Point2D.Float pt;
                                pt = new Point2D.Float(pts[0], pts[1]);
                                if (count == 0) firstPt = pt;
                                switch (count) {
                                case 0: botPts[ptIdx]   = pt; break;
                                case 1: topPts[ptIdx]   = pt; break;
                                case 2: topPts[ptIdx+1] = pt; break;
                                case 3: botPts[ptIdx+1] = pt; break;
                                }
                            }
                        } else if (type == PathIterator.SEG_CLOSE) {
                            if ((count < 4) || (count > 5)) break;
                        } else {
                            break;
                        }
                        count++;
                        pi.next();
                    }
                    if (pi.isDone()) {
                        if ((botPts[ptIdx]!=null) &&
                            ((topPts[ptIdx].x != topPts[ptIdx+1].x) ||
                             (topPts[ptIdx].y != topPts[ptIdx+1].y)))
                            ptIdx += 2;
                    } else {
                        addPtsToPath(shape, topPts, botPts, ptIdx);
                        ptIdx = 0;
                        shape.append(gbounds, false);
                    }
                }
            }
            currentChar += getCharacterCount(i, i);
            if (currentChar >= charMap.length)
                currentChar = charMap.length-1;
        }
        addPtsToPath(shape, topPts, botPts, ptIdx);
        return shape;
    }
    public static final double eps = 0.00001;
    public static boolean epsEQ(double a, double b) {
        return ((a+eps > b) && (a-eps < b));
    }
    public static int makeConvexHull(Point2D.Float [] pts, int numPts) {
        Point2D.Float tmp;
        for (int i=1; i<numPts; i++) {
            if ((pts[i].x < pts[i-1].x) ||
                ((pts[i].x == pts[i-1].x) && (pts[i].y < pts[i-1].y))) {
                tmp = pts[i];
                pts[i] = pts[i-1];
                pts[i-1] = tmp;
                i=0;
                continue;
            }
        }
        Point2D.Float pt0 = pts[0];
        Point2D.Float pt1 = pts[numPts-1];
        Point2D.Float dxdy = new Point2D.Float(pt1.x-pt0.x, pt1.y-pt0.y);
        float soln, c = dxdy.y*pt0.x-dxdy.x*pt0.y;
        Point2D.Float [] topList = new Point2D.Float[numPts];
        Point2D.Float [] botList = new Point2D.Float[numPts];
        botList[0] = topList[0] = pts[0];
        int nTopPts=1;
        int nBotPts=1;
        for (int i=1; i<numPts-1; i++) {
            Point2D.Float pt = pts[i];
            soln = dxdy.x*pt.y-dxdy.y*pt.x+c;
            if (soln < 0) {
                while (nBotPts >= 2) {
                    pt0 = botList[nBotPts-2];
                    pt1 = botList[nBotPts-1];
                    float dx = pt1.x-pt0.x;
                    float dy = pt1.y-pt0.y;
                    float c0 = dy*pt0.x-dx*pt0.y;
                    soln = dx*pt.y-dy*pt.x+c0;
                    if (soln > eps) 
                        break;
                    if (soln > -eps) {
                        if (pt1.y < pt.y) pt = pt1;
                        nBotPts--;
                        break;
                    }
                    nBotPts--;
                }
                botList[nBotPts++] = pt;
            } else {
                while (nTopPts >= 2) {
                    pt0 = topList[nTopPts-2];
                    pt1 = topList[nTopPts-1];
                    float dx = pt1.x-pt0.x;
                    float dy = pt1.y-pt0.y;
                    float c0 = dy*pt0.x-dx*pt0.y;
                    soln = dx*pt.y-dy*pt.x+c0;
                    if (soln < -eps) 
                        break;
                    if (soln < eps) {
                        if (pt1.y > pt.y) pt = pt1;
                        nTopPts--;
                        break;
                    }
                    nTopPts--;
                }
                topList[nTopPts++] = pt;
            }
        }
        Point2D.Float pt = pts[numPts-1];
        while (nBotPts >= 2) {
            pt0 = botList[nBotPts-2];
            pt1 = botList[nBotPts-1];
            float dx = pt1.x-pt0.x;
            float dy = pt1.y-pt0.y;
            float c0 = dy*pt0.x-dx*pt0.y;
            soln = dx*pt.y-dy*pt.x+c0;
            if (soln > eps)
                break;
            if (soln > -eps) {
                if (pt1.y >= pt.y) nBotPts--;
                break;
            }
            nBotPts--;
        }
        while (nTopPts >= 2) {
            pt0 = topList[nTopPts-2];
            pt1 = topList[nTopPts-1];
            float dx = pt1.x-pt0.x;
            float dy = pt1.y-pt0.y;
            float c0 = dy*pt0.x-dx*pt0.y;
            soln = dx*pt.y-dy*pt.x+c0;
            if (soln < -eps)
                break;
            if (soln < eps) {
                if (pt1.y <= pt.y) nTopPts--;
                break;
            }
            nTopPts--;
        }
        System.arraycopy( topList, 0, pts, 0, nTopPts );
        int i= nTopPts;
        pts[i++] = pts[numPts-1];
        for (int n=nBotPts-1; n>0; n--, i++)
            pts[i] = botList[n];
        return i;
    }
    public static void addPtsToPath(GeneralPath shape,
                                     Point2D.Float [] topPts,
                                     Point2D.Float [] botPts,
                                     int numPts) {
        if (numPts < 2) return;
        if (numPts == 2) {
            shape.moveTo(topPts[0].x, topPts[0].y);
            shape.lineTo(topPts[1].x, topPts[1].y);
            shape.lineTo(botPts[1].x, botPts[1].y);
            shape.lineTo(botPts[0].x, botPts[0].y);
            shape.lineTo(topPts[0].x, topPts[0].y);
            return;
        }
        Point2D.Float [] boxes = new Point2D.Float[8];
        Point2D.Float [] chull = new Point2D.Float[8];
        boxes[4] = topPts[0];
        boxes[5] = topPts[1];
        boxes[6] = botPts[1];
        boxes[7] = botPts[0];
        Area []areas = new Area[numPts/2];
        int nAreas =0;
        for (int i=2; i<numPts; i+=2) {
            boxes[0] = boxes[4];
            boxes[1] = boxes[5];
            boxes[2] = boxes[6];
            boxes[3] = boxes[7];
            boxes[4] = topPts[i];
            boxes[5] = topPts[i+1];
            boxes[6] = botPts[i+1];
            boxes[7] = botPts[i];
            float delta,sz,dist;
            delta  = boxes[2].x-boxes[0].x;
            dist   = delta*delta;
            delta  = boxes[2].y-boxes[0].y;
            dist  += delta*delta;
            sz     = (float)Math.sqrt(dist);
            delta  = boxes[6].x-boxes[4].x;
            dist   = delta*delta;
            delta  = boxes[6].y-boxes[4].y;
            dist  += delta*delta;
            sz    += (float)Math.sqrt(dist);
            delta = ((boxes[0].x+boxes[1].x+boxes[2].x+boxes[3].x)-
                     (boxes[4].x+boxes[5].x+boxes[6].x+boxes[7].x))/4;
            dist = delta*delta;
            delta = ((boxes[0].y+boxes[1].y+boxes[2].y+boxes[3].y)-
                     (boxes[4].y+boxes[5].y+boxes[6].y+boxes[7].y))/4;
            dist += delta*delta;
            dist  = (float)Math.sqrt(dist);
            GeneralPath gp = new GeneralPath();
            if (dist < sz) {
                System.arraycopy(boxes, 0, chull, 0, 8);
                int npts = makeConvexHull(chull, 8);
                gp.moveTo(chull[0].x, chull[0].y);
                for(int n=1; n<npts; n++)
                    gp.lineTo(chull[n].x, chull[n].y);
                gp.closePath();
            } else {
                mergeAreas(shape, areas, nAreas);
                nAreas = 0; 
                if (i==2) {
                    gp.moveTo(boxes[0].x, boxes[0].y);
                    gp.lineTo(boxes[1].x, boxes[1].y);
                    gp.lineTo(boxes[2].x, boxes[2].y);
                    gp.lineTo(boxes[3].x, boxes[3].y);
                    gp.closePath();
                    shape.append(gp, false);
                    gp.reset();
                }
                gp.moveTo(boxes[4].x, boxes[4].y);
                gp.lineTo(boxes[5].x, boxes[5].y);
                gp.lineTo(boxes[6].x, boxes[6].y);
                gp.lineTo(boxes[7].x, boxes[7].y);
                gp.closePath();
            }
            areas[nAreas++] = new Area(gp);
        }
        mergeAreas(shape, areas, nAreas);
    }
    public static void mergeAreas(GeneralPath shape,
                                  Area []shapes, int nShapes) {
        while (nShapes > 1) {
            int n=0;
            for (int i=1; i<nShapes;i+=2) {
                shapes[i-1].add(shapes[i]);
                shapes[n++] = shapes[i-1];
                shapes[i] = null;
            }
            if ((nShapes&0x1) == 1)
                shapes[n-1].add(shapes[nShapes-1]);
            nShapes = nShapes/2;
        }
        if (nShapes == 1)
            shape.append(shapes[0], false);
    }
    public TextHit hitTestChar(float x, float y) {
        syncLayout();
        TextHit textHit = null;
        int currentChar = 0;
        for (int i = 0; i < gv.getNumGlyphs(); i++) {
            Shape gbounds = gv.getGlyphLogicalBounds(i);
            if (gbounds != null) {
                Rectangle2D gbounds2d = gbounds.getBounds2D();
                if (gbounds.contains(x, y)) {
                    boolean isRightHalf =
                        (x > (gbounds2d.getX()+(gbounds2d.getWidth()/2d)));
                    boolean isLeadingEdge = !isRightHalf;
                    int charIndex = charMap[currentChar];
                    textHit = new TextHit(charIndex, isLeadingEdge);
                    return textHit;
                }
            }
            currentChar += getCharacterCount(i, i);
            if (currentChar >= charMap.length)
                currentChar = charMap.length-1;
        }
        return textHit;
    }
    protected GVTFont getFont() {
        aci.first();
        GVTFont gvtFont = (GVTFont)aci.getAttribute(GVT_FONT);
        if (gvtFont != null)
            return gvtFont;
        return new AWTGVTFont(aci.getAttributes());
    }
    protected Shape getOverlineShape() {
        double y = metrics.getOverlineOffset();
        float overlineThickness = metrics.getOverlineThickness();
        y += overlineThickness;
        aci.first();
        Float dy = (Float) aci.getAttribute(DY);
        if (dy != null)
            y += dy.floatValue();
        Stroke overlineStroke =
            new BasicStroke(overlineThickness);
        Rectangle2D logicalBounds = gv.getLogicalBounds();
        return overlineStroke.createStrokedShape(
                           new Line2D.Double(
                           logicalBounds.getMinX() + overlineThickness/2.0, offset.getY()+y,
                           logicalBounds.getMaxX() - overlineThickness/2.0, offset.getY()+y));
    }
    protected Shape getUnderlineShape() {
        double y = metrics.getUnderlineOffset();
        float underlineThickness = metrics.getUnderlineThickness();
        y += underlineThickness*1.5;
        BasicStroke underlineStroke =
            new BasicStroke(underlineThickness);
        aci.first();
        Float dy = (Float) aci.getAttribute(DY);
        if (dy != null)
            y += dy.floatValue();
        Rectangle2D logicalBounds = gv.getLogicalBounds();
        return underlineStroke.createStrokedShape(
                           new Line2D.Double(
                           logicalBounds.getMinX() + underlineThickness/2.0, offset.getY()+y,
                           logicalBounds.getMaxX() - underlineThickness/2.0, offset.getY()+y));
    }
    protected Shape getStrikethroughShape() {
        double y = metrics.getStrikethroughOffset();
        float strikethroughThickness = metrics.getStrikethroughThickness();
        Stroke strikethroughStroke =
            new BasicStroke(strikethroughThickness);
        aci.first();
        Float dy = (Float) aci.getAttribute(DY);
        if (dy != null)
            y += dy.floatValue();
        Rectangle2D logicalBounds = gv.getLogicalBounds();
        return strikethroughStroke.createStrokedShape(
                           new Line2D.Double(
                           logicalBounds.getMinX() + strikethroughThickness/2.0, offset.getY()+y,
                           logicalBounds.getMaxX() - strikethroughThickness/2.0, offset.getY()+y));
    }
    protected void doExplicitGlyphLayout() {
        this.gv.performDefaultLayout();
        float baselineAscent
            = vertical ?
            (float) gv.getLogicalBounds().getWidth() :
            (metrics.getAscent() + Math.abs(metrics.getDescent()));
        int numGlyphs = gv.getNumGlyphs();
        float[] gp = gv.getGlyphPositions(0, numGlyphs+1, null);
        float verticalFirstOffset = 0f;
        float horizontalFirstOffset = 0f;
        boolean glyphOrientationAuto = isGlyphOrientationAuto();
        int glyphOrientationAngle = 0;
        if (!glyphOrientationAuto) {
            glyphOrientationAngle = getGlyphOrientationAngle();
        }
        int i=0;
        int aciStart = aci.getBeginIndex();
        int aciIndex = 0;
        char ch = aci.first();
        int runLimit = aciIndex+aciStart;
        Float x=null, y=null, dx=null, dy=null, rotation=null;
        Object baseline=null;
        float shift_x_pos = 0;
        float shift_y_pos = 0;
        float curr_x_pos = (float)offset.getX();
        float curr_y_pos = (float)offset.getY();
        Point2D.Float pos = new Point2D.Float();
        boolean hasArabicTransparent = false;
        while (i < numGlyphs) {
            if (aciIndex+aciStart >= runLimit) {
                runLimit = aci.getRunLimit(runAtts);
                x        = (Float) aci.getAttribute(X);
                y        = (Float) aci.getAttribute(Y);
                dx       = (Float) aci.getAttribute(DX);
                dy       = (Float) aci.getAttribute(DY);
                rotation = (Float) aci.getAttribute(ROTATION);
                baseline = aci.getAttribute(BASELINE_SHIFT);
            }
            GVTGlyphMetrics gm = gv.getGlyphMetrics(i);
            if (i==0) {
                if (isVertical()) {
                    if (glyphOrientationAuto) {
                        if (isLatinChar(ch)) {
                            verticalFirstOffset = 0f;
                        } else {
                            float advY = gm.getVerticalAdvance();
                            float asc  = metrics.getAscent();
                            float dsc  = metrics.getDescent();
                            verticalFirstOffset =  asc+(advY-(asc+dsc))/2;
                        }
                    } else {
                        if (glyphOrientationAngle == 0) {
                            float advY = gm.getVerticalAdvance();
                            float asc  = metrics.getAscent();
                            float dsc  = metrics.getDescent();
                            verticalFirstOffset =  asc+(advY-(asc+dsc))/2;
                        } else {
                            verticalFirstOffset = 0f;
                        }
                    }
                } else {  
                    if ((glyphOrientationAngle == 270)) {
                        horizontalFirstOffset =
                            (float)gm.getBounds2D().getHeight();
                    } else {
                        horizontalFirstOffset = 0;
                    }
                }
            } else {  
                if (glyphOrientationAuto        &&
                    (verticalFirstOffset == 0f) && !isLatinChar(ch)) {
                    float advY = gm.getVerticalAdvance();
                    float asc  = metrics.getAscent();
                    float dsc  = metrics.getDescent();
                    verticalFirstOffset =  asc + (advY - (asc+dsc))/2;
                }
            }
            float ox = 0f;
            float oy = 0f;
            float glyphOrientationRotation = 0f;
            float glyphRotation = 0f;
            if (ch != CharacterIterator.DONE) {
                if (vertical) {
                    if (glyphOrientationAuto) {
                        if (isLatinChar(ch)) {
                            glyphOrientationRotation = (float) (Math.PI / 2f);
                        } else {
                            glyphOrientationRotation = 0f;
                        }
                    } else {
                        glyphOrientationRotation = (float)Math.toRadians(glyphOrientationAngle);
                    }
                    if (textPath != null) {
                        x = null;
                    }
                } else {
                    glyphOrientationRotation = (float)Math.toRadians(glyphOrientationAngle);
                    if (textPath != null) {
                        y = null;
                    }
                }
                if (rotation == null || rotation.isNaN()) {
                    glyphRotation = glyphOrientationRotation;
                } else {
                    glyphRotation = (rotation.floatValue() +
                                     glyphOrientationRotation);
                }
                if ((x != null) && !x.isNaN()) {
                    if (i == 0)
                        shift_x_pos = (float)(x.floatValue()-offset.getX());
                    curr_x_pos = x.floatValue()-shift_x_pos;
                }
                if (dx != null && !dx.isNaN()) {
                    curr_x_pos += dx.floatValue();
                }
                if ((y != null) && !y.isNaN()) {
                    if (i == 0)
                        shift_y_pos = (float)(y.floatValue()-offset.getY());
                    curr_y_pos = y.floatValue()-shift_y_pos;
                }
                if (dy != null && !dy.isNaN()) {
                    curr_y_pos += dy.floatValue();
                } else if (i > 0) {
                    curr_y_pos += gp[i*2 + 1]-gp[i*2 - 1];
                }
                float baselineAdjust = 0f;
                if (baseline != null) {
                    if (baseline instanceof Integer) {
                        if (baseline==TextAttribute.SUPERSCRIPT_SUPER) {
                            baselineAdjust = baselineAscent*0.5f;
                        } else if (baseline==TextAttribute.SUPERSCRIPT_SUB) {
                            baselineAdjust = -baselineAscent*0.5f;
                        }
                    } else if (baseline instanceof Float) {
                        baselineAdjust = ((Float) baseline).floatValue();
                    }
                    if (vertical) {
                        ox = baselineAdjust;
                    } else {
                        oy = -baselineAdjust;
                    }
                }
                if (vertical) {
                    oy += verticalFirstOffset;
                    if (glyphOrientationAuto) {
                        if (isLatinChar(ch)) {
                            ox += metrics.getStrikethroughOffset();
                        } else {
                            Rectangle2D glyphBounds
                                = gv.getGlyphVisualBounds(i).getBounds2D();
                            ox -= (float)((glyphBounds.getMaxX() - gp[2*i]) -
                                          glyphBounds.getWidth()/2);
                        }
                    } else {
                        Rectangle2D glyphBounds
                            = gv.getGlyphVisualBounds(i).getBounds2D();
                        if (glyphOrientationAngle == 0) {
                            ox -= (float)((glyphBounds.getMaxX() - gp[2*i]) -
                                          glyphBounds.getWidth()/2);
                        } else if (glyphOrientationAngle == 180) {
                            ox += (float)((glyphBounds.getMaxX() - gp[2*i]) -
                                          glyphBounds.getWidth()/2);
                        } else if (glyphOrientationAngle == 90) {
                            ox += metrics.getStrikethroughOffset();
                        } else { 
                            ox -= metrics.getStrikethroughOffset();
                        }
                    }
                } else {
                    ox += horizontalFirstOffset;
                    if (glyphOrientationAngle == 90) {
                        oy -= gm.getHorizontalAdvance();
                    } else if (glyphOrientationAngle == 180) {
                        oy -= metrics.getAscent();
                    }
                }
            }
            pos.x = curr_x_pos+ox;
            pos.y = curr_y_pos+oy;
            gv.setGlyphPosition(i, pos);
            if (ArabicTextHandler.arabicCharTransparent(ch)) {
                hasArabicTransparent = true;
            } else {
                if (vertical) {
                    float advanceY = 0;
                    if (glyphOrientationAuto) {
                        if (isLatinChar(ch)) {
                            advanceY = gm.getHorizontalAdvance();
                        } else {
                            advanceY = gm.getVerticalAdvance();
                        }
                    } else {
                        if ((glyphOrientationAngle ==   0) ||
                            (glyphOrientationAngle == 180)) {
                            advanceY = gm.getVerticalAdvance();
                        } else if (glyphOrientationAngle == 90) {
                            advanceY = gm.getHorizontalAdvance();
                        } else { 
                            advanceY = gm.getHorizontalAdvance();
                            gv.setGlyphTransform
                                (i, AffineTransform.getTranslateInstance
                                 (0, advanceY));
                        }
                    }
                    curr_y_pos += advanceY;
                } else {
                    float advanceX = 0;
                    if (glyphOrientationAngle ==   0) {
                        advanceX = gm.getHorizontalAdvance();
                    } else if (glyphOrientationAngle == 180) {
                        advanceX = gm.getHorizontalAdvance();
                        gv.setGlyphTransform
                            (i, AffineTransform.getTranslateInstance
                             (advanceX, 0));
                    } else {
                        advanceX = gm.getVerticalAdvance();
                    }
                    curr_x_pos += advanceX;
                }
            }
            if (!epsEQ(glyphRotation,0)) {
                AffineTransform glyphTransform = gv.getGlyphTransform(i);
                if (glyphTransform == null) {
                    glyphTransform = new AffineTransform();
                }
                AffineTransform rotAt;
                if (epsEQ(glyphRotation, Math.PI/2)) {
                    rotAt = new AffineTransform(0, 1, -1, 0, 0, 0);
                } else if (epsEQ(glyphRotation, Math.PI)) {
                    rotAt = new AffineTransform(-1, 0, 0, -1, 0, 0);
                } else if (epsEQ(glyphRotation, 3*Math.PI/2)) {
                    rotAt = new AffineTransform(0, -1, 1, 0, 0, 0);
                } else {
                    rotAt = AffineTransform.getRotateInstance(glyphRotation);
                }
                glyphTransform.concatenate(rotAt);
                gv.setGlyphTransform(i, glyphTransform);
            }
            aciIndex += gv.getCharacterCount(i,i);
            if (aciIndex >= charMap.length)
                aciIndex = charMap.length-1;
            ch = aci.setIndex(aciIndex+aciStart);
            i++;
        }
        pos.x = curr_x_pos;
        pos.y = curr_y_pos;
        gv.setGlyphPosition(i, pos);
        advance = new Point2D.Float((float)(curr_x_pos - offset.getX()),
                                    (float)(curr_y_pos - offset.getY()));
        if (hasArabicTransparent) {
            ch = aci.first();
            aciIndex = 0;
            i=0;
            int transparentStart = -1;
            while (i < numGlyphs) {
                if (ArabicTextHandler.arabicCharTransparent(ch)) {
                    if (transparentStart == -1) transparentStart = i;
                } else {
                    if (transparentStart != -1) {
                        Point2D         loc   = gv.getGlyphPosition(i);
                        GVTGlyphMetrics gm    = gv.getGlyphMetrics(i);
                        int tyS=0, txS=0;      
                        float advX=0, advY=0;
                        if (vertical) {
                            if (glyphOrientationAuto ||
                                (glyphOrientationAngle == 90))
                                advY = gm.getHorizontalAdvance();
                            else if (glyphOrientationAngle == 270)
                                advY = 0;
                            else if (glyphOrientationAngle == 0)
                                advX = gm.getHorizontalAdvance();
                            else 
                                advX = -gm.getHorizontalAdvance();
                        } else {
                            if (glyphOrientationAngle ==   0)
                                advX = gm.getHorizontalAdvance();
                            else if (glyphOrientationAngle == 90)
                                advY = gm.getHorizontalAdvance();
                            else if (glyphOrientationAngle == 180)
                                advX = 0;
                            else 
                                advY = -gm.getHorizontalAdvance();
                        }
                        float baseX = (float)(loc.getX()+advX);
                        float baseY = (float)(loc.getY()+advY);
                        for (int j=transparentStart; j<i; j++) {
                            Point2D         locT = gv.getGlyphPosition(j);
                            GVTGlyphMetrics gmT  = gv.getGlyphMetrics(j);
                            float           locX = (float)locT.getX();
                            float           locY = (float)locT.getY();
                            float           tx=0, ty=0;
                            float           advT = gmT.getHorizontalAdvance();
                            if (vertical) {
                                if (glyphOrientationAuto ||
                                    (glyphOrientationAngle == 90))
                                    locY = baseY-advT;
                                else if (glyphOrientationAngle == 270)
                                    locY = baseY+advT;
                                else if (glyphOrientationAngle == 0)
                                    locX = baseX-advT;
                                else 
                                    locX = baseX+advT;
                            } else {
                                if (glyphOrientationAngle ==   0)
                                    locX = baseX-advT;
                                else if (glyphOrientationAngle == 90)
                                    locY = baseY-advT;
                                else if (glyphOrientationAngle == 180)
                                    locX = baseX+advT;
                                else 
                                    locY = baseY+advT;
                            }
                            locT = new Point2D.Double(locX, locY);
                            gv.setGlyphPosition(j, locT);
                            if ((txS != 0) || (tyS != 0)) {        
                                AffineTransform at;                
                                at = AffineTransform.getTranslateInstance
                                    (tx,ty);
                                at.concatenate(gv.getGlyphTransform(i));
                                gv.setGlyphTransform(i, at);
                            }
                        }
                        transparentStart = -1;
                    }
                }
                aciIndex += gv.getCharacterCount(i,i);
                if (aciIndex >= charMap.length)
                    aciIndex = charMap.length-1;
                ch = aci.setIndex(aciIndex+aciStart);
                i++;
            }
        }
        layoutApplied  = true;
        spacingApplied = false;
        glyphAdvances  = null;
        pathApplied    = false;
    }
    protected void adjustTextSpacing() {
        if (spacingApplied)
            return;
        if (!layoutApplied)
            doExplicitGlyphLayout();
        aci.first();
        Boolean customSpacing =  (Boolean) aci.getAttribute(
               GVTAttributedCharacterIterator.TextAttribute.CUSTOM_SPACING);
        if ((customSpacing != null) && customSpacing.booleanValue()) {
            advance = doSpacing
                ((Float) aci.getAttribute
                 (GVTAttributedCharacterIterator.TextAttribute.KERNING),
                 (Float) aci.getAttribute
                 (GVTAttributedCharacterIterator.TextAttribute.LETTER_SPACING),
                 (Float) aci.getAttribute
                 (GVTAttributedCharacterIterator.TextAttribute.WORD_SPACING));
            layoutApplied  = false;
        }
        applyStretchTransform(!adjSpacing);
        spacingApplied = true;
        pathApplied    = false;
    }
    protected Point2D doSpacing(Float kern,
                                Float letterSpacing,
                                Float wordSpacing) {
        boolean autoKern = true;
        boolean doWordSpacing = false;
        boolean doLetterSpacing = false;
        float kernVal = 0f;
        float letterSpacingVal = 0f;
        if ((kern != null) && (!kern.isNaN())) {
            kernVal = kern.floatValue();
            autoKern = false;
        }
        if ((letterSpacing != null) && (!letterSpacing.isNaN())) {
            letterSpacingVal = letterSpacing.floatValue();
            doLetterSpacing = true;
        }
        if ((wordSpacing != null) && (!wordSpacing.isNaN())) {
            doWordSpacing = true;
        }
        int numGlyphs = gv.getNumGlyphs();
        float dx = 0f;
        float dy = 0f;
        Point2D[] newPositions = new Point2D[numGlyphs+1];
        Point2D prevPos = gv.getGlyphPosition(0);
        int prevCode    = gv.getGlyphCode(0);
        float x = (float) prevPos.getX();
        float y = (float) prevPos.getY();
        Point2D lastCharAdvance
            = new Point2D.Double(advance.getX() - (gv.getGlyphPosition(numGlyphs-1).getX() - x),
                                 advance.getY() - (gv.getGlyphPosition(numGlyphs-1).getY() - y));
        try {
            GVTFont font = gv.getFont();
            if ((numGlyphs > 1) && (doLetterSpacing || !autoKern)) {
                for (int i=1; i<=numGlyphs; ++i) {
                    Point2D gpos = gv.getGlyphPosition(i);
                    int     currCode;
                    currCode = (i == numGlyphs)?-1:gv.getGlyphCode(i);
                    dx = (float)gpos.getX()-(float)prevPos.getX();
                    dy = (float)gpos.getY()-(float)prevPos.getY();
                    if (autoKern) {
                        if (vertical) dy += letterSpacingVal;
                        else          dx += letterSpacingVal;
                    } else {
                        if (vertical) {
                            float vKern = 0;
                            if (currCode != -1)
                                vKern = font.getVKern(prevCode, currCode);
                            dy += kernVal - vKern + letterSpacingVal;
                        } else {
                            float hKern = 0;
                            if (currCode != -1)
                                hKern = font.getHKern(prevCode, currCode);
                            dx += kernVal - hKern + letterSpacingVal;
                        }
                    }
                    x += dx;
                    y += dy;
                    newPositions[i] = new Point2D.Float(x, y);
                    prevPos = gpos;
                    prevCode = currCode;
                }
                for (int i=1; i<=numGlyphs; ++i) { 
                    if (newPositions[i] != null) {
                        gv.setGlyphPosition(i, newPositions[i]);
                    }
                }
            }
            if (vertical) {
                lastCharAdvance.setLocation
                    (lastCharAdvance.getX(),
                     lastCharAdvance.getY() + kernVal + letterSpacingVal);
            } else {
                lastCharAdvance.setLocation
                    (lastCharAdvance.getX() + kernVal + letterSpacingVal,
                     lastCharAdvance.getY());
            }
            dx = 0f;
            dy = 0f;
            prevPos = gv.getGlyphPosition(0);
            x = (float) prevPos.getX();
            y = (float) prevPos.getY();
            if ((numGlyphs > 1) && (doWordSpacing)) {
                for (int i = 1; i < numGlyphs; i++) {
                    Point2D gpos = gv.getGlyphPosition(i);
                    dx = (float)gpos.getX()-(float)prevPos.getX();
                    dy = (float)gpos.getY()-(float)prevPos.getY();
                    boolean inWS = false;
                    int beginWS = i;
                    int endWS = i;
                    GVTGlyphMetrics gm = gv.getGlyphMetrics(i);
                    while ((gm.getBounds2D().getWidth()<0.01d) || gm.isWhitespace()) {
                        if (!inWS) inWS = true;
                        if (i == numGlyphs-1) {
                            break;
                        }
                        ++i;
                        ++endWS;
                        gpos = gv.getGlyphPosition(i);
                        gm = gv.getGlyphMetrics(i);
                    }
                    if ( inWS ) {  
                        int nWS = endWS-beginWS;
                        float px = (float) prevPos.getX();
                        float py = (float) prevPos.getY();
                        dx = (float) (gpos.getX() - px)/(nWS+1);
                        dy = (float) (gpos.getY() - py)/(nWS+1);
                        if (vertical) {
                            dy += wordSpacing.floatValue()/(nWS+1);
                        } else {
                            dx += wordSpacing.floatValue()/(nWS+1);
                        }
                        for (int j=beginWS; j<=endWS; ++j) {
                            x += dx;
                            y += dy;
                            newPositions[j] = new Point2D.Float(x, y);
                        }
                    } else {
                        dx = (float) (gpos.getX()-prevPos.getX());
                        dy = (float) (gpos.getY()-prevPos.getY());
                        x += dx;
                        y += dy;
                        newPositions[i] = new Point2D.Float(x, y);
                    }
                    prevPos = gpos;
                }
                Point2D gPos = gv.getGlyphPosition(numGlyphs);
                x += (float) (gPos.getX()-prevPos.getX());
                y += (float) (gPos.getY()-prevPos.getY());
                newPositions[numGlyphs] = new Point2D.Float(x, y);
                for (int i=1; i<=numGlyphs; ++i) { 
                    if (newPositions[i] != null) {
                        gv.setGlyphPosition(i, newPositions[i]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        double advX = gv.getGlyphPosition(numGlyphs-1).getX()
                     - gv.getGlyphPosition(0).getX();
        double advY = gv.getGlyphPosition(numGlyphs-1).getY()
                     - gv.getGlyphPosition(0).getY();
        Point2D newAdvance = new Point2D.Double(advX + lastCharAdvance.getX(),
                                                advY + lastCharAdvance.getY());
        return newAdvance;
    }
    protected void applyStretchTransform(boolean stretchGlyphs) {
        if ((xScale == 1) && (yScale==1))
            return;
        AffineTransform scaleAT =
            AffineTransform.getScaleInstance(xScale, yScale);
        int numGlyphs = gv.getNumGlyphs();
        float [] gp   = gv.getGlyphPositions(0, numGlyphs+1, null);
        float initX   = gp[0];
        float initY   = gp[1];
        Point2D.Float pos = new Point2D.Float();
        for (int i = 0; i <= numGlyphs; i++) {
            float dx = gp[2*i]  -initX;
            float dy = gp[2*i+1]-initY;
            pos.x = initX+dx*xScale;
            pos.y = initY+dy*yScale;
            gv.setGlyphPosition(i, pos);
            if ((stretchGlyphs) && (i != numGlyphs)) {
                AffineTransform glyphTransform = gv.getGlyphTransform(i);
                if (glyphTransform != null) {
                    glyphTransform.preConcatenate(scaleAT);
                    gv.setGlyphTransform(i, glyphTransform);
                } else {
                    gv.setGlyphTransform (i, scaleAT);
                }
            }
        }
        advance = new Point2D.Float((float)(advance.getX()*xScale),
                                    (float)(advance.getY()*yScale));
        layoutApplied  = false;
    }
    protected void doPathLayout() {
        if (pathApplied)
            return;
        if (!spacingApplied)
            adjustTextSpacing();
        getGlyphAdvances();
        if (textPath == null) {
            pathApplied = true;
            return;
        }
        boolean horizontal = !isVertical();
        boolean glyphOrientationAuto = isGlyphOrientationAuto();
        int glyphOrientationAngle = 0;
        if (!glyphOrientationAuto) {
            glyphOrientationAngle = getGlyphOrientationAngle();
        }
        float pathLength  = textPath.lengthOfPath();
        float startOffset = textPath.getStartOffset();
        int   numGlyphs   = gv.getNumGlyphs();
        for (int i = 0; i < numGlyphs; i++) {
            gv.setGlyphVisible(i, true);
        }
        float glyphsLength;
        if (horizontal) {
            glyphsLength = (float) gv.getLogicalBounds().getWidth();
        } else {
            glyphsLength = (float) gv.getLogicalBounds().getHeight();
        }
        if (pathLength == 0f || glyphsLength == 0f) {
            pathApplied = true;
            textPathAdvance = advance;
            return;
        }
        Point2D firstGlyphPosition = gv.getGlyphPosition(0);
        float glyphOffset = 0;   
        float currentPosition;
        if (horizontal) {
            glyphOffset     = (float)(firstGlyphPosition.getY());
            currentPosition = (float)(firstGlyphPosition.getX() + startOffset);
        } else {
            glyphOffset     = (float)(firstGlyphPosition.getX());
            currentPosition = (float)(firstGlyphPosition.getY() + startOffset);
        }
        char ch = aci.first();
        int start       = aci.getBeginIndex();
        int currentChar = 0;
        int lastGlyphDrawn = -1;
        float lastGlyphAdvance = 0;
        for (int i = 0; i < numGlyphs; i++) {
            Point2D currentGlyphPos = gv.getGlyphPosition(i);
            float glyphAdvance = 0;  
            float nextGlyphOffset = 0;  
            Point2D nextGlyphPosition = gv.getGlyphPosition(i+1);
            if (horizontal) {
                glyphAdvance    = (float)(nextGlyphPosition.getX() -
                                          currentGlyphPos.getX());
                nextGlyphOffset = (float)(nextGlyphPosition.getY() -
                                          currentGlyphPos.getY());
            } else {
                glyphAdvance    = (float)(nextGlyphPosition.getY() -
                                          currentGlyphPos.getY());
                nextGlyphOffset = (float)(nextGlyphPosition.getX() -
                                          currentGlyphPos.getX());
            }
            Rectangle2D glyphBounds = gv.getGlyphOutline(i).getBounds2D();
            float glyphWidth = (float) glyphBounds.getWidth();
            float glyphHeight = (float) glyphBounds.getHeight();
            float glyphMidX = 0;
            if (glyphWidth > 0) {
                glyphMidX  = (float)(glyphBounds.getX()+glyphWidth/2f);
                glyphMidX -= (float)currentGlyphPos.getX();
            }
            float glyphMidY=0;
            if (glyphHeight > 0) {
                glyphMidY  = (float)(glyphBounds.getY()+glyphHeight/2f);
                glyphMidY -= (float)currentGlyphPos.getY();
            }
            float charMidPos;
            if (horizontal) {
                charMidPos = currentPosition + glyphMidX;
            } else {
                charMidPos = currentPosition + glyphMidY;
            }
            Point2D charMidPoint = textPath.pointAtLength(charMidPos);
            if (charMidPoint != null) {
                float angle = textPath.angleAtLength(charMidPos);
                AffineTransform glyphPathTransform = new AffineTransform();
                if (horizontal) {
                    glyphPathTransform.rotate(angle);
                } else {
                    glyphPathTransform.rotate(angle-(Math.PI/2));
                }
                if (horizontal) {
                    glyphPathTransform.translate(0, glyphOffset);
                } else {
                    glyphPathTransform.translate(glyphOffset, 0);
                }
                if (horizontal) {
                    glyphPathTransform.translate(-glyphMidX, 0f);
                } else {
                    glyphPathTransform.translate(0f, -glyphMidY);
                }
                AffineTransform glyphTransform = gv.getGlyphTransform(i);
                if (glyphTransform != null) {
                    glyphPathTransform.concatenate(glyphTransform);
                }
                gv.setGlyphTransform(i, glyphPathTransform);
                gv.setGlyphPosition (i, charMidPoint);
                lastGlyphDrawn = i;
                lastGlyphAdvance = glyphAdvance;
            } else {
                gv.setGlyphVisible(i, false);
            }
            currentPosition += glyphAdvance;
            glyphOffset += nextGlyphOffset;
            currentChar += gv.getCharacterCount(i,i);
            if (currentChar >= charMap.length)
                currentChar = charMap.length-1;
            ch = aci.setIndex(currentChar+start);
        }
        if (lastGlyphDrawn > -1) {
            Point2D lastGlyphPos = gv.getGlyphPosition(lastGlyphDrawn);
            if (horizontal) {
                textPathAdvance = new Point2D.Double
                    (lastGlyphPos.getX()+lastGlyphAdvance,
                     lastGlyphPos.getY());
            } else {
                textPathAdvance = new Point2D.Double
                    (lastGlyphPos.getX(),
                     lastGlyphPos.getY()+lastGlyphAdvance);
            }
        } else {
            textPathAdvance = new Point2D.Double(0,0);
        }
        layoutApplied  = false;
        spacingApplied = false;
        pathApplied    = true;
    }
    protected boolean isLatinChar(char c) {
        if ( c < 255 && Character.isLetterOrDigit( c )){
            return true;
        }
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        if (block == Character.UnicodeBlock.BASIC_LATIN ||
            block == Character.UnicodeBlock.LATIN_1_SUPPLEMENT ||
            block == Character.UnicodeBlock.LATIN_EXTENDED_ADDITIONAL ||
            block == Character.UnicodeBlock.LATIN_EXTENDED_A ||
            block == Character.UnicodeBlock.LATIN_EXTENDED_B ||
            block == Character.UnicodeBlock.ARABIC ||
            block == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_A ||
            block == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_B) {
            return true;
        }
        return false;
    }
    protected boolean isGlyphOrientationAuto() {
        if (!isVertical()) return false;
        aci.first();
        Integer vOrient = (Integer)aci.getAttribute(VERTICAL_ORIENTATION);
        if (vOrient != null) {
            return (vOrient == ORIENTATION_AUTO);
        }
        return true;
    }
    protected int getGlyphOrientationAngle() {
        int glyphOrientationAngle = 0;
        aci.first();
        Float angle;
        if (isVertical()) {
            angle = (Float)aci.getAttribute(VERTICAL_ORIENTATION_ANGLE);
        } else {
            angle = (Float)aci.getAttribute(HORIZONTAL_ORIENTATION_ANGLE);
        }
        if (angle != null) {
            glyphOrientationAngle = (int)angle.floatValue();
        }
        if ((glyphOrientationAngle !=   0) || (glyphOrientationAngle !=  90) ||       
            (glyphOrientationAngle != 180) || (glyphOrientationAngle != 270)) {       
            while (glyphOrientationAngle < 0) {
                glyphOrientationAngle += 360;
            }
            while (glyphOrientationAngle >= 360) {
                glyphOrientationAngle -= 360;
            }
            if ((glyphOrientationAngle <= 45) ||
                (glyphOrientationAngle > 315)) {
                glyphOrientationAngle = 0;
            } else if ((glyphOrientationAngle > 45) &&
                       (glyphOrientationAngle <= 135)) {
                glyphOrientationAngle = 90;
            } else if ((glyphOrientationAngle > 135) &&
                       (glyphOrientationAngle <= 225)) {
                glyphOrientationAngle = 180;
            } else {
                glyphOrientationAngle = 270;
            }
        }
        return glyphOrientationAngle;
    }
    public boolean hasCharacterIndex(int index){
        for (int n=0; n<charMap.length; n++) {
            if (index == charMap[n])
                return true;
        }
        return false;
    }
    public boolean isAltGlyph(){
        return this.isAltGlyph;
    }
}
