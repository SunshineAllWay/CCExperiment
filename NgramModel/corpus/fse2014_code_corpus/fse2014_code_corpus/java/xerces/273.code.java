package org.apache.xerces.dom3.as;
import org.w3c.dom.ls.LSSerializer;
public interface DOMASWriter extends LSSerializer {
    public void writeASModel(java.io.OutputStream destination, 
                             ASModel model)
                             throws Exception;
}
