package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDefinitionSrcElement;
public class SVGOMDefinitionSrcElement
    extends    SVGOMElement
    implements SVGDefinitionSrcElement {
    protected SVGOMDefinitionSrcElement() {
    }
    public SVGOMDefinitionSrcElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_DEFINITION_SRC_TAG;
    }
    protected Node newNode() {
        return new SVGOMDefinitionSrcElement();
    }
}
