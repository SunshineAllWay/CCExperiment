package org.apache.xerces.xs;
import java.util.List;
public interface StringList extends List {
    public int getLength();
    public boolean contains(String item);
    public String item(int index);
}
