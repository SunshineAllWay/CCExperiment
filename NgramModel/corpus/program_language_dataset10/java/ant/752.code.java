package org.apache.tools.ant.types.selectors.modifiedselector;
import java.util.Iterator;
public interface Cache {
    boolean isValid();
    void delete();
    void load();
    void save();
    Object get(Object key);
    void put(Object key, Object value);
    Iterator iterator();
}