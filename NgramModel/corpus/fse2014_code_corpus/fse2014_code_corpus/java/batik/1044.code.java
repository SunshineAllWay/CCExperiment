package org.apache.batik.gvt.text;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
public class BidiAttributedCharacterIterator implements AttributedCharacterIterator {
    private AttributedCharacterIterator reorderedACI;
    private FontRenderContext frc;
    private int chunkStart;
    private int [] newCharOrder;
    private static final Float FLOAT_NAN = new Float(Float.NaN);
    protected BidiAttributedCharacterIterator
        (AttributedCharacterIterator reorderedACI,
         FontRenderContext frc,
         int chunkStart,
         int [] newCharOrder) {
        this.reorderedACI = reorderedACI;
        this.frc = frc;
        this.chunkStart = chunkStart;
        this.newCharOrder = newCharOrder;
    }
    public BidiAttributedCharacterIterator(AttributedCharacterIterator aci,
                                           FontRenderContext           frc,
                                           int chunkStart) {
        this.frc = frc;
        this.chunkStart = chunkStart;
        aci.first();
        int   numChars    = aci.getEndIndex()-aci.getBeginIndex();
        AttributedString as;
        if (false) {
            as = new AttributedString(aci);
        } else {
            StringBuffer strB = new StringBuffer( numChars );
            char c = aci.first();
            for (int i = 0; i < numChars; i++) {
                strB.append(c);
                c = aci.next();
            }
            as = new AttributedString(strB.toString());
            int start=aci.getBeginIndex();
            int end  =aci.getEndIndex();
            int index = start;
            while (index < end) {
                aci.setIndex(index);
                Map attrMap = aci.getAttributes();
                int extent  = aci.getRunLimit();
                Map destMap = new HashMap(attrMap.size());
                Iterator it  = attrMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry e = (Map.Entry)it.next();
                    Object key = e.getKey();
                    if (key == null) continue;
                    Object value = e.getValue();
                    if (value == null) continue;
                    destMap.put(key, value);
                }
                as.addAttributes (destMap, index-start, extent-start);
                index = extent;
            }
        }
        TextLayout tl = new TextLayout(as.getIterator(), frc);
        int[] charIndices = new int[numChars];
        int[] charLevels  = new int[numChars];
        int runStart   = 0;
        int currBiDi   = tl.getCharacterLevel(0);
        charIndices[0] = 0;
        charLevels [0] = currBiDi;
        int maxBiDi    = currBiDi;
        for (int i = 1; i < numChars; i++) {
            int newBiDi = tl.getCharacterLevel(i);
            charIndices[i] = i;
            charLevels [i] = newBiDi;
            if (newBiDi != currBiDi) {
                as.addAttribute
                    (GVTAttributedCharacterIterator.TextAttribute.BIDI_LEVEL,
                     new Integer(currBiDi), runStart, i);
                runStart = i;
                currBiDi  = newBiDi;
                if (newBiDi > maxBiDi) maxBiDi = newBiDi;
            }
        }
        as.addAttribute
            (GVTAttributedCharacterIterator.TextAttribute.BIDI_LEVEL,
             new Integer(currBiDi), runStart, numChars);
        aci = as.getIterator();
        if ((runStart == 0) && (currBiDi==0)) {
            this.reorderedACI = aci;
            newCharOrder = new int[numChars];
            for (int i=0; i<numChars; i++)
                newCharOrder[i] = chunkStart+i;
            return;
        }
        newCharOrder = doBidiReorder(charIndices, charLevels,
                                     numChars, maxBiDi);
        StringBuffer reorderedString = new StringBuffer( numChars );
        int reorderedFirstChar = 0;
        for (int i = 0; i < numChars; i++) {
            int srcIdx = newCharOrder[i];
            char c = aci.setIndex(srcIdx);
            if (srcIdx == 0) reorderedFirstChar = i;
            int bidiLevel = tl.getCharacterLevel(srcIdx);
            if ((bidiLevel & 0x01) != 0) {
                c = (char)mirrorChar(c);
            }
            reorderedString.append(c);
        }
        AttributedString reorderedAS
            = new AttributedString(reorderedString.toString());
        Map [] attrs = new Map[numChars];
        int start=aci.getBeginIndex();
        int end  =aci.getEndIndex();
        int index = start;
        while (index < end) {
            aci.setIndex(index);
            Map attrMap = aci.getAttributes();
            int extent  = aci.getRunLimit();
            for (int i=index; i<extent; i++)
                attrs[i-start] = attrMap;
            index = extent;
        }
        runStart=0;
        Map prevAttrMap = attrs[newCharOrder[0]];
        for (int i = 1; i < numChars; i++) {
            Map attrMap = attrs[newCharOrder[i]];
            if (attrMap != prevAttrMap) {
                reorderedAS.addAttributes(prevAttrMap, runStart, i);
                prevAttrMap = attrMap;
                runStart = i;
            }
        }
        reorderedAS.addAttributes(prevAttrMap, runStart, numChars);
        aci.first();
        Float x = (Float) aci.getAttribute
            (GVTAttributedCharacterIterator.TextAttribute.X);
        if (x != null && !x.isNaN()) {
            reorderedAS.addAttribute
                (GVTAttributedCharacterIterator.TextAttribute.X,
                 FLOAT_NAN, reorderedFirstChar, reorderedFirstChar+1);
            reorderedAS.addAttribute
                (GVTAttributedCharacterIterator.TextAttribute.X, x, 0, 1);
        }
        Float y = (Float) aci.getAttribute
            (GVTAttributedCharacterIterator.TextAttribute.Y);
        if (y != null && !y.isNaN()) {
            reorderedAS.addAttribute
                (GVTAttributedCharacterIterator.TextAttribute.Y,
                 FLOAT_NAN, reorderedFirstChar, reorderedFirstChar+1);
            reorderedAS.addAttribute
                (GVTAttributedCharacterIterator.TextAttribute.Y, y, 0, 1);
        }
        Float dx = (Float) aci.getAttribute
            (GVTAttributedCharacterIterator.TextAttribute.DX);
        if (dx != null && !dx.isNaN()) {
            reorderedAS.addAttribute
                (GVTAttributedCharacterIterator.TextAttribute.DX,
                 FLOAT_NAN, reorderedFirstChar, reorderedFirstChar+1);
            reorderedAS.addAttribute
                (GVTAttributedCharacterIterator.TextAttribute.DX, dx, 0, 1);
        }
        Float dy = (Float) aci.getAttribute
            (GVTAttributedCharacterIterator.TextAttribute.DY);
        if (dy != null && !dy.isNaN()) {
            reorderedAS.addAttribute
                (GVTAttributedCharacterIterator.TextAttribute.DY,
                 FLOAT_NAN, reorderedFirstChar, reorderedFirstChar+1);
            reorderedAS.addAttribute
                (GVTAttributedCharacterIterator.TextAttribute.DY, dy, 0, 1);
        }
        reorderedAS = ArabicTextHandler.assignArabicForms(reorderedAS);
        for (int i=0; i<newCharOrder.length; i++) {
            newCharOrder[i] += chunkStart;
        }
        reorderedACI = reorderedAS.getIterator();
    }
    public int[] getCharMap() { return newCharOrder; }
    private int[] doBidiReorder(int[] charIndices, int[] charLevels,
                                int numChars, int highestLevel) {
        if (highestLevel == 0) return charIndices;
        int currentIndex = 0;
        while (currentIndex < numChars) {
            while ((currentIndex < numChars) &&
                   (charLevels[currentIndex] < highestLevel)) {
                currentIndex++;
            }
            if (currentIndex == numChars) {
                break;
            }
            int startIndex = currentIndex;
            currentIndex++;
            while ((currentIndex < numChars) &&
                   (charLevels[currentIndex] == highestLevel)) {
                currentIndex++;
            }
            int endIndex = currentIndex-1;
            int middle = ((endIndex-startIndex)>>1)+1;
            for (int i = 0; i<middle; i++) {
                int tmp = charIndices[startIndex+i];
                charIndices[startIndex+i] = charIndices[endIndex-i];
                charIndices[endIndex  -i] = tmp;
                charLevels [startIndex+i] = highestLevel-1;
                charLevels [endIndex  -i] = highestLevel-1;
            }
        }
        return doBidiReorder(charIndices, charLevels, numChars, highestLevel-1);
    }
    public Set getAllAttributeKeys() {
        return reorderedACI.getAllAttributeKeys();
    }
    public Object getAttribute(AttributedCharacterIterator.Attribute attribute) {
        return reorderedACI.getAttribute(attribute);
    }
    public Map getAttributes() {
        return reorderedACI.getAttributes();
    }
    public int getRunLimit() {
        return reorderedACI.getRunLimit();
    }
    public int getRunLimit(AttributedCharacterIterator.Attribute attribute) {
        return reorderedACI.getRunLimit(attribute);
    }
    public int getRunLimit(Set attributes) {
        return reorderedACI.getRunLimit(attributes);
    }
    public int getRunStart() {
        return reorderedACI.getRunStart();
    }
    public int getRunStart(AttributedCharacterIterator.Attribute attribute) {
        return reorderedACI.getRunStart(attribute);
    }
    public int getRunStart(Set attributes) {
        return reorderedACI.getRunStart(attributes);
    }
    public Object clone() {
        return new BidiAttributedCharacterIterator
            ((AttributedCharacterIterator)reorderedACI.clone(),
             frc, chunkStart, (int [])newCharOrder.clone());
    }
    public char current() {
        return reorderedACI.current();
    }
    public char first() {
        return reorderedACI.first();
    }
    public int getBeginIndex() {
        return reorderedACI.getBeginIndex();
    }
    public int getEndIndex() {
        return reorderedACI.getEndIndex();
    }
    public int getIndex() {
        return reorderedACI.getIndex();
    }
    public char last() {
        return reorderedACI.last();
    }
    public char next() {
        return reorderedACI.next();
    }
    public char previous() {
        return reorderedACI.previous();
    }
    public char setIndex(int position) {
       return reorderedACI.setIndex(position);
    }
    public static int mirrorChar(int c) {
        switch(c) {
        case 0x0028: return 0x0029;  
        case 0x0029: return 0x0028;  
        case 0x003C: return 0x003E;  
        case 0x003E: return 0x003C;  
        case 0x005B: return 0x005D;  
        case 0x005D: return 0x005B;  
        case 0x007B: return 0x007D;  
        case 0x007D: return 0x007B;  
        case 0x00AB: return 0x00BB;  
        case 0x00BB: return 0x00AB;  
        case 0x2039: return 0x203A;  
        case 0x203A: return 0x2039;  
        case 0x2045: return 0x2046;  
        case 0x2046: return 0x2045;  
        case 0x207D: return 0x207E;  
        case 0x207E: return 0x207D;  
        case 0x208D: return 0x208E;  
        case 0x208E: return 0x208D;  
        case 0x2208: return 0x220B;  
        case 0x2209: return 0x220C;  
        case 0x220A: return 0x220D;  
        case 0x220B: return 0x2208;  
        case 0x220C: return 0x2209;  
        case 0x220D: return 0x220A;  
        case 0x223C: return 0x223D;  
        case 0x223D: return 0x223C;  
        case 0x2243: return 0x22CD;  
        case 0x2252: return 0x2253;  
        case 0x2253: return 0x2252;  
        case 0x2254: return 0x2255;  
        case 0x2255: return 0x2254;  
        case 0x2264: return 0x2265;  
        case 0x2265: return 0x2264;  
        case 0x2266: return 0x2267;  
        case 0x2267: return 0x2266;  
        case 0x2268: return 0x2269;  
        case 0x2269: return 0x2268;  
        case 0x226A: return 0x226B;  
        case 0x226B: return 0x226A;  
        case 0x226E: return 0x226F;  
        case 0x226F: return 0x226E;  
        case 0x2270: return 0x2271;  
        case 0x2271: return 0x2270;  
        case 0x2272: return 0x2273;  
        case 0x2273: return 0x2272;  
        case 0x2274: return 0x2275;  
        case 0x2275: return 0x2274;  
        case 0x2276: return 0x2277;  
        case 0x2277: return 0x2276;  
        case 0x2278: return 0x2279;  
        case 0x2279: return 0x2278;  
        case 0x227A: return 0x227B;  
        case 0x227B: return 0x227A;  
        case 0x227C: return 0x227D;  
        case 0x227D: return 0x227C;  
        case 0x227E: return 0x227F;  
        case 0x227F: return 0x227E;  
        case 0x2280: return 0x2281;  
        case 0x2281: return 0x2280;  
        case 0x2282: return 0x2283;  
        case 0x2283: return 0x2282;  
        case 0x2284: return 0x2285;  
        case 0x2285: return 0x2284;  
        case 0x2286: return 0x2287;  
        case 0x2287: return 0x2286;  
        case 0x2288: return 0x2289;  
        case 0x2289: return 0x2288;  
        case 0x228A: return 0x228B;  
        case 0x228B: return 0x228A;  
        case 0x228F: return 0x2290;  
        case 0x2290: return 0x228F;  
        case 0x2291: return 0x2292;  
        case 0x2292: return 0x2291;  
        case 0x22A2: return 0x22A3;  
        case 0x22A3: return 0x22A2;  
        case 0x22B0: return 0x22B1;  
        case 0x22B1: return 0x22B0;  
        case 0x22B2: return 0x22B3;  
        case 0x22B3: return 0x22B2;  
        case 0x22B4: return 0x22B5;  
        case 0x22B5: return 0x22B4;  
        case 0x22B6: return 0x22B7;  
        case 0x22B7: return 0x22B6;  
        case 0x22C9: return 0x22CA;  
        case 0x22CA: return 0x22C9;  
        case 0x22CB: return 0x22CC;  
        case 0x22CC: return 0x22CB;  
        case 0x22CD: return 0x2243;  
        case 0x22D0: return 0x22D1;  
        case 0x22D1: return 0x22D0;  
        case 0x22D6: return 0x22D7;  
        case 0x22D7: return 0x22D6;  
        case 0x22D8: return 0x22D9;  
        case 0x22D9: return 0x22D8;  
        case 0x22DA: return 0x22DB;  
        case 0x22DB: return 0x22DA;  
        case 0x22DC: return 0x22DD;  
        case 0x22DD: return 0x22DC;  
        case 0x22DE: return 0x22DF;  
        case 0x22DF: return 0x22DE;  
        case 0x22E0: return 0x22E1;  
        case 0x22E1: return 0x22E0;  
        case 0x22E2: return 0x22E3;  
        case 0x22E3: return 0x22E2;  
        case 0x22E4: return 0x22E5;  
        case 0x22E5: return 0x22E4;  
        case 0x22E6: return 0x22E7;  
        case 0x22E7: return 0x22E6;  
        case 0x22E8: return 0x22E9;  
        case 0x22E9: return 0x22E8;  
        case 0x22EA: return 0x22EB;  
        case 0x22EB: return 0x22EA;  
        case 0x22EC: return 0x22ED;  
        case 0x22ED: return 0x22EC;  
        case 0x22F0: return 0x22F1;  
        case 0x22F1: return 0x22F0;  
        case 0x2308: return 0x2309;  
        case 0x2309: return 0x2308;  
        case 0x230A: return 0x230B;  
        case 0x230B: return 0x230A;  
        case 0x2329: return 0x232A;  
        case 0x232A: return 0x2329;  
        case 0x3008: return 0x3009;  
        case 0x3009: return 0x3008;  
        case 0x300A: return 0x300B;  
        case 0x300B: return 0x300A;  
        case 0x300C: return 0x300D;  
        case 0x300D: return 0x300C;  
        case 0x300E: return 0x300F;  
        case 0x300F: return 0x300E;  
        case 0x3010: return 0x3011;  
        case 0x3011: return 0x3010;  
        case 0x3014: return 0x3015;  
        case 0x3015: return 0x3014;  
        case 0x3016: return 0x3017;  
        case 0x3017: return 0x3016;  
        case 0x3018: return 0x3019;  
        case 0x3019: return 0x3018;  
        case 0x301A: return 0x301B;  
        case 0x301B: return 0x301A;  
        default: break;
        }
        return  c;
    }
}
