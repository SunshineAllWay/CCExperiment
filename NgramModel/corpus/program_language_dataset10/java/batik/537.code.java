package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimateColorElement;
public class SVGOMAnimateColorElement
    extends    SVGOMAnimationElement
    implements SVGAnimateColorElement {
    protected SVGOMAnimateColorElement() {
    }
    public SVGOMAnimateColorElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_ANIMATE_COLOR_TAG;
    }
    protected Node newNode() {
        return new SVGOMAnimateColorElement();
    }
}
