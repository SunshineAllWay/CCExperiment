package org.apache.batik.gvt.font;
import java.util.Arrays;
public class Kern {
    private int[] firstGlyphCodes;
    private int[] secondGlyphCodes;
    private UnicodeRange[] firstUnicodeRanges;
    private UnicodeRange[] secondUnicodeRanges;
    private float kerningAdjust;
    public Kern(int[] firstGlyphCodes, 
                int[] secondGlyphCodes,
                UnicodeRange[] firstUnicodeRanges,
                UnicodeRange[] secondUnicodeRanges,
                float adjustValue) {
        this.firstGlyphCodes = firstGlyphCodes;
        this.secondGlyphCodes = secondGlyphCodes;
        this.firstUnicodeRanges = firstUnicodeRanges;
        this.secondUnicodeRanges = secondUnicodeRanges;
        this.kerningAdjust = adjustValue;
        if (firstGlyphCodes != null) 
            Arrays.sort(this.firstGlyphCodes);
        if (secondGlyphCodes != null) 
            Arrays.sort(this.secondGlyphCodes);
    }
    public boolean matchesFirstGlyph(int glyphCode, String glyphUnicode) {
        if (firstGlyphCodes != null) {
            int pt = Arrays.binarySearch(firstGlyphCodes, glyphCode);
            if (pt >= 0) return true;
        }
        if (glyphUnicode.length() < 1) return false;
        char glyphChar = glyphUnicode.charAt(0);
        for (int i = 0; i < firstUnicodeRanges.length; i++) {
            if (firstUnicodeRanges[i].contains(glyphChar))
                return true;
        }
        return false;
    }
    public boolean matchesFirstGlyph(int glyphCode, char glyphUnicode) {
        if (firstGlyphCodes != null) {
            int pt = Arrays.binarySearch(firstGlyphCodes, glyphCode);
            if (pt >= 0) return true;
        }
        for (int i = 0; i < firstUnicodeRanges.length; i++) {
            if (firstUnicodeRanges[i].contains(glyphUnicode))
                return true;
        }
        return false;
    }
    public boolean matchesSecondGlyph(int glyphCode, String glyphUnicode) {
        if (secondGlyphCodes != null) {
            int pt = Arrays.binarySearch(secondGlyphCodes, glyphCode);
            if (pt >= 0) return true;
        }
        if (glyphUnicode.length() < 1) return false;
        char glyphChar = glyphUnicode.charAt(0);
        for (int i = 0; i < secondUnicodeRanges.length; i++) {
            if (secondUnicodeRanges[i].contains(glyphChar))
                return true;
        }
        return false;
    }
    public boolean matchesSecondGlyph(int glyphCode, char glyphUnicode) {
        if (secondGlyphCodes != null) {
            int pt = Arrays.binarySearch(secondGlyphCodes, glyphCode);
            if (pt >= 0) return true;
        }
        for (int i = 0; i < secondUnicodeRanges.length; i++) {
            if (secondUnicodeRanges[i].contains(glyphUnicode))
                return true;
        }
        return false;
    }
    public float getAdjustValue() {
        return kerningAdjust;
    }
}
