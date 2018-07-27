package org.apache.tools.ant.taskdefs.optional.junit;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import junit.framework.TestCase;
import org.apache.tools.ant.util.JAXPUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
public class DOMUtilTest extends TestCase {
    public void testListChildNodes() throws SAXException, IOException {
        DocumentBuilder db = JAXPUtils.getDocumentBuilder();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("taskdefs/optional/junit/matches.xml");
        Document doc = db.parse(is);
        NodeList nl = DOMUtil.listChildNodes(doc.getDocumentElement(), new FooNodeFilter(), true);
        assertEquals("expecting 3", 3, nl.getLength());
    }
    public class FooNodeFilter implements DOMUtil.NodeFilter {
        public boolean accept(Node node) {
            if (node.getNodeName().equals("foo")) {
                return true;
            }
            return false;
        }
    }
}
