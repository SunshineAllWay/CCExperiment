package org.apache.batik.bridge;
import java.util.List;
import java.util.LinkedList;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.FontFaceRule;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.SVGCSSEngine;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSPrimitiveValue;
public class CSSFontFace extends FontFace implements SVGConstants {
    GVTFontFamily fontFamily = null;
    public CSSFontFace
        (List srcs,
         String familyName, float unitsPerEm, String fontWeight,
         String fontStyle, String fontVariant, String fontStretch,
         float slope, String panose1, float ascent, float descent,
         float strikethroughPosition, float strikethroughThickness,
         float underlinePosition, float underlineThickness,
         float overlinePosition, float overlineThickness) {
        super(srcs,
              familyName, unitsPerEm, fontWeight, fontStyle,
              fontVariant, fontStretch, slope, panose1, ascent, descent,
              strikethroughPosition, strikethroughThickness,
              underlinePosition, underlineThickness,
              overlinePosition, overlineThickness);
    }
    protected CSSFontFace(String familyName) {
        super(familyName);
    }
    public static CSSFontFace createCSSFontFace(CSSEngine eng,
                                                FontFaceRule ffr) {
        StyleMap sm = ffr.getStyleMap();
        String familyName = getStringProp
            (sm, eng, SVGCSSEngine.FONT_FAMILY_INDEX);
        CSSFontFace ret = new CSSFontFace(familyName);
        Value v;
        v = sm.getValue(SVGCSSEngine.FONT_WEIGHT_INDEX);
        if (v != null) 
            ret.fontWeight = v.getCssText();
        v = sm.getValue(SVGCSSEngine.FONT_STYLE_INDEX);
        if (v != null) 
            ret.fontStyle = v.getCssText();
        v = sm.getValue(SVGCSSEngine.FONT_VARIANT_INDEX);
        if (v != null) 
            ret.fontVariant = v.getCssText();
        v = sm.getValue(SVGCSSEngine.FONT_STRETCH_INDEX);
        if (v != null) 
            ret.fontStretch = v.getCssText();
        v = sm.getValue(SVGCSSEngine.SRC_INDEX);
        ParsedURL base = ffr.getURL();
        if ((v != null) && (v != ValueConstants.NONE_VALUE)) {
            if (v.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
                ret.srcs = new LinkedList();
                ret.srcs.add(getSrcValue(v, base));
            } else if (v.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
                ret.srcs = new LinkedList();
                for (int i=0; i<v.getLength(); i++) {
                    ret.srcs.add(getSrcValue(v.item(i), base));
                }
            }
        }
        return ret;
    }
    public static Object getSrcValue(Value v, ParsedURL base) {
        if (v.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE) 
            return null;
        if (v.getPrimitiveType() == CSSPrimitiveValue.CSS_URI) {
            if (base != null)
                return new ParsedURL(base, v.getStringValue());
            return new ParsedURL(v.getStringValue());
        } 
        if (v.getPrimitiveType() == CSSPrimitiveValue.CSS_STRING)
            return v.getStringValue();
        return null;
    }
    public static String getStringProp(StyleMap sm, CSSEngine eng, int pidx) {
        Value v = sm.getValue(pidx);
        ValueManager [] vms = eng.getValueManagers();
        if (v == null) {
            ValueManager vm = vms[pidx];
            v = vm.getDefaultValue();
        }
        while (v.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
            v = v.item(0);
        }
        return v.getStringValue();
    }
    public static float getFloatProp(StyleMap sm, CSSEngine eng, int pidx) {
        Value v = sm.getValue(pidx);
        ValueManager [] vms = eng.getValueManagers();
        if (v == null) {
            ValueManager vm = vms[pidx];
            v = vm.getDefaultValue();
        }
        while (v.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
            v = v.item(0);
        }
        return v.getFloatValue();
    }
    public GVTFontFamily getFontFamily(BridgeContext ctx) {
        if (fontFamily != null)
            return fontFamily ;
        fontFamily = super.getFontFamily(ctx);
        return fontFamily;
    }
}
