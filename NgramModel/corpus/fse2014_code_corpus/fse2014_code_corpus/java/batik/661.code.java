package org.apache.batik.dom.svg12;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.AttributeInitializer;
import org.apache.batik.dom.svg.SVGGraphicsElement;
import org.w3c.dom.Node;
public class BindableElement extends SVGGraphicsElement {
    protected String namespaceURI;
    protected String localName;
    protected XBLOMShadowTreeElement xblShadowTree;
    protected BindableElement() {
    }
    public BindableElement(String prefix,
                           AbstractDocument owner,
                           String ns,
                           String ln) {
        super(prefix, owner);
        namespaceURI = ns;
        localName = ln;
    }
    public String getNamespaceURI() {
        return namespaceURI;
    }
    public String getLocalName() {
        return localName;
    }
    protected AttributeInitializer getAttributeInitializer() {
        return null;
    }
    protected Node newNode() {
        return new BindableElement(null, null, namespaceURI, localName);
    }
    public void setShadowTree(XBLOMShadowTreeElement s) {
        xblShadowTree = s;
    }
    public XBLOMShadowTreeElement getShadowTree() {
        return xblShadowTree;
    }
    public Node getCSSFirstChild() {
        if (xblShadowTree != null) {
            return xblShadowTree.getFirstChild();
        }
        return null;
    }
    public Node getCSSLastChild() {
        return getCSSFirstChild();
    }
}
