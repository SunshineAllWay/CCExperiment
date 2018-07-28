package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGFontFaceUriElement;
public class SVGOMFontFaceUriElement
    extends    SVGOMElement
    implements SVGFontFaceUriElement {
    protected SVGOMFontFaceUriElement() {
    }
    public SVGOMFontFaceUriElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_FONT_FACE_URI_TAG;
    }
    protected Node newNode() {
        return new SVGOMFontFaceUriElement();
    }
}
