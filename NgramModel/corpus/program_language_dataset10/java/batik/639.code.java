package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGTRefElement;
public class SVGOMTRefElement
    extends    SVGURIReferenceTextPositioningElement
    implements SVGTRefElement {
    protected SVGOMTRefElement() {
    }
    public SVGOMTRefElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_TREF_TAG;
    }
    protected Node newNode() {
        return new SVGOMTRefElement();
    }
}
