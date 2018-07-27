package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimateElement;
public class SVGOMAnimateElement
    extends    SVGOMAnimationElement
    implements SVGAnimateElement {
    protected SVGOMAnimateElement() {
    }
    public SVGOMAnimateElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_ANIMATE_TAG;
    }
    protected Node newNode() {
        return new SVGOMAnimateElement();
    }
}
