package org.apache.batik.bridge;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.apache.batik.gvt.font.GVTFontFace;
import org.apache.batik.gvt.font.UnresolvedFontFamily;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.FontFaceRule;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
public abstract class SVGFontUtilities implements SVGConstants {
    public static List getFontFaces(Document doc,
                                    BridgeContext ctx) {
        Map fontFamilyMap = ctx.getFontFamilyMap();
        List ret = (List)fontFamilyMap.get(doc);
        if (ret != null)
            return ret;
        ret = new LinkedList();
        NodeList fontFaceElements = doc.getElementsByTagNameNS
            (SVG_NAMESPACE_URI, SVG_FONT_FACE_TAG);
        SVGFontFaceElementBridge fontFaceBridge;
        fontFaceBridge = (SVGFontFaceElementBridge)ctx.getBridge
            (SVG_NAMESPACE_URI, SVG_FONT_FACE_TAG);
        for (int i = 0; i < fontFaceElements.getLength(); i++) {
            Element fontFaceElement = (Element)fontFaceElements.item(i);
            ret.add(fontFaceBridge.createFontFace
                    (ctx, fontFaceElement));
        }
        CSSEngine engine = ((SVGOMDocument)doc).getCSSEngine();
        List sms = engine.getFontFaces();
        Iterator iter = sms.iterator();
        while (iter.hasNext()) {
            FontFaceRule ffr = (FontFaceRule)iter.next();
            ret.add(CSSFontFace.createCSSFontFace(engine, ffr));
        }
        return ret;
    }
    public static GVTFontFamily getFontFamily(Element textElement,
                                             BridgeContext ctx,
                                             String fontFamilyName,
                                             String fontWeight,
                                             String fontStyle) {
        String fontKeyName = fontFamilyName.toLowerCase() + " " +              
            fontWeight + " " + fontStyle;
        Map fontFamilyMap = ctx.getFontFamilyMap();
        GVTFontFamily fontFamily =
            (GVTFontFamily)fontFamilyMap.get(fontKeyName);
        if (fontFamily != null) {
            return fontFamily;
        }
        Document doc = textElement.getOwnerDocument();
        List fontFaces = (List)fontFamilyMap.get(doc);
        if (fontFaces == null) {
            fontFaces = getFontFaces(doc, ctx);
            fontFamilyMap.put(doc, fontFaces);
        }
        Iterator iter = fontFaces.iterator();
        List svgFontFamilies = new LinkedList();
        while (iter.hasNext()) {
            FontFace fontFace = (FontFace)iter.next();
            if (!fontFace.hasFamilyName(fontFamilyName)) {
                continue;
            }
            String fontFaceStyle = fontFace.getFontStyle();
            if (fontFaceStyle.equals(SVG_ALL_VALUE) ||
                fontFaceStyle.indexOf(fontStyle) != -1) {
                GVTFontFamily ffam = fontFace.getFontFamily(ctx);
                if (ffam != null)
                    svgFontFamilies.add(ffam);
            }
        }
        if (svgFontFamilies.size() == 1) {
            fontFamilyMap.put(fontKeyName, svgFontFamilies.get(0));
            return (GVTFontFamily)svgFontFamilies.get(0);
        } else if (svgFontFamilies.size() > 1) {
            String fontWeightNumber = getFontWeightNumberString(fontWeight);
            List fontFamilyWeights = new ArrayList(svgFontFamilies.size());
            Iterator ffiter = svgFontFamilies.iterator();
            while(ffiter.hasNext()) {
                GVTFontFace fontFace;
                fontFace = ((GVTFontFamily)ffiter.next()).getFontFace();
                String fontFaceWeight = fontFace.getFontWeight();
                fontFaceWeight = getFontWeightNumberString(fontFaceWeight);
                fontFamilyWeights.add(fontFaceWeight);
            }
            List newFontFamilyWeights = new ArrayList(fontFamilyWeights);
            for (int i = 100; i <= 900; i+= 100) {
                String weightString = String.valueOf(i);
                boolean matched = false;
                int minDifference = 1000;
                int minDifferenceIndex = 0;
                for (int j = 0; j < fontFamilyWeights.size(); j++) {
                    String fontFamilyWeight = (String)fontFamilyWeights.get(j);
                    if (fontFamilyWeight.indexOf(weightString) > -1) {
                        matched = true;
                        break;
                    }
                    StringTokenizer st =
                        new StringTokenizer(fontFamilyWeight, " ,");
                    while (st.hasMoreTokens()) {
                        int weightNum = Integer.parseInt(st.nextToken());
                        int difference = Math.abs(weightNum - i);
                        if (difference < minDifference) {
                            minDifference = difference;
                            minDifferenceIndex = j;
                        }
                    }
                }
                if (!matched) {
                    String newFontFamilyWeight =
                        newFontFamilyWeights.get(minDifferenceIndex) +
                        ", " + weightString;
                    newFontFamilyWeights.set(minDifferenceIndex,
                                             newFontFamilyWeight);
                }
            }
            for (int i = 0; i < svgFontFamilies.size(); i++) {
                String fontFaceWeight = (String)newFontFamilyWeights.get(i);
                if (fontFaceWeight.indexOf(fontWeightNumber) > -1) {
                    fontFamilyMap.put(fontKeyName, svgFontFamilies.get(i));
                    return (GVTFontFamily)svgFontFamilies.get(i);
                }
            }
            fontFamilyMap.put(fontKeyName, svgFontFamilies.get(0));
            return (GVTFontFamily) svgFontFamilies.get(0);
        } else {
            GVTFontFamily gvtFontFamily =
                new UnresolvedFontFamily(fontFamilyName);
            fontFamilyMap.put(fontKeyName, gvtFontFamily);
            return gvtFontFamily;
        }
    }
    protected static String getFontWeightNumberString(String fontWeight) {
        if (fontWeight.equals(SVG_NORMAL_VALUE)) {
            return SVG_400_VALUE;
        } else if (fontWeight.equals(SVG_BOLD_VALUE)) {
            return SVG_700_VALUE;
        } else if (fontWeight.equals(SVG_ALL_VALUE)) {
            return "100, 200, 300, 400, 500, 600, 700, 800, 900";
        }
        return fontWeight;
    }
}
