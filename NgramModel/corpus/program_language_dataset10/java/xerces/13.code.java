package dom.wrappers;
import org.apache.xerces.dom.TextImpl;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import dom.ParserWrapper;
public class Xerces 
    implements ParserWrapper, ParserWrapper.DocumentInfo, ErrorHandler {
    protected DOMParser parser = new DOMParser();
    public Xerces() {
        parser.setErrorHandler(this);
    } 
    public Document parse(String uri) throws Exception {
        parser.parse(uri);
        return parser.getDocument();
    } 
    public void setFeature(String featureId, boolean state)
        throws SAXNotRecognizedException, SAXNotSupportedException {
        parser.setFeature(featureId, state);
    } 
    public ParserWrapper.DocumentInfo getDocumentInfo() {
        return this;
    } 
    public boolean isIgnorableWhitespace(Text text) {
        return ((TextImpl)text).isIgnorableWhitespace();
    }
    public void warning(SAXParseException ex) throws SAXException {
        printError("Warning", ex);
    } 
    public void error(SAXParseException ex) throws SAXException {
        printError("Error", ex);
    } 
    public void fatalError(SAXParseException ex) throws SAXException {
        printError("Fatal Error", ex);
        throw ex;
    } 
    protected void printError(String type, SAXParseException ex) {
        System.err.print("[");
        System.err.print(type);
        System.err.print("] ");
        String systemId = ex.getSystemId();
        if (systemId != null) {
            int index = systemId.lastIndexOf('/');
            if (index != -1)
                systemId = systemId.substring(index + 1);
            System.err.print(systemId);
        }
        System.err.print(':');
        System.err.print(ex.getLineNumber());
        System.err.print(':');
        System.err.print(ex.getColumnNumber());
        System.err.print(": ");
        System.err.print(ex.getMessage());
        System.err.println();
        System.err.flush();
    } 
} 
