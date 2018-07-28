package org.apache.batik.gvt.flow;
import org.apache.batik.gvt.font.GVTGlyphVector;
class GlyphGroupInfo {
    int start, end;
    int glyphCount, lastGlyphCount;
    boolean hideLast;
    float advance, lastAdvance;
    int range;
    GVTGlyphVector gv;
    boolean [] hide;
    public GlyphGroupInfo(GVTGlyphVector gv, 
                          int start,
                          int end,
                          boolean  [] glyphHide,
                          boolean glyphGroupHideLast,
                          float   [] glyphPos,
                          float   [] advAdj,
                          float   [] lastAdvAdj,
                          boolean [] space) {
        this.gv             = gv;
        this.start          = start;
        this.end            = end;
        this.hide           = new boolean[this.end-this.start+1];
        this.hideLast       = glyphGroupHideLast;
        System.arraycopy(glyphHide, this.start, this.hide, 0, 
                         this.hide.length);
        float adv  = glyphPos[2*end+2]-glyphPos[2*start];
        float ladv = adv;
        adv += advAdj[end];
        int glyphCount = end-start+1;
        for (int g=start; g<end; g++) {
            if (glyphHide[g]) glyphCount--;
        }
        int lastGlyphCount = glyphCount;
        for (int g=end; g>=start; g--) {
            ladv += lastAdvAdj[g];
            if (!space[g]) break;
            lastGlyphCount--;
        }
        if (hideLast) lastGlyphCount--;
        this.glyphCount     = glyphCount;
        this.lastGlyphCount = lastGlyphCount;
        this.advance        = adv;
        this.lastAdvance    = ladv;
    }
    public GVTGlyphVector getGlyphVector() { return gv; }
    public int     getStart() { return start; }
    public int     getEnd() { return end; }
    public int     getGlyphCount() { return glyphCount; }
    public int     getLastGlyphCount() { return lastGlyphCount; }
    public boolean [] getHide() { return hide; }
    public boolean getHideLast() { return hideLast; }
    public float   getAdvance() { return advance; }
    public float   getLastAdvance() { return lastAdvance; }
    public void setRange(int range) { this.range = range; }
    public int getRange() { return this.range; }
}
