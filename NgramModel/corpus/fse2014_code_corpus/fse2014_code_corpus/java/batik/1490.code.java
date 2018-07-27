package org.apache.batik.dom;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
public class ElementSetIdAttributeNSTest extends DOM3Test {
    public boolean runImplBasic() throws Exception {
        Document doc = newSVGDoc();
        doc.getDocumentElement().setAttributeNS(null, "blah", "abc");
        ((AbstractElement) doc.getDocumentElement()).setIdAttributeNS(null, "blah", true);
        Element e = doc.getElementById("abc");
        return doc.getDocumentElement() == e;
    }
}
