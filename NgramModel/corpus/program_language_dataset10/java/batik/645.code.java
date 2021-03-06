package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGVKernElement;
public class SVGOMVKernElement
    extends    SVGOMElement
    implements SVGVKernElement {
    protected SVGOMVKernElement() {
    }
    public SVGOMVKernElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_VKERN_TAG;
    }
    protected Node newNode() {
        return new SVGOMVKernElement();
    }
}
