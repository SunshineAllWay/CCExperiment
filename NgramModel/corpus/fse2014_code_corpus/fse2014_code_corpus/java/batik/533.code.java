package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAltGlyphDefElement;
public class SVGOMAltGlyphDefElement
    extends    SVGOMElement
    implements SVGAltGlyphDefElement {
    protected SVGOMAltGlyphDefElement() {
    }
    public SVGOMAltGlyphDefElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_ALT_GLYPH_DEF_TAG;
    }
    protected Node newNode() {
        return new SVGOMAltGlyphDefElement();
    }
}
