package org.apache.batik.dom.svg12;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.SVGOMTextPositioningElement;
import org.apache.batik.util.SVG12Constants;
import org.w3c.dom.Node;
public class   SVGOMFlowSpanElement 
    extends    SVGOMTextPositioningElement {
    protected SVGOMFlowSpanElement() {
    }
    public SVGOMFlowSpanElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG12Constants.SVG_FLOW_SPAN_TAG;
    }
    protected Node newNode() {
        return new SVGOMFlowSpanElement();
    }
}
