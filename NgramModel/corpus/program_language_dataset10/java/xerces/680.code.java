package org.apache.xerces.xni.parser;
import org.apache.xerces.xni.XMLDTDHandler;
public interface XMLDTDSource {
    public void setDTDHandler(XMLDTDHandler handler);
    public XMLDTDHandler getDTDHandler();
} 
