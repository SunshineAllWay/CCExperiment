package org.apache.xerces.util;
import java.io.PrintWriter;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLParseException;
public class DefaultErrorHandler
    implements XMLErrorHandler {
    protected PrintWriter fOut;
    public DefaultErrorHandler() {
        this(new PrintWriter(System.err));
    } 
    public DefaultErrorHandler(PrintWriter out) {
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
