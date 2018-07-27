package org.apache.batik.dom;
import org.apache.batik.test.AbstractTest;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.w3c.dom.Document;
public class DOM3Test extends AbstractTest {
    static String SVG_NAMESPACE_URI = "http://www.w3.org/2000/svg";
    static String EX_NAMESPACE_URI = "http://www.example.org/";
    static String XML_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";
    static String XMLNS_NAMESPACE_URI = "http://www.w3.org/2000/xmlns/";
    static String XML_EVENTS_NAMESPACE_URI = "http://www.w3.org/2001/xml-events";
    protected Document newDoc() {
        return new GenericDocument(null, GenericDOMImplementation.getDOMImplementation());
    }
    protected Document newSVGDoc() {
        Document doc = new SVGOMDocument(null, SVGDOMImplementation.getDOMImplementation());
        doc.appendChild(doc.createElementNS(SVG_NAMESPACE_URI, "svg"));
        return doc;
    }
}
