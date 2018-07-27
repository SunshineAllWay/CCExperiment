package org.apache.batik.dom;
import org.w3c.dom.Document;
import org.w3c.dom.Text;
public class TextReplaceWholeTextTest extends DOM3Test {
    public boolean runImplBasic() throws Exception {
        Document doc = newSVGDoc();
        Text n1 = doc.createTextNode("abc");
        Text n2 = doc.createTextNode("def");
        Text n3 = doc.createCDATASection("ghi");
        doc.getDocumentElement().appendChild(n1);
        doc.getDocumentElement().appendChild(n2);
        doc.getDocumentElement().appendChild(n3);
        ((AbstractText) n2).replaceWholeText("xyz");
        return doc.getDocumentElement().getFirstChild().getNodeValue().equals("xyz")
                && doc.getDocumentElement().getFirstChild().getNextSibling() == null;
    }
}
