package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGFEFuncGElement;
public class SVGOMFEFuncGElement
    extends    SVGOMComponentTransferFunctionElement
    implements SVGFEFuncGElement {
    protected SVGOMFEFuncGElement() {
    }
    public SVGOMFEFuncGElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_FE_FUNC_G_TAG;
    }
    protected Node newNode() {
        return new SVGOMFEFuncGElement();
    }
}
