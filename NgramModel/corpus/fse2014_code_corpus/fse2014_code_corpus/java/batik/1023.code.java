package org.apache.batik.gvt.font;
import java.awt.font.LineMetrics;
public class GVTLineMetrics {
    protected float ascent;
    protected int baselineIndex;
    protected float[] baselineOffsets;
    protected float descent;
    protected float height;
    protected float leading;
    protected int numChars;
    protected float strikethroughOffset;
    protected float strikethroughThickness;
    protected float underlineOffset;
    protected float underlineThickness;
    protected float overlineOffset;
    protected float overlineThickness;
    public GVTLineMetrics(LineMetrics lineMetrics) {
        this.ascent = lineMetrics.getAscent();
        this.baselineIndex = lineMetrics.getBaselineIndex();
        this.baselineOffsets = lineMetrics.getBaselineOffsets();
        this.descent = lineMetrics.getDescent();
        this.height = lineMetrics.getHeight();
        this.leading = lineMetrics.getLeading();
        this.numChars = lineMetrics.getNumChars();
        this.strikethroughOffset = lineMetrics.getStrikethroughOffset();
        this.strikethroughThickness = lineMetrics.getStrikethroughThickness();
        this.underlineOffset = lineMetrics.getUnderlineOffset();
        this.underlineThickness = lineMetrics.getUnderlineThickness();
        this.overlineOffset = -this.ascent;
        this.overlineThickness = this.underlineThickness;
    }
    public GVTLineMetrics(LineMetrics lineMetrics, float scaleFactor) {
        this.ascent = lineMetrics.getAscent() * scaleFactor;
        this.baselineIndex = lineMetrics.getBaselineIndex();
        this.baselineOffsets = lineMetrics.getBaselineOffsets();
        for (int i=0; i<baselineOffsets.length; i++) {
            this.baselineOffsets[i] *= scaleFactor;
        }
        this.descent = lineMetrics.getDescent() * scaleFactor;
        this.height = lineMetrics.getHeight() * scaleFactor;
        this.leading = lineMetrics.getLeading();
        this.numChars = lineMetrics.getNumChars();
        this.strikethroughOffset = 
            lineMetrics.getStrikethroughOffset() * scaleFactor;
        this.strikethroughThickness = 
            lineMetrics.getStrikethroughThickness() * scaleFactor;
        this.underlineOffset = lineMetrics.getUnderlineOffset() * scaleFactor;
        this.underlineThickness = 
            lineMetrics.getUnderlineThickness() * scaleFactor;
        this.overlineOffset = -this.ascent;
        this.overlineThickness = this.underlineThickness;
    }
    public GVTLineMetrics(float ascent, 
                          int baselineIndex, 
                          float[] baselineOffsets,
                          float descent, 
                          float height, 
                          float leading, int numChars,
                          float strikethroughOffset, 
                          float strikethroughThickness,
                          float underlineOffset, 
                          float underlineThickness,
                          float overlineOffset, 
                          float overlineThickness) {
        this.ascent = ascent;
        this.baselineIndex = baselineIndex;
        this.baselineOffsets = baselineOffsets;
        this.descent = descent;
        this.height = height;
        this.leading = leading;
        this.numChars = numChars;
        this.strikethroughOffset = strikethroughOffset;
        this.strikethroughThickness = strikethroughThickness;
        this.underlineOffset = underlineOffset;
        this.underlineThickness = underlineThickness;
        this.overlineOffset = overlineOffset;
        this.overlineThickness = overlineThickness;
    }
    public float getAscent() {
        return ascent;
    }
    public int getBaselineIndex() {
        return baselineIndex;
    }
    public float[] getBaselineOffsets() {
        return baselineOffsets;
    }
    public float getDescent() {
        return descent;
    }
    public float getHeight() {
        return height;
    }
    public float getLeading() {
        return leading;
    }
    public int getNumChars() {
        return numChars;
    }
    public float getStrikethroughOffset() {
        return strikethroughOffset;
    }
    public float getStrikethroughThickness() {
        return strikethroughThickness;
    }
    public float getUnderlineOffset() {
        return underlineOffset;
    }
    public float getUnderlineThickness() {
        return underlineThickness;
    }
    public float getOverlineOffset() {
        return overlineOffset;
    }
    public float getOverlineThickness() {
        return overlineThickness;
    }
}
