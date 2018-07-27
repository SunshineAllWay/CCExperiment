package org.apache.batik.dom.svg12;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.SVGGraphicsElement;
import org.apache.batik.util.SVG12Constants;
import org.w3c.dom.Node;
public class SVGOMFlowRootElement extends SVGGraphicsElement {
    protected SVGOMFlowRootElement() {
    }
    public SVGOMFlowRootElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG12Constants.SVG_FLOW_ROOT_TAG;
    }
    protected Node newNode() {
        return new SVGOMFlowRootElement();
    }
}
