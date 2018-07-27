package org.apache.batik.dom;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGGElement;
import org.w3c.dom.svg.SVGSVGElement;
public class DocumentRenameNodeTest extends DOM3Test {
    public boolean runImplBasic() throws Exception {
        Document doc = newSVGDoc();
        Element e2 = doc.createElementNS(SVG_NAMESPACE_URI, "g");
        boolean pass = e2 instanceof SVGGElement;
        e2 = (Element) ((AbstractDocument) doc).renameNode(e2, SVG_NAMESPACE_URI, "svg");
        pass = pass && e2 instanceof SVGSVGElement;
        Attr a = doc.createAttributeNS(null, "test");
        a = (Attr) ((AbstractDocument) doc).renameNode(a, EX_NAMESPACE_URI, "test2");
        pass = pass && a.getNamespaceURI().equals(EX_NAMESPACE_URI)
            && a.getLocalName().equals("test2");
        return pass;
    }
}
