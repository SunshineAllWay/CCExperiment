package org.apache.tools.ant.taskdefs.cvslib;
import junit.framework.TestCase;
import java.util.Date;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.apache.tools.ant.util.JAXPUtils;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;
public class ChangeLogWriterTest extends TestCase {
    private ChangeLogWriter writer = new ChangeLogWriter();
    public void testNonUTF8Characters() throws Exception {
        CVSEntry entry = new CVSEntry(new Date(), "Se\u00f1orita", "2003 < 2004 && 3 > 5");
        entry.addFile("Medicare & review.doc", "1.1");
        entry.addFile("El\u00e8ments de style", "1.2");
        CVSEntry[] entries = { entry };
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintWriter pwriter = new PrintWriter(new OutputStreamWriter(output, "UTF-8"));
        writer.printChangeLog(pwriter, entries);
        XMLReader xmlReader = JAXPUtils.getXMLReader();
        InputStream input = new ByteArrayInputStream(output.toByteArray());
        xmlReader.setContentHandler(new NullContentHandler());
        xmlReader.parse(new InputSource(input));
    }
    public static class NullContentHandler implements ContentHandler {
        public void endDocument() throws SAXException {
        }
        public void startDocument() throws SAXException {
        }
        public void characters(char ch[], int start, int length) throws SAXException {
            String debug = new String(ch, start, length);
        }
        public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
        }
        public void endPrefixMapping(String prefix) throws SAXException {
        }
        public void skippedEntity(String name) throws SAXException {
        }
        public void setDocumentLocator(Locator locator) {
        }
        public void processingInstruction(String target, String data) throws SAXException {
        }
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
        }
        public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        }
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        }
    }
}
