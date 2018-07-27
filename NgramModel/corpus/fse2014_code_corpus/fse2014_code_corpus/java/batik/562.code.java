package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDefsElement;
public class SVGOMDefsElement
    extends    SVGGraphicsElement
    implements SVGDefsElement {
    protected SVGOMDefsElement() {
    }
    public SVGOMDefsElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_DEFS_TAG;
    }
    protected Node newNode() {
        return new SVGOMDefsElement();
    }
}
