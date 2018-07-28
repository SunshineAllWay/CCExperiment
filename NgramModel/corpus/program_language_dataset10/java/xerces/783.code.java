package jaxp;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import junit.framework.TestCase;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
public class JAXP12Tests extends TestCase implements JAXPConstants {
    protected DocumentBuilderFactory dbf;
    protected DocumentBuilder db;
    protected DocumentBuilder dbn;
    protected DocumentBuilder dbnv;
    SAXParserFactory spf;
    SAXParser spn;
    SAXParser spnv;
    public JAXP12Tests(String name) {
        super(name);
    }
    private static class MyErrorHandler implements ErrorHandler {
        public void fatalError(SAXParseException x) throws SAXException {
            x.printStackTrace();
            fail("ErrorHandler#fatalError() should not have been" +
                 " called: " +
                 x.getMessage() +
                 " [line = " + x.getLineNumber() + ", systemId = " +
                 x.getSystemId() + "]");
        }
        public void error(SAXParseException x) throws SAXException {
            x.printStackTrace();
            fail("ErrorHandler#error() should not have been called: " +
                 x.getMessage() +
                 " [line = " + x.getLineNumber() + ", systemId = " +
                 x.getSystemId() + "]");
        }
        public void warning(SAXParseException x) throws SAXException {
            x.printStackTrace();
            fail("ErrorHandler#warning() should not have been called: "
                 + x.getMessage() +
                 " [line = " + x.getLineNumber() + ", systemId = " +
                 x.getSystemId() + "]");
        }
    }
    private static class ErrorHandlerCheck extends MyErrorHandler {
        Boolean gotError = Boolean.FALSE;
        public void error(SAXParseException x) throws SAXException {
            gotError = Boolean.TRUE;
            throw x;
        }
        public Object getStatus() {
            return gotError;
        }
    };
    protected void setUp() throws Exception {
        dbf = DocumentBuilderFactory.newInstance();
        db = dbf.newDocumentBuilder();  
        dbf.setNamespaceAware(true);
        dbn = dbf.newDocumentBuilder(); 
        dbn.setErrorHandler(new MyErrorHandler());
        dbf.setValidating(true);
        dbnv = dbf.newDocumentBuilder(); 
        dbnv.setErrorHandler(new MyErrorHandler());
        spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spn = spf.newSAXParser();
        spf.setValidating(true);
        spnv = spf.newSAXParser();
    }
    public void testSaxParseXSD() throws Exception {
        spnv.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
        XMLReader xr = spnv.getXMLReader();
        xr.setErrorHandler(new MyErrorHandler());
        xr.parse(new InputData("personal-schema.xml"));
    }
    public void testSaxParseXSD2() throws Exception {
        spnv.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
        XMLReader xr = spnv.getXMLReader();
        ErrorHandlerCheck meh = new ErrorHandlerCheck();
        xr.setErrorHandler(meh);
        try {
            xr.parse(new InputData("personal-schema-err.xml"));
            fail("ErrorHandler.error() should have thrown a SAXParseException");
        } catch (SAXException x) {
            assertEquals("Should have caused validation error.",
                         Boolean.TRUE, meh.getStatus());
        }
    }
    public void testSaxParseSchemaSource() throws Exception {
        spnv.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
        spnv.setProperty(JAXP_SCHEMA_SOURCE, new InputData("personal.xsd"));
        XMLReader xr = spnv.getXMLReader();
        xr.setErrorHandler(new MyErrorHandler());
        xr.parse(new InputData("personal-schema-badhint.xml"));
        xr.parse(new InputData("personal-schema-nohint.xml"));
    }
    public void testSaxParseNoXSD() throws Exception {
        XMLReader xr = spnv.getXMLReader();
        ErrorHandlerCheck meh = new ErrorHandlerCheck();
        xr.setErrorHandler(meh);
        try {
            xr.parse(new InputData("personal-schema.xml"));
            fail("ErrorHandler.error() should have thrown a SAXParseException");
        } catch (SAXException x) {
            assertEquals("Should have caused validation error.",
                         Boolean.TRUE, meh.getStatus());
        }
    }
    public void testDomParseXSD() throws Exception {
        dbf.setNamespaceAware(true);
        dbf.setValidating(true);
        dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
        DocumentBuilder mydb = dbf.newDocumentBuilder();
        mydb.setErrorHandler(new MyErrorHandler());
        mydb.parse(new InputData("personal-schema.xml"));
    }
    public void testDomParseXSD2() throws Exception {
        dbf.setNamespaceAware(true);
        dbf.setValidating(true);
        dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
        DocumentBuilder mydb = dbf.newDocumentBuilder();
        ErrorHandlerCheck meh = new ErrorHandlerCheck();
        mydb.setErrorHandler(meh);
        try {
            mydb.parse(new InputData("personal-schema-err.xml"));
            fail("ErrorHandler.error() should have thrown a SAXParseException");
        } catch (SAXException x) {
            assertEquals("Should have caused validation error.",
                         Boolean.TRUE, meh.getStatus());
        }
    }
    public void testDomParseSchemaSource() throws Exception {
        dbf.setNamespaceAware(true);
        dbf.setValidating(true);
        dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
        dbf.setAttribute(JAXP_SCHEMA_SOURCE, new InputData("personal.xsd"));
        DocumentBuilder mydb = dbf.newDocumentBuilder();
        mydb.setErrorHandler(new MyErrorHandler());
        mydb.parse(new InputData("personal-schema-badhint.xml"));
        mydb.parse(new InputData("personal-schema-nohint.xml"));
    }
    public void testDomParseNoXSD() throws Exception {
        dbf.setNamespaceAware(true);
        dbf.setValidating(true);
        DocumentBuilder mydb = dbf.newDocumentBuilder();
        ErrorHandlerCheck meh = new ErrorHandlerCheck();
        mydb.setErrorHandler(meh);
        try {
            mydb.parse(new InputData("personal-schema.xml"));
            fail("ErrorHandler.error() should have thrown a SAXParseException");
        } catch (SAXException x) {
            assertEquals("Should have caused validation error.",
                         Boolean.TRUE, meh.getStatus());
        }
    }
}
