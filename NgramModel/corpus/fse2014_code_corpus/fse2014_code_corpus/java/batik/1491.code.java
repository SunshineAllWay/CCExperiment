package org.apache.batik.dom;
import java.io.StringReader;
import org.apache.batik.dom.AbstractElement;
import org.apache.batik.dom.util.SAXDocumentFactory;
import org.apache.batik.test.AbstractTest;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.ElementTraversal;
public class ElementTraversalTest extends AbstractTest {
    private String DOC = "<a><b/><c>.<?x?>.</c><d>.<?x?><e/><f/><?x?>.</d><g><h/>.<i/></g></a>";
    public boolean runImplBasic() throws Exception {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXDocumentFactory df = new SAXDocumentFactory(GenericDOMImplementation.getDOMImplementation(), parser);
        Document doc = df.createDocument("http://example.org/", new StringReader(DOC));
        AbstractElement a = (AbstractElement) doc.getDocumentElement();
        AbstractElement b = (AbstractElement) a.getFirstChild();
        AbstractElement c = (AbstractElement) b.getNextSibling();
        AbstractElement d = (AbstractElement) c.getNextSibling();
        AbstractElement g = (AbstractElement) d.getNextSibling();
        ensure(1, b.getFirstElementChild() == null);
        ensure(2, c.getFirstElementChild() == null);
        AbstractElement e = (AbstractElement) d.getFirstElementChild();
        ensure(3, e != null && e.getNodeName().equals("e"));
        AbstractElement h = (AbstractElement) g.getFirstElementChild();
        ensure(4, h != null && h.getNodeName().equals("h"));
        ensure(5, b.getLastElementChild() == null);
        ensure(6, c.getLastElementChild() == null);
        AbstractElement f = (AbstractElement) d.getLastElementChild();
        ensure(7, f != null && f.getNodeName().equals("f"));
        AbstractElement i = (AbstractElement) g.getLastElementChild();
        ensure(8, i != null && i.getNodeName().equals("i"));
        ensure(9, a.getNextElementSibling() == null);
        ensure(10, f.getNextElementSibling() == null);
        ensure(11, h.getNextElementSibling() == i);
        ensure(12, e.getNextElementSibling() == f);
        ensure(13, a.getPreviousElementSibling() == null);
        ensure(14, e.getPreviousElementSibling() == null);
        ensure(15, i.getPreviousElementSibling() == h);
        ensure(16, f.getPreviousElementSibling() == e);
        ensure(17, a.getChildElementCount() == 4);
        ensure(18, b.getChildElementCount() == 0);
        ensure(19, c.getChildElementCount() == 0);
        ensure(20, d.getChildElementCount() == 2);
        return true;
    }
    protected void ensure(int subTestNumber, boolean b) {
        if (!b) {
            throw new RuntimeException("Assertion failure in sub-test " + subTestNumber);
        }
    }
}
