package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGMissingGlyphElement;
public class SVGOMMissingGlyphElement
    extends    SVGStylableElement
    implements SVGMissingGlyphElement {
    protected SVGOMMissingGlyphElement() {
    }
    public SVGOMMissingGlyphElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_MISSING_GLYPH_TAG;
    }
    protected Node newNode() {
        return new SVGOMMissingGlyphElement();
    }
}
