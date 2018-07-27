package org.apache.xerces.dom3.as;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;
public interface DOMASBuilder extends LSParser {
    public ASModel getAbstractSchema();
    public void setAbstractSchema(ASModel abstractSchema);
    public ASModel parseASURI(String uri)
                              throws DOMASException, Exception;
    public ASModel parseASInputSource(LSInput is)
                                      throws DOMASException, Exception;
}
