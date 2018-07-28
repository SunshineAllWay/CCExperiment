package org.apache.batik.dom.svg;
import org.w3c.dom.Attr;
public interface LiveAttributeValue {
    void attrAdded(Attr node, String newv);
    void attrModified(Attr node, String oldv, String newv);
    void attrRemoved(Attr node, String oldv);
}
