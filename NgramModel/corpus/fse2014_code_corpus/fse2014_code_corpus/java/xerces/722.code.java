package org.apache.xerces.xs;
import java.util.List;
public interface XSObjectList extends List {
    public int getLength();
    public XSObject item(int index);
}
