package org.apache.batik.gvt.font;
public class KerningTable {
    private Kern[] entries;
    public KerningTable(Kern[] entries) {
        this.entries = entries;
    }
    public float getKerningValue(int glyphCode1, 
                                 int glyphCode2,
                                 String glyphUnicode1, 
                                 String glyphUnicode2) {
        for (int i = 0; i < entries.length; i++) {
            if (entries[i].matchesFirstGlyph(glyphCode1, glyphUnicode1) &&
                entries[i].matchesSecondGlyph(glyphCode2, glyphUnicode2)) {
                return entries[i].getAdjustValue();
            }
        }
        return 0f;
    }
}
