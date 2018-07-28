package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGFontFaceSrcElement;
public class SVGOMFontFaceSrcElement
    extends    SVGOMElement
    implements SVGFontFaceSrcElement {
    protected SVGOMFontFaceSrcElement() {
    }
    public SVGOMFontFaceSrcElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_FONT_FACE_SRC_TAG;
    }
    protected Node newNode() {
        return new SVGOMFontFaceSrcElement();
    }
}
