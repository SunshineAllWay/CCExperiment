package org.apache.xerces.xni.parser;
import org.apache.xerces.xni.XMLDTDContentModelHandler;
public interface XMLDTDContentModelSource {
    public void setDTDContentModelHandler(XMLDTDContentModelHandler handler);
    public XMLDTDContentModelHandler getDTDContentModelHandler( );
} 
