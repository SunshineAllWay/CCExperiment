package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGGlyphElement;
public class SVGOMGlyphElement
    extends    SVGStylableElement
    implements SVGGlyphElement {
    protected SVGOMGlyphElement() {
    }
    public SVGOMGlyphElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_GLYPH_TAG;
    }
    protected Node newNode() {
        return new SVGOMGlyphElement();
    }
}
