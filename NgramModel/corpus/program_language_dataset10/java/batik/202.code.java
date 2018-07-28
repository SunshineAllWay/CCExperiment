package org.apache.batik.bridge;
import java.lang.ref.SoftReference;
import java.text.AttributedCharacterIterator;
import java.util.Map;
import org.apache.batik.gvt.font.GVTFont;
import org.apache.batik.gvt.font.GVTFontFace;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
public class SVGFontFamily implements GVTFontFamily {
    public static final
        AttributedCharacterIterator.Attribute TEXT_COMPOUND_ID =
        GVTAttributedCharacterIterator.TextAttribute.TEXT_COMPOUND_ID;
    protected GVTFontFace fontFace;
    protected Element fontElement;
    protected BridgeContext ctx;
    protected Boolean complex = null;
    public SVGFontFamily(GVTFontFace fontFace,
                         Element fontElement,
                         BridgeContext ctx) {
        this.fontFace = fontFace;
        this.fontElement = fontElement;
        this.ctx = ctx;
    }
    public String getFamilyName() {
        return fontFace.getFamilyName();
    }
    public GVTFontFace getFontFace() {
        return fontFace;
    }
    public GVTFont deriveFont(float size, AttributedCharacterIterator aci) {
        return deriveFont(size, aci.getAttributes());
    }
    public GVTFont deriveFont(float size, Map attrs) {
        SVGFontElementBridge fontBridge;
        fontBridge = (SVGFontElementBridge)ctx.getBridge(fontElement);
        SoftReference sr = (SoftReference)attrs.get(TEXT_COMPOUND_ID);
        Element textElement = (Element)sr.get();
        return fontBridge.createFont(ctx, fontElement, textElement,
                                     size, fontFace);
    }
    public boolean isComplex() {
        if (complex != null) return complex.booleanValue();
        boolean ret = isComplex(fontElement, ctx);
        complex = ret ? Boolean.TRUE : Boolean.FALSE;
        return ret;
    }
    public static boolean isComplex(Element fontElement, BridgeContext ctx) {
        NodeList glyphElements = fontElement.getElementsByTagNameNS
            (SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_GLYPH_TAG);
        int numGlyphs = glyphElements.getLength();
        for (int i = 0; i < numGlyphs; i++) {
            Element glyph = (Element)glyphElements.item(i);
            Node child    = glyph.getFirstChild();
            for (;child != null; child = child.getNextSibling()) {
                if (child.getNodeType() != Node.ELEMENT_NODE)
                    continue;
                Element e = (Element)child;
                Bridge b = ctx.getBridge(e);
                if ((b != null) && (b instanceof GraphicsNodeBridge)) {
                    return true;
                }
            }
        }
        return false;
    }
}
