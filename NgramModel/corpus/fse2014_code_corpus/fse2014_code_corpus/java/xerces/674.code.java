package org.apache.xerces.xni.parser;
import java.io.IOException;
import org.apache.xerces.xni.XNIException;
public interface XMLDocumentScanner 
    extends XMLDocumentSource {
    public void setInputSource(XMLInputSource inputSource) throws IOException;
    public boolean scanDocument(boolean complete)
        throws IOException, XNIException;
} 
