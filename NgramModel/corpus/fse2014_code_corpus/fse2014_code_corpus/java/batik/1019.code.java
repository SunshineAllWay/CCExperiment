package org.apache.batik.gvt.font;
import org.apache.batik.util.SVGConstants;
public class GVTFontFace implements SVGConstants {
    protected String familyName;
    protected float unitsPerEm;
    protected String fontWeight;
    protected String fontStyle;
    protected String fontVariant;
    protected String fontStretch;
    protected float slope;
    protected String panose1;
    protected float ascent;
    protected float descent;
    protected float strikethroughPosition;
    protected float strikethroughThickness;
    protected float underlinePosition;
    protected float underlineThickness;
    protected float overlinePosition;
    protected float overlineThickness;
    public GVTFontFace
        (String familyName, float unitsPerEm, String fontWeight,
         String fontStyle, String fontVariant, String fontStretch,
         float slope, String panose1, float ascent, float descent,
         float strikethroughPosition, float strikethroughThickness,
         float underlinePosition,     float underlineThickness,
         float overlinePosition,      float overlineThickness) {
        this.familyName = familyName;
        this.unitsPerEm = unitsPerEm;
        this.fontWeight = fontWeight;
        this.fontStyle = fontStyle;
        this.fontVariant = fontVariant;
        this.fontStretch = fontStretch;
        this.slope = slope;
        this.panose1 = panose1;
        this.ascent = ascent;
        this.descent = descent;
        this.strikethroughPosition = strikethroughPosition;
        this.strikethroughThickness = strikethroughThickness;
        this.underlinePosition = underlinePosition;
        this.underlineThickness = underlineThickness;
        this.overlinePosition = overlinePosition;
        this.overlineThickness = overlineThickness;
    }
    public GVTFontFace(String familyName) {
        this(familyName, 1000, 
             SVG_FONT_FACE_FONT_WEIGHT_DEFAULT_VALUE,
             SVG_FONT_FACE_FONT_STYLE_DEFAULT_VALUE,
             SVG_FONT_FACE_FONT_VARIANT_DEFAULT_VALUE,
             SVG_FONT_FACE_FONT_STRETCH_DEFAULT_VALUE,
             0, SVG_FONT_FACE_PANOSE_1_DEFAULT_VALUE, 
             800, 200, 300, 50, -75, 50, 800, 50);
    }
    public String getFamilyName() {
        return familyName;
    }
    public boolean hasFamilyName(String family) {
        String ffname = familyName;
        if (ffname.length() < family.length()) {
            return false;
        }
        ffname = ffname.toLowerCase();
        int idx = ffname.indexOf(family.toLowerCase());
        if (idx == -1) {
            return false;
        }
        if (ffname.length() > family.length()) {
            boolean quote = false;
            if (idx > 0) {
                char c = ffname.charAt(idx - 1);
                switch (c) {
                default:
                    return false;
                case ' ':
                    loop: for (int i = idx - 2; i >= 0; --i) {
                        switch (ffname.charAt(i)) {
                        default:
                            return false;
                        case ' ':
                            continue;
                        case '"':
                        case '\'':
                            quote = true;
                            break loop;
                        }
                    }
                    break;
                case '"':
                case '\'':
                    quote = true;
                case ',':
                }
            }
            if (idx + family.length() < ffname.length()) {
                char c = ffname.charAt(idx + family.length());
                switch (c) {
                default:
                    return false;
                case ' ':
                    loop: for (int i = idx + family.length() + 1;
                         i < ffname.length(); i++) {
                        switch (ffname.charAt(i)) {
                        default:
                            return false;
                        case ' ':
                            continue;
                        case '"':
                        case '\'':
                            if (!quote) {
                                return false;
                            }
                            break loop;
                        }
                    }
                    break;
                case '"':
                case '\'':
                    if (!quote) {
                        return false;
                    }
                case ',':
                }
            }
        }
        return true;
    }
    public String getFontWeight() {
        return fontWeight;
    }
    public String getFontStyle() {
        return fontStyle;
    }
    public float getUnitsPerEm() {
        return unitsPerEm;
    }
    public float getAscent() {
        return ascent;
    }
    public float getDescent() {
        return descent;
    }
    public float getStrikethroughPosition() {
        return strikethroughPosition;
    }
    public float getStrikethroughThickness() {
        return strikethroughThickness;
    }
    public float getUnderlinePosition() {
        return underlinePosition;
    }
    public float getUnderlineThickness() {
        return underlineThickness;
    }
    public float getOverlinePosition() {
        return overlinePosition;
    }
    public float getOverlineThickness() {
        return overlineThickness;
    }
}
