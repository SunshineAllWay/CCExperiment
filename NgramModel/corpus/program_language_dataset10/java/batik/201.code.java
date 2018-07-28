package org.apache.batik.bridge;
import java.util.List;
import java.util.LinkedList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.util.ParsedURL;
public class SVGFontFaceElementBridge extends AbstractSVGBridge
                                      implements ErrorConstants {
    public SVGFontFaceElementBridge() {
    }
    public String getLocalName() {
        return SVG_FONT_FACE_TAG;
    }
    public SVGFontFace createFontFace(BridgeContext ctx,
                                      Element fontFaceElement) {
        String familyNames = fontFaceElement.getAttributeNS
            (null, SVG_FONT_FAMILY_ATTRIBUTE);
        String unitsPerEmStr = fontFaceElement.getAttributeNS
            (null, SVG_UNITS_PER_EM_ATTRIBUTE);
        if (unitsPerEmStr.length() == 0) {
            unitsPerEmStr = SVG_FONT_FACE_UNITS_PER_EM_DEFAULT_VALUE;
        }
        float unitsPerEm;
        try {
            unitsPerEm = SVGUtilities.convertSVGNumber(unitsPerEmStr);
        } catch (NumberFormatException nfEx ) {
            throw new BridgeException
                (ctx, fontFaceElement, nfEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                new Object [] {SVG_UNITS_PER_EM_ATTRIBUTE, unitsPerEmStr});
        }
        String fontWeight = fontFaceElement.getAttributeNS
            (null, SVG_FONT_WEIGHT_ATTRIBUTE);
        if (fontWeight.length() == 0) {
            fontWeight = SVG_FONT_FACE_FONT_WEIGHT_DEFAULT_VALUE;
        }
        String fontStyle = fontFaceElement.getAttributeNS
            (null, SVG_FONT_STYLE_ATTRIBUTE);
        if (fontStyle.length() == 0) {
            fontStyle = SVG_FONT_FACE_FONT_STYLE_DEFAULT_VALUE;
        }
        String fontVariant = fontFaceElement.getAttributeNS
            (null, SVG_FONT_VARIANT_ATTRIBUTE);
         if (fontVariant.length() == 0) {
            fontVariant = SVG_FONT_FACE_FONT_VARIANT_DEFAULT_VALUE;
        }
        String fontStretch = fontFaceElement.getAttributeNS
            (null, SVG_FONT_STRETCH_ATTRIBUTE);
         if (fontStretch.length() == 0) {
            fontStretch = SVG_FONT_FACE_FONT_STRETCH_DEFAULT_VALUE;
        }
        String slopeStr = fontFaceElement.getAttributeNS
            (null, SVG_SLOPE_ATTRIBUTE);
        if (slopeStr.length() == 0) {
            slopeStr = SVG_FONT_FACE_SLOPE_DEFAULT_VALUE;
        }
        float slope;
        try {
            slope = SVGUtilities.convertSVGNumber(slopeStr);
        } catch (NumberFormatException nfEx ) {
            throw new BridgeException
                (ctx, fontFaceElement, nfEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                new Object [] {SVG_FONT_FACE_SLOPE_DEFAULT_VALUE, slopeStr});
        }
        String panose1 = fontFaceElement.getAttributeNS
            (null, SVG_PANOSE_1_ATTRIBUTE);
         if (panose1.length() == 0) {
            panose1 = SVG_FONT_FACE_PANOSE_1_DEFAULT_VALUE;
        }
        String ascentStr = fontFaceElement.getAttributeNS
            (null, SVG_ASCENT_ATTRIBUTE);
        if (ascentStr.length() == 0) {
            ascentStr = String.valueOf( unitsPerEm * 0.8);
        }
        float ascent;
        try {
           ascent = SVGUtilities.convertSVGNumber(ascentStr);
        } catch (NumberFormatException nfEx ) {
            throw new BridgeException
                (ctx, fontFaceElement, nfEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                 new Object [] {SVG_FONT_FACE_SLOPE_DEFAULT_VALUE, ascentStr});
        }
        String descentStr = fontFaceElement.getAttributeNS
            (null, SVG_DESCENT_ATTRIBUTE);
        if (descentStr.length() == 0) {
            descentStr = String.valueOf(unitsPerEm*0.2);
        }
        float descent;
        try {
            descent = SVGUtilities.convertSVGNumber(descentStr);
        } catch (NumberFormatException nfEx ) {
            throw new BridgeException
                (ctx, fontFaceElement, nfEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                 new Object [] {SVG_FONT_FACE_SLOPE_DEFAULT_VALUE, descentStr });
        }
        String underlinePosStr = fontFaceElement.getAttributeNS
            (null, SVG_UNDERLINE_POSITION_ATTRIBUTE);
        if (underlinePosStr.length() == 0) {
            underlinePosStr = String.valueOf(-3*unitsPerEm/40);
        }
        float underlinePos;
        try {
            underlinePos = SVGUtilities.convertSVGNumber(underlinePosStr);
        } catch (NumberFormatException nfEx ) {
            throw new BridgeException
                (ctx, fontFaceElement, nfEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                new Object [] {SVG_FONT_FACE_SLOPE_DEFAULT_VALUE,
                               underlinePosStr});
        }
        String underlineThicknessStr = fontFaceElement.getAttributeNS
            (null, SVG_UNDERLINE_THICKNESS_ATTRIBUTE);
        if (underlineThicknessStr.length() == 0) {
            underlineThicknessStr = String.valueOf(unitsPerEm/20);
        }
        float underlineThickness;
        try {
            underlineThickness =
                SVGUtilities.convertSVGNumber(underlineThicknessStr);
        } catch (NumberFormatException nfEx ) {
            throw new BridgeException
                (ctx, fontFaceElement, nfEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                new Object [] {SVG_FONT_FACE_SLOPE_DEFAULT_VALUE,
                               underlineThicknessStr});
        }
        String strikethroughPosStr = fontFaceElement.getAttributeNS
            (null, SVG_STRIKETHROUGH_POSITION_ATTRIBUTE);
        if (strikethroughPosStr.length() == 0) {
            strikethroughPosStr = String.valueOf(3*ascent/8);
        }
        float strikethroughPos;
        try {
            strikethroughPos =
                SVGUtilities.convertSVGNumber(strikethroughPosStr);
        } catch (NumberFormatException nfEx ) {
            throw new BridgeException
                (ctx, fontFaceElement, nfEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                new Object [] {SVG_FONT_FACE_SLOPE_DEFAULT_VALUE,
                               strikethroughPosStr});
        }
        String strikethroughThicknessStr = fontFaceElement.getAttributeNS
            (null, SVG_STRIKETHROUGH_THICKNESS_ATTRIBUTE);
        if (strikethroughThicknessStr.length() == 0) {
            strikethroughThicknessStr = String.valueOf(unitsPerEm/20);
        }
        float strikethroughThickness;
        try {
            strikethroughThickness =
                SVGUtilities.convertSVGNumber(strikethroughThicknessStr);
        } catch (NumberFormatException nfEx ) {
            throw new BridgeException
                (ctx, fontFaceElement, nfEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                new Object [] {SVG_FONT_FACE_SLOPE_DEFAULT_VALUE,
                               strikethroughThicknessStr});
        }
        String overlinePosStr = fontFaceElement.getAttributeNS
            (null, SVG_OVERLINE_POSITION_ATTRIBUTE);
         if (overlinePosStr.length() == 0) {
            overlinePosStr = String.valueOf(ascent);
        }
        float overlinePos;
        try {
            overlinePos = SVGUtilities.convertSVGNumber(overlinePosStr);
        } catch (NumberFormatException nfEx ) {
            throw new BridgeException
                (ctx, fontFaceElement, nfEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                new Object [] {SVG_FONT_FACE_SLOPE_DEFAULT_VALUE,
                               overlinePosStr});
        }
        String overlineThicknessStr = fontFaceElement.getAttributeNS
            (null, SVG_OVERLINE_THICKNESS_ATTRIBUTE);
        if (overlineThicknessStr.length() == 0) {
            overlineThicknessStr = String.valueOf(unitsPerEm/20);
        }
        float overlineThickness;
        try {
            overlineThickness =
                SVGUtilities.convertSVGNumber(overlineThicknessStr);
        } catch (NumberFormatException nfEx ) {
            throw new BridgeException
                (ctx, fontFaceElement, nfEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                new Object [] {SVG_FONT_FACE_SLOPE_DEFAULT_VALUE,
                               overlineThicknessStr});
        }
        List srcs = null;
        Element fontElt = SVGUtilities.getParentElement(fontFaceElement);
        if (!fontElt.getNamespaceURI().equals(SVG_NAMESPACE_URI) ||
            !fontElt.getLocalName().equals(SVG_FONT_TAG)) {
            srcs = getFontFaceSrcs(fontFaceElement);
        }
        return new SVGFontFace(fontFaceElement, srcs,
                               familyNames, unitsPerEm, fontWeight, fontStyle,
                               fontVariant, fontStretch, slope, panose1,
                               ascent, descent, strikethroughPos,
                               strikethroughThickness, underlinePos,
                               underlineThickness, overlinePos,
                               overlineThickness);
    }
    public List getFontFaceSrcs(Element fontFaceElement) {
        Element ffsrc = null;
        for (Node n = fontFaceElement.getFirstChild();
             n != null;
             n = n.getNextSibling()) {
            if ((n.getNodeType() == Node.ELEMENT_NODE) &&
                n.getNamespaceURI().equals(SVG_NAMESPACE_URI) &&
                n.getLocalName().equals(SVG_FONT_FACE_SRC_TAG)) {
                    ffsrc = (Element)n;
                    break;
            }
        }
        if (ffsrc == null)
            return null;
        List ret = new LinkedList();
        for (Node n = ffsrc.getFirstChild();
             n != null;
             n = n.getNextSibling()) {
            if ((n.getNodeType() != Node.ELEMENT_NODE) ||
                !n.getNamespaceURI().equals(SVG_NAMESPACE_URI))
                continue;
            if (n.getLocalName().equals(SVG_FONT_FACE_URI_TAG)) {
                Element ffuri = (Element)n;
                String uri = XLinkSupport.getXLinkHref(ffuri);
                String base = AbstractNode.getBaseURI(ffuri);
                ParsedURL purl;
                if (base != null) purl = new ParsedURL(base, uri);
                else              purl = new ParsedURL(uri);
                ret.add(purl);                                      
                continue;
            }
            if (n.getLocalName().equals(SVG_FONT_FACE_NAME_TAG)) {
                Element ffname = (Element)n;
                String s = ffname.getAttribute("name");
                if (s.length() != 0)
                    ret.add(s);                                     
            }
        }
        return ret;
    }
}
