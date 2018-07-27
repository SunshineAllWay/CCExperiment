package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDescElement;
public class SVGOMDescElement
    extends    SVGDescriptiveElement
    implements SVGDescElement {
    protected SVGOMDescElement() {
    }
    public SVGOMDescElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_DESC_TAG;
    }
    protected Node newNode() {
        return new SVGOMDescElement();
    }
}
