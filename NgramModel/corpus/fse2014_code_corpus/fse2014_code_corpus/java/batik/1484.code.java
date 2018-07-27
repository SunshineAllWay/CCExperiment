package org.apache.batik.dom;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
public class DocumentAdoptNodeTest extends DOM3Test {
    public boolean runImplBasic() throws Exception {
        Document doc1 = newSVGDoc();
        Document doc2 = newSVGDoc();
        Element e = doc2.getDocumentElement();
        e.setAttributeNS(EX_NAMESPACE_URI, "test", "blah");
        ((AbstractDocument) doc1).adoptNode(e);
        return e.getOwnerDocument() == doc1
            && e.getAttributeNodeNS(EX_NAMESPACE_URI, "test").getOwnerDocument() == doc1;
    }
}
