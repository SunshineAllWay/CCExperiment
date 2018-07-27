package org.apache.batik.gvt.text;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Map;
public class ArabicTextHandler {
    private static final int arabicStart = 0x0600;
    private static final int arabicEnd = 0x06FF;
    private static final AttributedCharacterIterator.Attribute ARABIC_FORM =
        GVTAttributedCharacterIterator.TextAttribute.ARABIC_FORM;
    private static final Integer ARABIC_NONE =
        GVTAttributedCharacterIterator.TextAttribute.ARABIC_NONE;
    private static final Integer ARABIC_ISOLATED =
        GVTAttributedCharacterIterator.TextAttribute.ARABIC_ISOLATED;
    private static final Integer ARABIC_TERMINAL =
        GVTAttributedCharacterIterator.TextAttribute.ARABIC_TERMINAL;
    private static final Integer ARABIC_INITIAL =
        GVTAttributedCharacterIterator.TextAttribute.ARABIC_INITIAL;
    private static final Integer ARABIC_MEDIAL =
        GVTAttributedCharacterIterator.TextAttribute.ARABIC_MEDIAL;
    private ArabicTextHandler() {
    }
    public static AttributedString assignArabicForms(AttributedString as) {
        if (!containsArabic(as)) {
            return as;
        }
        AttributedCharacterIterator aci = as.getIterator();
        int numChars = aci.getEndIndex() - aci.getBeginIndex();
        int[] charOrder = null;
        if (numChars >= 3) {
            char prevChar = aci.first();
            char c        = aci.next();
            int  i        = 1;
            for (char nextChar = aci.next();
                 nextChar != AttributedCharacterIterator.DONE;
                 prevChar = c, c = nextChar, nextChar = aci.next(), i++) {
                if (arabicCharTransparent(c)) {
                    if (hasSubstitute(prevChar, nextChar)) {
                        if (charOrder == null) {
                            charOrder = new int[numChars];
                            for (int j = 0; j < numChars; j++) {
                                charOrder[j] = j + aci.getBeginIndex();
                            }
                        }
                        int temp = charOrder[i];
                        charOrder[i] = charOrder[i-1];
                        charOrder[i-1] = temp;
                    }
                }
            }
        }
        if (charOrder != null) {
            StringBuffer reorderedString = new StringBuffer(numChars);
            char c;
            for (int i = 0; i < numChars; i++) {
                c = aci.setIndex(charOrder[i]);
                reorderedString.append( c );
            }
            AttributedString reorderedAS;
            reorderedAS = new AttributedString(reorderedString.toString());
            for (int i = 0; i < numChars; i++) {
                aci.setIndex(charOrder[i]);
                Map attributes = aci.getAttributes();
                reorderedAS.addAttributes(attributes, i, i+1);
            }
            if (charOrder[0] != aci.getBeginIndex()) {
                aci.setIndex(charOrder[0]);
                Float x = (Float) aci.getAttribute(
                    GVTAttributedCharacterIterator.TextAttribute.X);
                Float y = (Float) aci.getAttribute(
                    GVTAttributedCharacterIterator.TextAttribute.Y);
                if (x != null && !x.isNaN()) {
                    reorderedAS.addAttribute
                        (GVTAttributedCharacterIterator.TextAttribute.X,
                         new Float(Float.NaN), charOrder[0], charOrder[0]+1);
                    reorderedAS.addAttribute
                        (GVTAttributedCharacterIterator.TextAttribute.X,
                         x, 0, 1);
                }
                if (y != null && !y.isNaN()) {
                    reorderedAS.addAttribute
                        (GVTAttributedCharacterIterator.TextAttribute.Y,
                         new Float(Float.NaN), charOrder[0], charOrder[0]+1);
                    reorderedAS.addAttribute
                        (GVTAttributedCharacterIterator.TextAttribute.Y,
                         y, 0, 1);
                }
            }
            as = reorderedAS;
        }
        aci = as.getIterator();
        int runStart = -1;
        int idx = aci.getBeginIndex();
        for (int c = aci.first();
             c != AttributedCharacterIterator.DONE;
             c = aci.next(), idx++) {
            if ((c >= arabicStart) && (c <= arabicEnd)) {
                if (runStart == -1)
                    runStart = idx;
            } else if (runStart != -1) {
                as.addAttribute(ARABIC_FORM, ARABIC_NONE, runStart, idx);
                runStart = -1;
            }
        }
        if (runStart != -1)
            as.addAttribute(ARABIC_FORM, ARABIC_NONE, runStart, idx);
        aci = as.getIterator();  
        int end   = aci.getBeginIndex();
        Integer currentForm = ARABIC_NONE;
        while (aci.setIndex(end) != AttributedCharacterIterator.DONE) {
            int start = aci.getRunStart(ARABIC_FORM);
            end       = aci.getRunLimit(ARABIC_FORM);
            char currentChar = aci.setIndex(start);
            currentForm      = (Integer)aci.getAttribute(ARABIC_FORM);
            if (currentForm == null) {
                continue;
            }
            int currentIndex = start;
            int prevCharIndex = start-1;
            while (currentIndex < end) {
                char prevChar = currentChar;
                currentChar=  aci.setIndex(currentIndex);
                while (arabicCharTransparent(currentChar) &&
                       (currentIndex < end)) {
                    currentIndex++;
                    currentChar = aci.setIndex(currentIndex);
                }
                if (currentIndex >= end) {
                    break;
                }
                Integer prevForm = currentForm;
                currentForm = ARABIC_NONE;
                if (prevCharIndex >= start) {  
                    if (arabicCharShapesRight(prevChar)
                        && arabicCharShapesLeft(currentChar)) {
                        prevForm = new Integer(prevForm.intValue()+1);
                        as.addAttribute(ARABIC_FORM, prevForm,
                                        prevCharIndex, prevCharIndex+1);
                        currentForm = ARABIC_INITIAL;
                    } else if (arabicCharShaped(currentChar)) {
                        currentForm = ARABIC_ISOLATED;
                    }
                } else if (arabicCharShaped(currentChar)) {
                    currentForm = ARABIC_ISOLATED;
                }
                if (currentForm != ARABIC_NONE)
                    as.addAttribute(ARABIC_FORM, currentForm,
                                    currentIndex, currentIndex+1);
                prevCharIndex = currentIndex;
                currentIndex++;
            }
        }
        return as;
    }
    public static boolean arabicChar(char c) {
        if (c >= arabicStart && c <= arabicEnd) {
            return true;
        }
        return false;
    }
    public static boolean containsArabic(AttributedString as) {
        return containsArabic(as.getIterator());
    }
    public static boolean containsArabic(AttributedCharacterIterator aci) {
        for (char c = aci.first();
             c != AttributedCharacterIterator.DONE;
             c = aci.next()) {
            if (arabicChar(c)) {
                return true;
            }
        }
        return false;
    }
    public static boolean arabicCharTransparent(char c) {
        int charVal = c;
        if ((charVal  < 0x064B) || (charVal > 0x06ED))
            return false;
        if ((charVal <= 0x0655)                      ||
            (charVal == 0x0670)                      ||
            (charVal >= 0x06D6 && charVal <= 0x06E4) ||
            (charVal >= 0x06E7 && charVal <= 0x06E8) ||
            (charVal >= 0x06EA)) {
            return true;
        }
        return false;
    }
    private static boolean arabicCharShapesRight(char c) {
        int charVal = c;
        if ((charVal >= 0x0622 && charVal <= 0x0625)
         || (charVal == 0x0627)
         || (charVal == 0x0629)
         || (charVal >= 0x062F && charVal <= 0x0632)
         || (charVal == 0x0648)
         || (charVal >= 0x0671 && charVal <= 0x0673)
         || (charVal >= 0x0675 && charVal <= 0x0677)
         || (charVal >= 0x0688 && charVal <= 0x0699)
         || (charVal == 0x06C0)
         || (charVal >= 0x06C2 && charVal <= 0x06CB)
         || (charVal == 0x06CD)
         || (charVal == 0x06CF)
         || (charVal >= 0x06D2 && charVal <= 0x06D3)
         || arabicCharShapesDuel(c)) {
            return true;
        }
        return false;
    }
    private static boolean arabicCharShapesDuel(char c) {
        int charVal = c;
        if ((charVal == 0x0626)
         || (charVal == 0x0628)
         || (charVal >= 0x062A && charVal <= 0x062E)
         || (charVal >= 0x0633 && charVal <= 0x063A)
         || (charVal >= 0x0641 && charVal <= 0x0647)
         || (charVal >= 0x0649 && charVal <= 0x064A)
         || (charVal >= 0x0678 && charVal <= 0x0687)
         || (charVal >= 0x069A && charVal <= 0x06BF)
         || (charVal == 0x6C1)
         || (charVal == 0x6CC)
         || (charVal == 0x6CE)
         || (charVal >= 0x06D0 && charVal <= 0x06D1)
         || (charVal >= 0x06FA && charVal <= 0x06FC)) {
            return true;
        }
        return false;
    }
    private static boolean arabicCharShapesLeft(char c) {
        return arabicCharShapesDuel(c);
    }
    private static boolean arabicCharShaped(char c) {
        return arabicCharShapesRight(c);
    }
    public static boolean hasSubstitute(char ch1, char ch2) {
        if ((ch1 < doubleCharFirst) || (ch1 > doubleCharLast)) return false;
        int [][]remaps = doubleCharRemappings[ch1-doubleCharFirst];
        if (remaps == null) return false;
        for (int i=0; i<remaps.length; i++) {
            if (remaps[i][0] == ch2)
                return true;
        }
        return false;
    }
    public static int getSubstituteChar(char ch1, char ch2, int form) {
        if (form == 0) return -1;
        if ((ch1 < doubleCharFirst) || (ch1 > doubleCharLast)) return -1;
        int [][]remaps = doubleCharRemappings[ch1-doubleCharFirst];
        if (remaps == null) return -1;
        for (int i=0; i<remaps.length; i++) {
            if (remaps[i][0] == ch2)
                return remaps[i][form];
        }
        return -1;
    }
    public static int getSubstituteChar(char ch, int form) {
        if (form == 0) return -1;
        if ((ch < singleCharFirst) || (ch > singleCharLast)) return -1;
        int[] chars = singleCharRemappings[ch-singleCharFirst];
        if (chars == null) return -1;
        return chars[form-1];
    }
    public static String createSubstituteString(AttributedCharacterIterator aci) {
        int start = aci.getBeginIndex();
        int end   = aci.getEndIndex();
        int numChar = end-start;
        StringBuffer substString = new StringBuffer(numChar);
        for (int i=start; i< end; i++) {
            char c = aci.setIndex(i);
            if (!arabicChar(c)) {
                substString.append(c);
                continue;
            }
            Integer form = (Integer)aci.getAttribute(ARABIC_FORM);
            if (charStartsLigature(c) && (i+1 < end)) {
                char nextChar = aci.setIndex(i+1);
                Integer nextForm = (Integer)aci.getAttribute(ARABIC_FORM);
                if (form != null && nextForm != null) {
                    if (form.equals(ARABIC_TERMINAL)
                        && nextForm.equals(ARABIC_INITIAL)) {
                        int substChar = ArabicTextHandler.getSubstituteChar
                            (c, nextChar,ARABIC_ISOLATED.intValue());
                        if (substChar > -1) {
                            substString.append((char)substChar);
                            i++;
                            continue;
                        }
                    } else if (form.equals(ARABIC_TERMINAL)) {
                        int substChar = ArabicTextHandler.getSubstituteChar
                            (c, nextChar,ARABIC_TERMINAL.intValue());
                        if (substChar > -1) {
                            substString.append((char)substChar);
                            i++;
                            continue;
                        }
                    } else if (form.equals(ARABIC_MEDIAL)
                               && nextForm.equals(ARABIC_MEDIAL)) {
                        int substChar = ArabicTextHandler.getSubstituteChar
                            (c, nextChar,ARABIC_MEDIAL.intValue());
                        if (substChar > -1) {
                            substString.append((char)substChar);
                            i++;
                            continue;
                        }
                    }
                }
            }
            if (form != null && form.intValue() > 0) {
                int substChar = getSubstituteChar(c, form.intValue());
                if (substChar > -1) {
                    c = (char)substChar;
                }
            }
            substString.append(c);
        }
        return substString.toString();
    }
    public static boolean charStartsLigature(char c) {
        int charVal = c;
        if (charVal == 0x064B || charVal == 0x064C || charVal == 0x064D
         || charVal == 0x064E || charVal == 0x064F || charVal == 0x0650
         || charVal == 0x0651 || charVal == 0x0652 || charVal == 0x0622
         || charVal == 0x0623 || charVal == 0x0625 || charVal == 0x0627) {
            return true;
        }
        return false;
    }
    public static int getNumChars(char c) {
        if (isLigature(c))
            return 2;
        return 1;
    }
    public static boolean isLigature(char c) {
        int charVal = c;
        if ((charVal < 0xFE70) || (charVal > 0xFEFC))
            return false;
        if ((charVal <= 0xFE72)                      ||
            (charVal == 0xFE74)                      ||
            (charVal >= 0xFE76 && charVal <= 0xFE7F) ||
            (charVal >= 0xFEF5)) {
            return true;
        }
        return false;
    }
    static int singleCharFirst=0x0621;
    static int singleCharLast =0x064A;
    static int [][] singleCharRemappings = {
        {0xFE80,     -1,     -1,     -1},  
        {0xFE81, 0xFE82,     -1,     -1},  
        {0xFE83, 0xFE84,     -1,     -1},  
        {0xFE85, 0xFE86,     -1,     -1},  
        {0xFE87, 0xFE88,     -1,     -1},  
        {0xFE89, 0xFE8A, 0xFE8B, 0xFE8C},  
        {0xFE8D, 0xFE8E,     -1,     -1},  
        {0xFE8F, 0xFE90, 0xFE91, 0xFE92},  
        {0xFE93, 0xFE94,     -1,     -1},  
        {0xFE95, 0xFE96, 0xFE97, 0xFE98},  
        {0xFE99, 0xFE9A, 0xFE9B, 0xFE9C},  
        {0xFE9D, 0xFE9E, 0xFE9F, 0xFEA0},  
        {0xFEA1, 0xFEA2, 0xFEA3, 0xFEA4},  
        {0xFEA5, 0xFEA6, 0xFEA7, 0xFEA8},  
        {0xFEA9, 0xFEAA,     -1,     -1},  
        {0xFEAB, 0xFEAC,     -1,     -1},  
        {0xFEAD, 0xFEAE,     -1,     -1},  
        {0xFEAF, 0xFEB0,     -1,     -1},  
        {0xFEB1, 0xFEB2, 0xFEB3, 0xFEB4},  
        {0xFEB5, 0xFEB6, 0xFEB7, 0xFEB8},  
        {0xFEB9, 0xFEBA, 0xFEBB, 0xFEBC},  
        {0xFEBD, 0xFEBE, 0xFEBF, 0xFEC0},  
        {0xFEC1, 0xFEC2, 0xFEC3, 0xFEC4},  
        {0xFEC5, 0xFEC6, 0xFEC7, 0xFEC8},  
        {0xFEC9, 0xFECA, 0xFECB, 0xFECC},  
        {0xFECD, 0xFECE, 0xFECF, 0xFED0},  
        null,  
        null,  
        null,  
        null,  
        null,  
        null,  
        {0xFED1, 0xFED2, 0xFED3, 0xFED4},  
        {0xFED5, 0xFED6, 0xFED7, 0xFED8},  
        {0xFED9, 0xFEDA, 0xFEDB, 0xFEDC},  
        {0xFEDD, 0xFEDE, 0xFEDF, 0xFEE0},  
        {0xFEE1, 0xFEE2, 0xFEE3, 0xFEE4},  
        {0xFEE5, 0xFEE6, 0xFEE7, 0xFEE8},  
        {0xFEE9, 0xFEEA, 0xFEEB, 0xFEEC},  
        {0xFEED, 0xFEEE,     -1,     -1},  
        {0xFEEF, 0xFEF0,     -1,     -1},  
        {0xFEF1, 0xFEF2, 0xFEF3, 0xFEF4}}; 
    static int doubleCharFirst=0x0622;
    static int doubleCharLast =0x0652;
    static int [][][] doubleCharRemappings = {
        {{0x0644,   0xFEF5, 0xFEF6,     -1,     -1}},  
        {{0x0644,   0xFEF7, 0xFEF8,     -1,     -1}},  
        null,                                          
        {{0x0644,   0xFEF9, 0xFEFA,     -1,     -1}},  
        null,                                          
        {{0x0644,   0xFEFB, 0xFEFC,     -1,     -1}},  
        null,                                          
        null,                                          
        null,                                          
        null,                                          
        null,                                          
        null,                                          
        null,                                          
        null,                                          
        null,                                          
        null,                                          
        null,                                          
        null,                                          
        null,                                          
        null,                                          
        null,                                          
        null,                                          
        null,                                          
        null,                                          
        null,                                          
        null,                                          
        null,                                          
        null,                                          
        null,                                          
        null,                                          
        null,                                          
        null,                                          
        null,                                          
        null,                                          
        null,                                          
        {{0x0020,   0xFE70,     -1,     -1,     -1},   
         {0x0640,       -1,     -1,     -1, 0xFE71}},
        {{0x0020,   0xFE72,     -1,     -1,     -1}},  
        {{0x0020,   0xFE74,     -1,     -1,     -1}},  
        {{0x0020,   0xFE76,     -1,     -1,     -1},   
         {0x0640,       -1,     -1,     -1, 0xFE77}},
        {{0x0020,   0xFE78,     -1,     -1,     -1},   
         {0x0640,       -1,     -1,     -1, 0xFE79}},
        {{0x0020,   0xFE7A,     -1,     -1,     -1},   
         {0x0640,       -1,     -1,     -1, 0xFE7B}},
        {{0x0020,   0xFE7C,     -1,     -1,     -1},   
         {0x0640,       -1,     -1,     -1, 0xFE7D}},
        {{0x0020,   0xFE7E,     -1,     -1,     -1},   
         {0x0640,       -1,     -1,     -1, 0xFE7F}}};
}
