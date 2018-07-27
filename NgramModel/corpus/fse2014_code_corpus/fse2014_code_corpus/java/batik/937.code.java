package org.apache.batik.extension.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.SVGOMTextPositioningElement;
import org.w3c.dom.Node;
public class FlowParaElement
    extends    SVGOMTextPositioningElement
    implements BatikExtConstants {
    protected FlowParaElement() {
    }
    public FlowParaElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return BATIK_EXT_FLOW_PARA_TAG;
    }
    public String getNamespaceURI() {
        return BATIK_12_NAMESPACE_URI;
    }
    protected Node newNode() {
        return new FlowParaElement();
    }
}
