package org.apache.batik.extension.svg;
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;
import org.apache.batik.gvt.font.GVTGlyphVector;
public class LineInfo {
    Point2D.Float               loc;
    AttributedCharacterIterator aci;
    GVTGlyphVector              gv;
    int                         startIdx;
    int                         endIdx;
    float                       advance;
    float                       visualAdvance;
    float                       lastCharWidth;
    float                       lineWidth;
    boolean                     partial;
    Point2D.Float               verticalAlignOffset;
    public LineInfo(Point2D.Float loc,
                    AttributedCharacterIterator aci,
                    GVTGlyphVector gv,
                    int startIdx, int endIdx,
                    float advance,
                    float visualAdvance,
                    float lastCharWidth,
                    float lineWidth,
                    boolean partial,
                    Point2D.Float verticalAlignOffset) {
        this.loc           = loc;
        this.aci           = aci;
        this.gv            = gv;
        this.startIdx      = startIdx;
        this.endIdx        = endIdx;
        this.advance       = advance;
        this.visualAdvance = visualAdvance;
        this.lastCharWidth = lastCharWidth;
        this.lineWidth     = lineWidth;
        this.partial       = partial;
        this.verticalAlignOffset = verticalAlignOffset;
    }
    public Point2D.Float  getLocation()         { return loc; }
    public AttributedCharacterIterator getACI() { return aci; }
    public GVTGlyphVector getGlyphVector()      { return gv; }
    public int            getStartIdx()         { return startIdx; }
    public int            getEndIdx()           { return endIdx; }
    public float          getAdvance()          { return advance; }
    public float          getVisualAdvance()    { return visualAdvance; }
    public float          getLastCharWidth()    { return lastCharWidth; }
    public float          getLineWidth()        { return lineWidth; }
    public boolean        isPartialLine()       { return partial; }
    public Point2D.Float  getVerticalAlignOffset()    { return verticalAlignOffset; }
    public String         toString() {
        return "[LineInfo loc: " + loc
                + " [" + startIdx + ',' + endIdx + "] "
                + " LWidth: " + lineWidth
                + " Adv: " + advance
                + " VAdv: " + visualAdvance
                + " LCW: " + lastCharWidth
                + " Partial: " + partial
                + " verticalAlignOffset: " + verticalAlignOffset;
    }
}
