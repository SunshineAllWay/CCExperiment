package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGPolylineElement;
public class SVGOMPolylineElement
    extends    SVGPointShapeElement
    implements SVGPolylineElement {
    protected SVGOMPolylineElement() {
    }
    public SVGOMPolylineElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_POLYLINE_TAG;
    }
    protected Node newNode() {
        return new SVGOMPolylineElement();
    }
}
