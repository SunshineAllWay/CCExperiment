package org.apache.batik.dom.svg12;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.SVGOMElement;
import org.apache.batik.util.SVG12Constants;
import org.w3c.dom.Node;
public class SVGOMHandlerElement extends SVGOMElement {
    protected SVGOMHandlerElement() {
    }
    public SVGOMHandlerElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG12Constants.SVG_HANDLER_TAG;
    }
    protected Node newNode() {
        return new SVGOMHandlerElement();
    }
}
