package org.apache.xerces.xpointer;
import java.io.PrintWriter;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLParseException;
final class XPointerErrorHandler implements XMLErrorHandler {
    protected PrintWriter fOut;
    public XPointerErrorHandler() {
        this(new PrintWriter(System.err));
    } 
    public XPointerErrorHandler(PrintWriter out) {
        fOut = out;
    } 
    public void warning(String domain, String key, XMLParseException ex)
            throws XNIException {
        printError("Warning", ex);
    } 
    public void error(String domain, String key, XMLParseException ex)
            throws XNIException {
        printError("Error", ex);
    } 
    public void fatalError(String domain, String key, XMLParseException ex)
            throws XNIException {
        printError("Fatal Error", ex);
        throw ex;
    } 
    private void printError(String type, XMLParseException ex) {
        fOut.print("[");
        fOut.print(type);
        fOut.print("] ");
        String systemId = ex.getExpandedSystemId();
        if (systemId != null) {
            int index = systemId.lastIndexOf('/');
            if (index != -1)
                systemId = systemId.substring(index + 1);
            fOut.print(systemId);
        }
        fOut.print(':');
        fOut.print(ex.getLineNumber());
        fOut.print(':');
        fOut.print(ex.getColumnNumber());
        fOut.print(": ");
        fOut.print(ex.getMessage());
        fOut.println();
        fOut.flush();
    } 
} 
