package org.apache.batik.bridge;
import org.apache.batik.gvt.text.ArabicTextHandler;
import org.apache.batik.gvt.font.GVTFontFace;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
public class SVGFontElementBridge extends AbstractSVGBridge {
    public SVGFontElementBridge() {
    }
    public String getLocalName() {
        return SVG_FONT_TAG;
    }
    public SVGGVTFont createFont(BridgeContext ctx,
                                 Element fontElement,
                                 Element textElement,
                                 float size,
                                 GVTFontFace fontFace) {
        NodeList glyphElements = fontElement.getElementsByTagNameNS
            (SVG_NAMESPACE_URI, SVG_GLYPH_TAG);
        int numGlyphs = glyphElements.getLength();
        String[] glyphCodes = new String[numGlyphs];
        String[] glyphNames = new String[numGlyphs];
        String[] glyphLangs = new String[numGlyphs];
        String[] glyphOrientations = new String[numGlyphs];
        String[] glyphForms = new String[numGlyphs];
        Element[] glyphElementArray = new Element[numGlyphs];
        for (int i = 0; i < numGlyphs; i++) {
            Element glyphElement = (Element)glyphElements.item(i);
            glyphCodes[i] = glyphElement.getAttributeNS(null, SVG_UNICODE_ATTRIBUTE);
            if (glyphCodes[i].length() > 1) {
                if (ArabicTextHandler.arabicChar(glyphCodes[i].charAt(0))) {
                    glyphCodes[i] = (new StringBuffer(glyphCodes[i])).reverse().toString();
                }
            }
            glyphNames[i] = glyphElement.getAttributeNS(null, SVG_GLYPH_NAME_ATTRIBUTE);
            glyphLangs[i] = glyphElement.getAttributeNS(null, SVG_LANG_ATTRIBUTE);
            glyphOrientations[i] = glyphElement.getAttributeNS(null, SVG_ORIENTATION_ATTRIBUTE);
            glyphForms[i] = glyphElement.getAttributeNS(null, SVG_ARABIC_FORM_ATTRIBUTE);
            glyphElementArray[i] = glyphElement;
        }
        NodeList missingGlyphElements = fontElement.getElementsByTagNameNS
            (SVG_NAMESPACE_URI, SVG_MISSING_GLYPH_TAG);
        Element missingGlyphElement = null;
        if (missingGlyphElements.getLength() > 0) {
            missingGlyphElement = (Element)missingGlyphElements.item(0);
        }
        NodeList hkernElements = fontElement.getElementsByTagNameNS
            (SVG_NAMESPACE_URI, SVG_HKERN_TAG);
        Element[] hkernElementArray = new Element[hkernElements.getLength()];
        for (int i = 0; i < hkernElementArray.length; i++) {
            Element hkernElement = (Element)hkernElements.item(i);
            hkernElementArray[i] = hkernElement;
        }
        NodeList vkernElements = fontElement.getElementsByTagNameNS
            (SVG_NAMESPACE_URI, SVG_VKERN_TAG);
        Element[] vkernElementArray = new Element[vkernElements.getLength()];
        for (int i = 0; i < vkernElementArray.length; i++) {
            Element vkernElement = (Element)vkernElements.item(i);
            vkernElementArray[i] = vkernElement;
        }
        return new SVGGVTFont
            (size, fontFace, glyphCodes, glyphNames, glyphLangs,
             glyphOrientations, glyphForms, ctx,
             glyphElementArray, missingGlyphElement,
             hkernElementArray, vkernElementArray, textElement);
    }
}
