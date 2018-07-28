package org.apache.xerces.xs.datatypes;
import java.util.List;
import org.apache.xerces.xs.XSException;
public interface ByteList extends List {
    public int getLength();
    public boolean contains(byte item);
    public byte item(int index) throws XSException;
    public byte[] toByteArray();
}
