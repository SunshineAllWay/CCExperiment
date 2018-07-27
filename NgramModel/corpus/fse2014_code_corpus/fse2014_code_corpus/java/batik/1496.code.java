package org.apache.batik.dom;
import org.w3c.dom.Document;
public class NodeCompareDocumentPositionTest extends DOM3Test {
    public boolean runImplBasic() throws Exception {
        Document doc = newSVGDoc();
        AbstractNode e = (AbstractNode) doc.createElementNS(null, "test");
        doc.getDocumentElement().appendChild(e);
        AbstractNode e2 = (AbstractNode) doc.createElementNS(null, "two");
        e.appendChild(e2);
        AbstractNode e3 = (AbstractNode) doc.createElementNS(null, "three");
        e.appendChild(e3);
        AbstractNode e4 = (AbstractNode) doc.createElementNS(null, "four");
        doc.getDocumentElement().appendChild(e4);
        return e.compareDocumentPosition(e2) == (AbstractNode.DOCUMENT_POSITION_CONTAINS | AbstractNode.DOCUMENT_POSITION_PRECEDING)
                && e2.compareDocumentPosition(e) == (AbstractNode.DOCUMENT_POSITION_CONTAINED_BY | AbstractNode.DOCUMENT_POSITION_FOLLOWING)
                && e.compareDocumentPosition(e) == 0
                && e2.compareDocumentPosition(e3) == AbstractNode.DOCUMENT_POSITION_PRECEDING
                && e3.compareDocumentPosition(e2) == AbstractNode.DOCUMENT_POSITION_FOLLOWING
                && e3.compareDocumentPosition(e4) == AbstractNode.DOCUMENT_POSITION_PRECEDING
                && e4.compareDocumentPosition(e3) == AbstractNode.DOCUMENT_POSITION_FOLLOWING;
    }
}
