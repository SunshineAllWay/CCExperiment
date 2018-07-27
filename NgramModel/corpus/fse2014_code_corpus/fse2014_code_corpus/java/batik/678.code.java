package org.apache.batik.dom.svg12;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.SVGStylableElement;
import org.apache.batik.util.SVG12Constants;
import org.w3c.dom.Node;
public class SVGOMSubImageRefElement extends SVGStylableElement {
    protected SVGOMSubImageRefElement() {
    }
    public SVGOMSubImageRefElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG12Constants.SVG_SUB_IMAGE_REF_TAG;
    }
    protected Node newNode() {
        return new SVGOMSubImageRefElement();
    }
}
