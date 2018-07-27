package org.apache.batik.dom;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
public class AttrIsIdTest extends DOM3Test {
    public boolean runImplBasic() throws Exception {
        Document doc = newSVGDoc();
        Element g = doc.createElementNS(SVG_NAMESPACE_URI, "g");
        g.setAttributeNS(null, "id", "n1");
        doc.getDocumentElement().appendChild(g);
        Attr a = g.getAttributeNodeNS(null, "id");
        return ((AbstractAttr) a).isId();
    }
}
