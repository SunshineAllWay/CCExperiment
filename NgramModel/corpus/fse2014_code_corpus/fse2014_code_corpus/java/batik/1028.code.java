package org.apache.batik.gvt.font;
public class UnicodeRange {
    private int firstUnicodeValue;
    private int lastUnicodeValue;
    public UnicodeRange(String unicodeRange) {
        if (unicodeRange.startsWith("U+") && unicodeRange.length() > 2) {
            unicodeRange = unicodeRange.substring(2);
            int dashIndex = unicodeRange.indexOf('-');
            String firstValue;
            String lastValue;
            if (dashIndex != -1) { 
                firstValue = unicodeRange.substring(0, dashIndex);
                lastValue = unicodeRange.substring(dashIndex+1);
            } else {
                firstValue = unicodeRange;
                lastValue = unicodeRange;
                if (unicodeRange.indexOf('?') != -1) {
                    firstValue = firstValue.replace('?', '0');
                    lastValue = lastValue.replace('?', 'F');
                }
            }
            try {
                firstUnicodeValue = Integer.parseInt(firstValue, 16);
                lastUnicodeValue = Integer.parseInt(lastValue, 16);
            } catch (NumberFormatException e) {
                firstUnicodeValue = -1;
                lastUnicodeValue = -1;
            }
        } else {
            firstUnicodeValue = -1;
            lastUnicodeValue = -1;
        }
    }
    public boolean contains(String unicode) {
        if (unicode.length() == 1) {
            int unicodeVal = unicode.charAt(0);
            return contains(unicodeVal);
        }
        return false;
    }
    public boolean contains(int unicodeVal) {
        return ((unicodeVal >= firstUnicodeValue) &&
                (unicodeVal <= lastUnicodeValue));
    }
}
