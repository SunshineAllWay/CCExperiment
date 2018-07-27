package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGFontFaceElement;
public class SVGOMFontFaceElement
    extends    SVGOMElement
    implements SVGFontFaceElement {
    protected SVGOMFontFaceElement() {
    }
    public SVGOMFontFaceElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_FONT_FACE_TAG;
    }
    protected Node newNode() {
        return new SVGOMFontFaceElement();
    }
}
