package org.apache.batik.dom.svg12;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.SVGStylableElement;
import org.apache.batik.util.SVG12Constants;
import org.w3c.dom.Node;
public class SVGOMFlowRegionElement extends SVGStylableElement {
    protected SVGOMFlowRegionElement() {
    }
    public SVGOMFlowRegionElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG12Constants.SVG_FLOW_REGION_TAG;
    }
    protected Node newNode() {
        return new SVGOMFlowRegionElement();
    }
}
