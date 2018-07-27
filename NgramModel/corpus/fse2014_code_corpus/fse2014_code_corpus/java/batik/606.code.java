package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGHKernElement;
public class SVGOMHKernElement
    extends    SVGOMElement
    implements SVGHKernElement {
    protected SVGOMHKernElement() {
    }
    public SVGOMHKernElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_HKERN_TAG;
    }
    protected Node newNode() {
        return new SVGOMHKernElement();
    }
}
