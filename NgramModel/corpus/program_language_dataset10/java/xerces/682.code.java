package org.apache.xerces.xni.parser;
import org.apache.xerces.xni.XNIException;
public interface XMLErrorHandler {
    public void warning(String domain, String key, 
                        XMLParseException exception) throws XNIException;
    public void error(String domain, String key, 
                      XMLParseException exception) throws XNIException;
    public void fatalError(String domain, String key, 
                           XMLParseException exception) throws XNIException;
} 
