package org.apache.batik.bridge;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import org.apache.batik.css.engine.SVGCSSEngine;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.gvt.font.GVTFont;
import org.apache.batik.gvt.font.GVTFontFace;
import org.apache.batik.gvt.font.GVTGlyphVector;
import org.apache.batik.gvt.font.GVTLineMetrics;
import org.apache.batik.gvt.font.Glyph;
import org.apache.batik.gvt.font.Kern;
import org.apache.batik.gvt.font.KerningTable;
import org.apache.batik.gvt.font.SVGGVTGlyphVector;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Element;
public final class SVGGVTFont implements GVTFont, SVGConstants {
    public static final AttributedCharacterIterator.Attribute PAINT_INFO
        = GVTAttributedCharacterIterator.TextAttribute.PAINT_INFO;
    private float fontSize;
    private GVTFontFace fontFace;
    private String[] glyphUnicodes;
    private String[] glyphNames;
    private String[] glyphLangs;
    private String[] glyphOrientations;
    private String[] glyphForms;
    private Element[] glyphElements;
    private Element[] hkernElements;
    private Element[] vkernElements;
    private BridgeContext ctx;
    private Element textElement;
    private Element missingGlyphElement;
    private KerningTable hKerningTable;
    private KerningTable vKerningTable;
    private String language;
    private String orientation;
    private float  scale;
    private GVTLineMetrics lineMetrics=null;
    public SVGGVTFont(float fontSize,
                      GVTFontFace fontFace,
                      String[] glyphUnicodes,
                      String[] glyphNames,
                      String[] glyphLangs,
                      String[] glyphOrientations,
                      String[] glyphForms,
                      BridgeContext ctx,
                      Element[] glyphElements,
                      Element missingGlyphElement,
                      Element[] hkernElements,
                      Element[] vkernElements,
                      Element textElement) {
        this.fontFace = fontFace;
        this.fontSize = fontSize;
        this.glyphUnicodes = glyphUnicodes;
        this.glyphNames = glyphNames;
        this.glyphLangs = glyphLangs;
        this.glyphOrientations = glyphOrientations;
        this.glyphForms = glyphForms;
        this.ctx = ctx;
        this.glyphElements = glyphElements;
        this.missingGlyphElement = missingGlyphElement;
        this.hkernElements = hkernElements;
        this.vkernElements = vkernElements;
        this.scale         = fontSize/fontFace.getUnitsPerEm();
        this.textElement = textElement;
        this.language = XMLSupport.getXMLLang(textElement);
        Value v = CSSUtilities.getComputedStyle
            (textElement, SVGCSSEngine.WRITING_MODE_INDEX);
        if (v.getStringValue().startsWith(CSS_TB_VALUE)) {
            this.orientation = SVG_V_VALUE;
        } else {
            this.orientation = SVG_H_VALUE;
        }
        createKerningTables();
    }
    private void createKerningTables() {
        Kern[] hEntries = new Kern[hkernElements.length];
        for (int i = 0; i < hkernElements.length; i++) {
            Element hkernElement = hkernElements[i];
            SVGHKernElementBridge hkernBridge =
                (SVGHKernElementBridge)ctx.getBridge(hkernElement);
            Kern hkern = hkernBridge.createKern(ctx, hkernElement, this);
            hEntries[i] = hkern;
        }
        hKerningTable = new KerningTable(hEntries);
        Kern[] vEntries = new Kern[vkernElements.length];
        for (int i = 0; i < vkernElements.length; i++) {
            Element vkernElement = vkernElements[i];
            SVGVKernElementBridge vkernBridge =
                (SVGVKernElementBridge)ctx.getBridge(vkernElement);
            Kern vkern = vkernBridge.createKern(ctx, vkernElement, this);
            vEntries[i] = vkern;
        }
        vKerningTable = new KerningTable(vEntries);
    }
    public float getHKern(int glyphCode1, int glyphCode2) {
        if (glyphCode1 < 0 || glyphCode1 >= glyphUnicodes.length
            || glyphCode2 < 0 || glyphCode2 >= glyphUnicodes.length) {
            return 0f;
        }
        float ret;
        ret = hKerningTable.getKerningValue(glyphCode1, glyphCode2,
                                            glyphUnicodes[glyphCode1],
                                            glyphUnicodes[glyphCode2]);
        return ret*scale;
    }
    public float getVKern(int glyphCode1, int glyphCode2) {
        if (glyphCode1 < 0 || glyphCode1 >= glyphUnicodes.length
            || glyphCode2 < 0 || glyphCode2 >= glyphUnicodes.length) {
            return 0f;
        }
        float ret;
        ret = vKerningTable.getKerningValue(glyphCode1, glyphCode2,
                                            glyphUnicodes[glyphCode1],
                                            glyphUnicodes[glyphCode2]);
        return ret*scale;
    }
    public int[] getGlyphCodesForName(String name) {
        List glyphCodes = new ArrayList();
        for (int i = 0; i < glyphNames.length; i++) {
            if (glyphNames[i] != null && glyphNames[i].equals(name)) {
                glyphCodes.add(new Integer(i));
            }
        }
        int[] glyphCodeArray = new int[glyphCodes.size()];
        for (int i = 0; i < glyphCodes.size(); i++) {
            glyphCodeArray[i] = ((Integer)glyphCodes.get(i)).intValue();
        }
        return glyphCodeArray;
    }
    public int[] getGlyphCodesForUnicode(String unicode) {
        List glyphCodes = new ArrayList();
        for (int i = 0; i < glyphUnicodes.length; i++) {
            if (glyphUnicodes[i] != null && glyphUnicodes[i].equals(unicode)) {
                glyphCodes.add(new Integer(i));
            }
        }
        int[] glyphCodeArray = new int[glyphCodes.size()];
        for (int i = 0; i < glyphCodes.size(); i++) {
            glyphCodeArray[i] = ((Integer)glyphCodes.get(i)).intValue();
        }
        return glyphCodeArray;
    }
    private boolean languageMatches(String glyphLang) {
        if (glyphLang == null || glyphLang.length() == 0) {
            return true;  
        }
        StringTokenizer st = new StringTokenizer(glyphLang, ",");
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (s.equals(language)
               || (s.startsWith(language) && s.length() > language.length()
                   && s.charAt(language.length()) == '-')) {
                return true;
            }
        }
        return false;
    }
    private boolean orientationMatches(String glyphOrientation) {
        if (glyphOrientation == null || glyphOrientation.length() == 0) {
            return true;
        }
        return glyphOrientation.equals(orientation);
    }
    private boolean formMatches(String glyphUnicode,
                                String glyphForm,
                                AttributedCharacterIterator aci,
                                int currentIndex) {
        if (aci == null || glyphForm == null || glyphForm.length() == 0) {
            return true;
        }
        char c = aci.setIndex(currentIndex);
        Integer form = (Integer)aci.getAttribute
            (GVTAttributedCharacterIterator.TextAttribute.ARABIC_FORM);
        if (form == null || form.equals
            (GVTAttributedCharacterIterator.TextAttribute.ARABIC_NONE)) {
            return false;
        }
        if (glyphUnicode.length() > 1) {
            boolean matched = true;
            for (int j = 1; j < glyphUnicode.length(); j++) {
                c = aci.next();
                if (glyphUnicode.charAt(j) != c) {
                    matched = false;
                    break;
                }
            }
            aci.setIndex(currentIndex);
            if (matched) {
                aci.setIndex(currentIndex + glyphUnicode.length() - 1);
                Integer lastForm = (Integer)aci.getAttribute(
                    GVTAttributedCharacterIterator.TextAttribute.ARABIC_FORM);
                aci.setIndex(currentIndex);
                if (form != null && lastForm != null) {
                    if (form.equals(GVTAttributedCharacterIterator.
                                    TextAttribute.ARABIC_TERMINAL) &&
                        lastForm.equals(GVTAttributedCharacterIterator.
                                        TextAttribute.ARABIC_INITIAL)) {
                        return glyphForm.equals
                            (SVGConstants.SVG_ISOLATED_VALUE);
                    } else if (form.equals(GVTAttributedCharacterIterator.
                                           TextAttribute.ARABIC_TERMINAL)) {
                        return glyphForm.equals
                            (SVGConstants.SVG_TERMINAL_VALUE);
                    } else if (form.equals(GVTAttributedCharacterIterator.
                                           TextAttribute.ARABIC_MEDIAL) &&
                               lastForm.equals(GVTAttributedCharacterIterator.
                                               TextAttribute.ARABIC_MEDIAL)) {
                        return glyphForm.equals(SVGConstants.SVG_MEDIAL_VALUE);
                    }
                }
            }
        }
        if (form.equals(GVTAttributedCharacterIterator.
                        TextAttribute.ARABIC_ISOLATED)) {
            return glyphForm.equals(SVGConstants.SVG_ISOLATED_VALUE);
        }
        if (form.equals(GVTAttributedCharacterIterator.
                        TextAttribute.ARABIC_TERMINAL)) {
            return glyphForm.equals(SVGConstants.SVG_TERMINAL_VALUE);
        }
        if (form.equals(GVTAttributedCharacterIterator.
                        TextAttribute.ARABIC_INITIAL)) {
            return glyphForm.equals(SVGConstants.SVG_INITIAL_VALUE);
        }
        if (form.equals(GVTAttributedCharacterIterator.
                        TextAttribute.ARABIC_MEDIAL)) {
            return glyphForm.equals(SVGConstants.SVG_MEDIAL_VALUE);
        }
        return false;
    }
    public boolean canDisplayGivenName(String name) {
        for (int i = 0; i < glyphNames.length; i++) {
            if (glyphNames[i] != null && glyphNames[i].equals(name)
                && languageMatches(glyphLangs[i])
                && orientationMatches(glyphOrientations[i])) {
                return true;
            }
        }
        return false;
    }
    public boolean canDisplay(char c) {
        for (int i = 0; i < glyphUnicodes.length; i++) {
            if (glyphUnicodes[i].indexOf(c) != -1
                && languageMatches(glyphLangs[i])
                && orientationMatches(glyphOrientations[i])) {
                return true;
            }
        }
        return false;
    }
    public int canDisplayUpTo(char[] text, int start, int limit) {
        StringCharacterIterator sci =
            new StringCharacterIterator(new String(text));
        return canDisplayUpTo(sci, start, limit);
    }
    public int canDisplayUpTo(CharacterIterator iter, int start, int limit) {
        AttributedCharacterIterator aci = null;
        if (iter instanceof AttributedCharacterIterator) {
            aci = (AttributedCharacterIterator)iter;
        }
        char c = iter.setIndex(start);
        int currentIndex = start;
        while (c != CharacterIterator.DONE && currentIndex < limit) {
            boolean foundMatchingGlyph = false;
            for (int i = 0; i < glyphUnicodes.length; i++) {
                if (glyphUnicodes[i].indexOf(c) == 0
                    && languageMatches(glyphLangs[i])
                    && orientationMatches(glyphOrientations[i])
                    && formMatches(glyphUnicodes[i], glyphForms[i],
                                   aci, currentIndex)) {
                    if (glyphUnicodes[i].length() == 1)  { 
                        foundMatchingGlyph = true;
                        break;
                    } else {
                        boolean matched = true;
                        for (int j = 1; j < glyphUnicodes[i].length(); j++) {
                            c = iter.next();
                            if (glyphUnicodes[i].charAt(j) != c) {
                                matched = false;
                                break;
                            }
                        }
                        if (matched) { 
                            foundMatchingGlyph = true;
                            break;
                        } else {
                            c = iter.setIndex(currentIndex);
                        }
                    }
                }
            }
            if (!foundMatchingGlyph) {
                return currentIndex;
            }
            c = iter.next();
            currentIndex = iter.getIndex();
        }
        return -1;
    }
    public int canDisplayUpTo(String str) {
        StringCharacterIterator sci = new StringCharacterIterator(str);
        return canDisplayUpTo(sci, 0, str.length());
    }
    public GVTGlyphVector createGlyphVector(FontRenderContext frc,
                                            char[] chars) {
         StringCharacterIterator sci =
             new StringCharacterIterator(new String(chars));
         return createGlyphVector(frc, sci);
    }
    public GVTGlyphVector createGlyphVector(FontRenderContext frc,
                                            CharacterIterator ci) {
        AttributedCharacterIterator aci = null;
        if (ci instanceof AttributedCharacterIterator) {
            aci = (AttributedCharacterIterator)ci;
        }
        List glyphs = new ArrayList();
        char c = ci.first();
        while (c != CharacterIterator.DONE) {
            boolean foundMatchingGlyph = false;
            for (int i = 0; i < glyphUnicodes.length; i++) {
                if (glyphUnicodes[i].indexOf(c) == 0 &&
                    languageMatches(glyphLangs[i]) &&
                    orientationMatches(glyphOrientations[i]) &&
                    formMatches(glyphUnicodes[i], glyphForms[i], aci,
                                ci.getIndex())) {  
                    if (glyphUnicodes[i].length() == 1)  { 
                        Element glyphElement = glyphElements[i];
                        SVGGlyphElementBridge glyphBridge =
                            (SVGGlyphElementBridge)ctx.getBridge(glyphElement);
                        TextPaintInfo tpi = null;
                        if (aci != null) {
                            tpi = (TextPaintInfo)aci.getAttribute(PAINT_INFO);
                        }
                        Glyph glyph = glyphBridge.createGlyph
                            (ctx, glyphElement, textElement, i,
                             fontSize, fontFace, tpi);
                        glyphs.add(glyph);
                        foundMatchingGlyph = true;
                        break;
                    } else {
                        int current = ci.getIndex();
                        boolean matched = true;
                        for (int j = 1; j < glyphUnicodes[i].length(); j++) {
                            c = ci.next();
                            if (glyphUnicodes[i].charAt(j) != c) {
                                matched = false;
                                break;
                            }
                        }
                        if (matched) { 
                            Element glyphElement = glyphElements[i];
                            SVGGlyphElementBridge glyphBridge
                                = (SVGGlyphElementBridge)ctx.getBridge
                                (glyphElement);
                            TextPaintInfo tpi = null;
                            if (aci != null) {
                                aci.setIndex(ci.getIndex());
                                tpi = (TextPaintInfo)aci.getAttribute
                                    (PAINT_INFO);
                            }
                            Glyph glyph = glyphBridge.createGlyph
                                (ctx, glyphElement, textElement, i,
                                 fontSize, fontFace, tpi);
                            glyphs.add(glyph);
                            foundMatchingGlyph = true;
                            break;
                        } else {
                            c = ci.setIndex(current);
                        }
                    }
                }
            }
            if (!foundMatchingGlyph) {
                SVGGlyphElementBridge glyphBridge =
                    (SVGGlyphElementBridge)ctx.getBridge(missingGlyphElement);
                TextPaintInfo tpi = null;
                if (aci != null) {
                    aci.setIndex(ci.getIndex());
                    tpi = (TextPaintInfo)aci.getAttribute(PAINT_INFO);
                }
                Glyph glyph = glyphBridge.createGlyph
                    (ctx, missingGlyphElement, textElement, -1,
                     fontSize, fontFace, tpi);
                glyphs.add(glyph);
            }
            c = ci.next();
        }
        int numGlyphs = glyphs.size();
        Glyph[] glyphArray = (Glyph[])glyphs.toArray( new Glyph[numGlyphs] );
        return new SVGGVTGlyphVector(this, glyphArray, frc);
    }
    public GVTGlyphVector createGlyphVector(FontRenderContext frc,
                                            int[] glyphCodes,
                                            CharacterIterator ci) {
        int nGlyphs = glyphCodes.length;
        StringBuffer workBuff = new StringBuffer( nGlyphs );
        for (int i = 0; i < nGlyphs; i++) {
            workBuff.append( glyphUnicodes[glyphCodes[i]] );
        }
        StringCharacterIterator sci = new StringCharacterIterator( workBuff.toString() );
        return createGlyphVector(frc, sci);
    }
    public GVTGlyphVector createGlyphVector(FontRenderContext frc,
                                            String str) {
        StringCharacterIterator sci = new StringCharacterIterator(str);
        return createGlyphVector(frc, sci);
    }
    public GVTFont deriveFont(float size) {
        return new SVGGVTFont(size, fontFace, glyphUnicodes, glyphNames,
                              glyphLangs, glyphOrientations, glyphForms, ctx,
                              glyphElements, missingGlyphElement,
                              hkernElements, vkernElements, textElement);
    }
    public String getFamilyName() {
        return fontFace.getFamilyName();
    }
    protected GVTLineMetrics getLineMetrics(int beginIndex, int limit) {
        if (lineMetrics != null)
            return lineMetrics;
        float fontHeight = fontFace.getUnitsPerEm();
        float scale = fontSize/fontHeight;
        float ascent = fontFace.getAscent() * scale;
        float descent = fontFace.getDescent() * scale;
        float[] baselineOffsets = new float[3];
        baselineOffsets[Font.ROMAN_BASELINE]   = 0;
        baselineOffsets[Font.CENTER_BASELINE]  = (ascent+descent)/2-ascent;
        baselineOffsets[Font.HANGING_BASELINE] = -ascent;
        float stOffset    = fontFace.getStrikethroughPosition() * -scale;
        float stThickness = fontFace.getStrikethroughThickness() * scale;
        float ulOffset    = fontFace.getUnderlinePosition() * scale;
        float ulThickness = fontFace.getUnderlineThickness() * scale;
        float olOffset    = fontFace.getOverlinePosition() * -scale;
        float olThickness = fontFace.getOverlineThickness() * scale;
        lineMetrics = new GVTLineMetrics
            (ascent, Font.ROMAN_BASELINE, baselineOffsets, descent,
             fontHeight, fontHeight, limit-beginIndex,
             stOffset, stThickness,
             ulOffset, ulThickness,
             olOffset, olThickness);
        return lineMetrics;
    }
    public GVTLineMetrics getLineMetrics(char[] chars, int beginIndex,
                                         int limit,
                                         FontRenderContext frc) {
        return getLineMetrics(beginIndex, limit);
    }
    public GVTLineMetrics getLineMetrics(CharacterIterator ci, int beginIndex,
                                         int limit, FontRenderContext frc) {
        return getLineMetrics(beginIndex, limit);
    }
    public GVTLineMetrics getLineMetrics(String str, FontRenderContext frc) {
        StringCharacterIterator sci = new StringCharacterIterator(str);
        return getLineMetrics(sci, 0, str.length(), frc);
    }
    public GVTLineMetrics getLineMetrics(String str, int beginIndex, int limit,
                                         FontRenderContext frc) {
        StringCharacterIterator sci = new StringCharacterIterator(str);
        return getLineMetrics(sci, beginIndex, limit, frc);
    }
    public float getSize() {
        return fontSize;
    }
    public String toString() {
        return fontFace.getFamilyName() + " " + fontFace.getFontWeight() + " "
              + fontFace.getFontStyle();
    }
}
