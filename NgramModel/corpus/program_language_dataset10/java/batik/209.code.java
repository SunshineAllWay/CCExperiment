package org.apache.batik.bridge;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import org.apache.batik.gvt.font.Kern;
import org.apache.batik.gvt.font.UnicodeRange;
import org.w3c.dom.Element;
public abstract class SVGKernElementBridge extends AbstractSVGBridge {
    public Kern createKern(BridgeContext ctx,
                           Element kernElement,
                           SVGGVTFont font) {
        String u1 = kernElement.getAttributeNS(null, SVG_U1_ATTRIBUTE);
        String u2 = kernElement.getAttributeNS(null, SVG_U2_ATTRIBUTE);
        String g1 = kernElement.getAttributeNS(null, SVG_G1_ATTRIBUTE);
        String g2 = kernElement.getAttributeNS(null, SVG_G2_ATTRIBUTE);
        String k = kernElement.getAttributeNS(null, SVG_K_ATTRIBUTE);
        if (k.length() == 0) {
            k = SVG_KERN_K_DEFAULT_VALUE;
        }
        float kernValue = Float.parseFloat(k);
        int firstGlyphLen = 0, secondGlyphLen = 0;
        int [] firstGlyphSet = null;
        int [] secondGlyphSet = null;
        List firstUnicodeRanges = new ArrayList();
        List secondUnicodeRanges = new ArrayList();
        StringTokenizer st = new StringTokenizer(u1, ",");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.startsWith("U+")) { 
                firstUnicodeRanges.add(new UnicodeRange(token));
            } else {
                int[] glyphCodes = font.getGlyphCodesForUnicode(token);
                if (firstGlyphSet == null) {
                    firstGlyphSet = glyphCodes;
                    firstGlyphLen = glyphCodes.length;
                }else {
                    if ((firstGlyphLen + glyphCodes.length) >
                        firstGlyphSet.length) {
                        int sz = firstGlyphSet.length*2;
                        if (sz <firstGlyphLen + glyphCodes.length)
                            sz = firstGlyphLen + glyphCodes.length;
                        int [] tmp = new int[sz];
                        System.arraycopy( firstGlyphSet, 0, tmp, 0, firstGlyphLen );
                        firstGlyphSet = tmp;
                    }
                    for (int i = 0; i < glyphCodes.length; i++)
                        firstGlyphSet[firstGlyphLen++] = glyphCodes[i];
                }
            }
        }
        st = new StringTokenizer(u2, ",");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.startsWith("U+")) { 
                secondUnicodeRanges.add(new UnicodeRange(token));
            } else {
                int[] glyphCodes = font.getGlyphCodesForUnicode(token);
                if (secondGlyphSet == null) {
                    secondGlyphSet = glyphCodes;
                    secondGlyphLen = glyphCodes.length;
                } else {
                    if ((secondGlyphLen + glyphCodes.length) >
                        secondGlyphSet.length) {
                        int sz = secondGlyphSet.length*2;
                        if (sz <secondGlyphLen + glyphCodes.length)
                            sz = secondGlyphLen + glyphCodes.length;
                        int [] tmp = new int[sz];
                        System.arraycopy( secondGlyphSet, 0, tmp, 0, secondGlyphLen );
                        secondGlyphSet = tmp;
                    }
                    for (int i = 0; i < glyphCodes.length; i++)
                        secondGlyphSet[secondGlyphLen++] = glyphCodes[i];
                }
            }
        }
        st = new StringTokenizer(g1, ",");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            int[] glyphCodes = font.getGlyphCodesForName(token);
            if (firstGlyphSet == null) {
                firstGlyphSet = glyphCodes;
                firstGlyphLen = glyphCodes.length;
            }else {
                if ((firstGlyphLen + glyphCodes.length) >
                    firstGlyphSet.length) {
                    int sz = firstGlyphSet.length*2;
                    if (sz <firstGlyphLen + glyphCodes.length)
                        sz = firstGlyphLen + glyphCodes.length;
                    int [] tmp = new int[sz];
                    System.arraycopy( firstGlyphSet, 0, tmp, 0, firstGlyphLen );
                    firstGlyphSet = tmp;
                }
                for (int i = 0; i < glyphCodes.length; i++)
                    firstGlyphSet[firstGlyphLen++] = glyphCodes[i];
            }
        }
        st = new StringTokenizer(g2, ",");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            int[] glyphCodes = font.getGlyphCodesForName(token);
            if (secondGlyphSet == null) {
                secondGlyphSet = glyphCodes;
                secondGlyphLen = glyphCodes.length;
            } else {
                if ((secondGlyphLen + glyphCodes.length) >
                    secondGlyphSet.length) {
                    int sz = secondGlyphSet.length*2;
                    if (sz <secondGlyphLen + glyphCodes.length)
                        sz = secondGlyphLen + glyphCodes.length;
                    int [] tmp = new int[sz];
                    System.arraycopy( secondGlyphSet, 0, tmp, 0, secondGlyphLen );
                    secondGlyphSet = tmp;
                }
                for (int i = 0; i < glyphCodes.length; i++)
                    secondGlyphSet[secondGlyphLen++] = glyphCodes[i];
            }
        }
        int[] firstGlyphs;
        if ((firstGlyphLen == 0) ||
            (firstGlyphLen == firstGlyphSet.length)) {
            firstGlyphs = firstGlyphSet;
        } else {
            firstGlyphs = new int[firstGlyphLen];
            System.arraycopy(firstGlyphSet, 0, firstGlyphs, 0, firstGlyphLen);
        }
        int[] secondGlyphs;
        if ((secondGlyphLen == 0) ||
            (secondGlyphLen == secondGlyphSet.length)) {
            secondGlyphs = secondGlyphSet;
        } else {
            secondGlyphs = new int[secondGlyphLen];
            System.arraycopy(secondGlyphSet, 0, secondGlyphs, 0,
                             secondGlyphLen);
        }
        UnicodeRange[] firstRanges;
        firstRanges = new UnicodeRange[firstUnicodeRanges.size()];
        firstUnicodeRanges.toArray(firstRanges);
        UnicodeRange[] secondRanges;
        secondRanges = new UnicodeRange[secondUnicodeRanges.size()];
        secondUnicodeRanges.toArray(secondRanges);
        return new Kern(firstGlyphs, secondGlyphs,
                        firstRanges, secondRanges, kernValue);
    }
}
