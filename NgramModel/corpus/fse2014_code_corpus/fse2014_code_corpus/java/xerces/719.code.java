package org.apache.xerces.xs;
import java.util.List;
public interface XSNamespaceItemList extends List {
    public int getLength();
    public XSNamespaceItem item(int index);
}
