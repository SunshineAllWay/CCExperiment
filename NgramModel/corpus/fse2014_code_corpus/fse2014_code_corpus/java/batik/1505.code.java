package org.apache.batik.dom;
import org.w3c.dom.Document;
public class TextWholeTextTest extends DOM3Test {
    public boolean runImplBasic() throws Exception {
        Document doc = newSVGDoc();
        AbstractText n1 = (AbstractText) doc.createTextNode("abc");
        AbstractText n2 = (AbstractText) doc.createTextNode("def");
        AbstractText n3 = (AbstractText) doc.createCDATASection("ghi");
        doc.getDocumentElement().appendChild(n1);
        doc.getDocumentElement().appendChild(n2);
        doc.getDocumentElement().appendChild(n3);
        return n1.getWholeText().equals("abcdefghi")
                && n2.getWholeText().equals("abcdefghi")
                && n3.getWholeText().equals("abcdefghi");
    }
}
