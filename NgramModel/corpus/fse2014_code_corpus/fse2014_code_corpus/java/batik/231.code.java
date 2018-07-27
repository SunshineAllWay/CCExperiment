package org.apache.batik.bridge;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.apache.batik.css.engine.SVGCSSEngine;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.gvt.TextNode;
import org.apache.batik.util.CSSConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSPrimitiveValue;
public abstract class TextUtilities implements CSSConstants, ErrorConstants {
    public static String getElementContent(Element e) {
        StringBuffer result = new StringBuffer();
        for (Node n = e.getFirstChild();
             n != null;
             n = n.getNextSibling()) {
            switch (n.getNodeType()) {
            case Node.ELEMENT_NODE:
                result.append(getElementContent((Element)n));
                break;
            case Node.CDATA_SECTION_NODE:
            case Node.TEXT_NODE:
                result.append(n.getNodeValue());
            }
        }
        return result.toString();
    }
    public static
        ArrayList svgHorizontalCoordinateArrayToUserSpace(Element element,
                                                          String attrName,
                                                          String valueStr,
                                                          BridgeContext ctx) {
        UnitProcessor.Context uctx = UnitProcessor.createContext(ctx, element);
        ArrayList values = new ArrayList();
        StringTokenizer st = new StringTokenizer(valueStr, ", ", false);
        while (st.hasMoreTokens()) {
            values.add
                (new Float(UnitProcessor.svgHorizontalCoordinateToUserSpace
                           (st.nextToken(), attrName, uctx)));
        }
        return values;
    }
    public static
        ArrayList svgVerticalCoordinateArrayToUserSpace(Element element,
                                                        String attrName,
                                                        String valueStr,
                                                        BridgeContext ctx) {
        UnitProcessor.Context uctx = UnitProcessor.createContext(ctx, element);
        ArrayList values = new ArrayList();
        StringTokenizer st = new StringTokenizer(valueStr, ", ", false);
        while (st.hasMoreTokens()) {
            values.add
                (new Float(UnitProcessor.svgVerticalCoordinateToUserSpace
                           (st.nextToken(), attrName, uctx)));
        }
        return values;
    }
    public static ArrayList svgRotateArrayToFloats(Element element,
                                                   String attrName,
                                                   String valueStr,
                                                   BridgeContext ctx) {
        StringTokenizer st = new StringTokenizer(valueStr, ", ", false);
        ArrayList values = new ArrayList();
        String s;
        while (st.hasMoreTokens()) {
            try {
                s = st.nextToken();
                values.add
                    (new Float(Math.toRadians
                               (SVGUtilities.convertSVGNumber(s))));
            } catch (NumberFormatException nfEx ) {
                throw new BridgeException
                    (ctx, element, nfEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                     new Object [] {attrName, valueStr});
            }
        }
        return values;
    }
    public static Float convertFontSize(Element e) {
        Value v = CSSUtilities.getComputedStyle
            (e, SVGCSSEngine.FONT_SIZE_INDEX);
        return new Float(v.getFloatValue());
    }
    public static Float convertFontStyle(Element e) {
        Value v = CSSUtilities.getComputedStyle
            (e, SVGCSSEngine.FONT_STYLE_INDEX);
        switch (v.getStringValue().charAt(0)) {
        case 'n':
            return TextAttribute.POSTURE_REGULAR;
        default:
            return TextAttribute.POSTURE_OBLIQUE;
        }
    }
    public static Float convertFontStretch(Element e) {
        Value v = CSSUtilities.getComputedStyle
            (e, SVGCSSEngine.FONT_STRETCH_INDEX);
        String s = v.getStringValue();
        switch (s.charAt(0)) {
        case 'u':
            if (s.charAt(6) == 'c') {
                return TextAttribute.WIDTH_CONDENSED;
            } else {
                return TextAttribute.WIDTH_EXTENDED;
            }
        case 'e':
            if (s.charAt(6) == 'c') {
                return TextAttribute.WIDTH_CONDENSED;
            } else {
                if (s.length() == 8) {
                    return TextAttribute.WIDTH_SEMI_EXTENDED;
                } else {
                    return TextAttribute.WIDTH_EXTENDED;
                }
            }
        case 's':
            if (s.charAt(6) == 'c') {
                return TextAttribute.WIDTH_SEMI_CONDENSED;
            } else {
                return TextAttribute.WIDTH_SEMI_EXTENDED;
            }
        default:
            return TextAttribute.WIDTH_REGULAR;
        }
    }
    public static Float convertFontWeight(Element e) {
        Value v = CSSUtilities.getComputedStyle
            (e, SVGCSSEngine.FONT_WEIGHT_INDEX);
        int weight = (int)v.getFloatValue();
        switch (weight) {
        case 100:
            return TextAttribute.WEIGHT_EXTRA_LIGHT;
        case 200:
            return TextAttribute.WEIGHT_LIGHT;
        case 300:
            return TextAttribute.WEIGHT_DEMILIGHT;
        case 400:
            return TextAttribute.WEIGHT_REGULAR;
        case 500:
            return TextAttribute.WEIGHT_SEMIBOLD;
        default:
            String javaVersionString = System.getProperty("java.specification.version");
            float javaVersion = (javaVersionString != null
                    ? Float.parseFloat(javaVersionString) : 1.5f);
            if (javaVersion < 1.5) {
                return TextAttribute.WEIGHT_BOLD;
            }
            switch (weight) {
            case 600:
                return TextAttribute.WEIGHT_MEDIUM;
            case 700:
                return TextAttribute.WEIGHT_BOLD;
            case 800:
                return TextAttribute.WEIGHT_HEAVY;
            case 900:
                return TextAttribute.WEIGHT_ULTRABOLD;
            default:
                return TextAttribute.WEIGHT_REGULAR; 
            }
        }
    }
    public static TextNode.Anchor convertTextAnchor(Element e) {
        Value v = CSSUtilities.getComputedStyle
            (e, SVGCSSEngine.TEXT_ANCHOR_INDEX);
        switch (v.getStringValue().charAt(0)) {
        case 's':
            return TextNode.Anchor.START;
        case 'm':
            return TextNode.Anchor.MIDDLE;
        default:
            return TextNode.Anchor.END;
        }
    }
    public static Object convertBaselineShift(Element e) {
        Value v = CSSUtilities.getComputedStyle
            (e, SVGCSSEngine.BASELINE_SHIFT_INDEX);
        if (v.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
            String s = v.getStringValue();
            switch (s.charAt(2)) {
            case 'p': 
                return TextAttribute.SUPERSCRIPT_SUPER;
            case 'b': 
                return TextAttribute.SUPERSCRIPT_SUB;
            default:
                return null;
            }
        } else {
            return new Float(v.getFloatValue());
        }
    }
    public static Float convertKerning(Element e) {
        Value v = CSSUtilities.getComputedStyle
            (e, SVGCSSEngine.KERNING_INDEX);
        if (v.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
            return null;
        }
        return new Float(v.getFloatValue());
    }
    public static Float convertLetterSpacing(Element e) {
        Value v = CSSUtilities.getComputedStyle
            (e, SVGCSSEngine.LETTER_SPACING_INDEX);
        if (v.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
            return null;
        }
        return new Float(v.getFloatValue());
    }
    public static Float convertWordSpacing(Element e) {
        Value v = CSSUtilities.getComputedStyle
            (e, SVGCSSEngine.WORD_SPACING_INDEX);
        if (v.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
            return null;
        }
        return new Float(v.getFloatValue());
    }
}
