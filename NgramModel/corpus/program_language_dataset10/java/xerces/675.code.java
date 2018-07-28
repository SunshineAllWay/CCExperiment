package org.apache.xerces.xni.parser;
import org.apache.xerces.xni.XMLDocumentHandler;
public interface XMLDocumentSource {
    public void setDocumentHandler(XMLDocumentHandler handler);
    public XMLDocumentHandler getDocumentHandler();
} 
