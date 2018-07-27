package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGFEFuncRElement;
public class SVGOMFEFuncRElement
    extends    SVGOMComponentTransferFunctionElement
    implements SVGFEFuncRElement {
    protected SVGOMFEFuncRElement() {
    }
    public SVGOMFEFuncRElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_FE_FUNC_R_TAG;
    }
    protected Node newNode() {
        return new SVGOMFEFuncRElement();
    }
}
