package org.apache.batik.bridge;
import java.util.List;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.w3c.dom.Element;
public class SVGFontFace extends FontFace {
    Element fontFaceElement;
    GVTFontFamily fontFamily = null;
    public SVGFontFace
        (Element fontFaceElement, List srcs,
         String familyName, float unitsPerEm, String fontWeight,
         String fontStyle, String fontVariant, String fontStretch,
         float slope, String panose1, float ascent, float descent,
         float strikethroughPosition, float strikethroughThickness,
         float underlinePosition, float underlineThickness,
         float overlinePosition, float overlineThickness) {
        super(srcs,
              familyName, unitsPerEm, fontWeight, 
              fontStyle, fontVariant, fontStretch, 
              slope, panose1, ascent, descent,
              strikethroughPosition, strikethroughThickness,
              underlinePosition, underlineThickness,
              overlinePosition, overlineThickness);
        this.fontFaceElement = fontFaceElement;
    }
    public GVTFontFamily getFontFamily(BridgeContext ctx) {
        if (fontFamily != null)
            return fontFamily;
        Element fontElt = SVGUtilities.getParentElement(fontFaceElement);
        if (fontElt.getNamespaceURI().equals(SVG_NAMESPACE_URI) &&
            fontElt.getLocalName().equals(SVG_FONT_TAG)) {
            return new SVGFontFamily(this, fontElt, ctx);
        }
        fontFamily = super.getFontFamily(ctx);
        return fontFamily;
    }
    public Element getFontFaceElement() {
        return fontFaceElement;
    }
    protected Element getBaseElement(BridgeContext ctx) {
        if (fontFaceElement != null) 
            return fontFaceElement;
        return super.getBaseElement(ctx);
    }
}
