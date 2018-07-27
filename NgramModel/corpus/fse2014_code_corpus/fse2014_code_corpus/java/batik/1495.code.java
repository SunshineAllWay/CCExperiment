package org.apache.batik.dom;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
public class NodeBaseURITest extends DOM3Test {
    public boolean runImplBasic() throws Exception {
        Document doc = newSVGDoc();
        ((AbstractDocument) doc).setDocumentURI("http://example.com/blah");
        Element e = doc.createElementNS(SVG_NAMESPACE_URI, "g");
        doc.getDocumentElement().appendChild(e);
        e.setAttributeNS(XML_NAMESPACE_URI, "xml:base", "http://example.org/base");
        Element e2 = doc.createElementNS(SVG_NAMESPACE_URI, "g");
        e.appendChild(e2);
        e2.setAttributeNS(XML_NAMESPACE_URI, "xml:base", "/somewhere");
        return "http://example.com/blah".equals(((AbstractNode) doc).getBaseURI())
                && "http://example.org/base".equals(((AbstractNode) e).getBaseURI())
                && "http://example.org/somewhere".equals(((AbstractNode) e2).getBaseURI());
    }
}
