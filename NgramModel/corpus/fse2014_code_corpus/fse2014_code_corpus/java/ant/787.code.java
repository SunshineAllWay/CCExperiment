package org.apache.tools.ant.util;
import java.util.Hashtable;
import java.util.Enumeration;
public class LazyHashtable extends Hashtable {
    protected boolean initAllDone = false;
    public LazyHashtable() {
        super();
    }
    protected void initAll() {
        if (initAllDone) {
            return;
        }
        initAllDone = true;
    }
    public Enumeration elements() {
        initAll();
        return super.elements();
    }
    public boolean isEmpty() {
        initAll();
        return super.isEmpty();
    }
    public int size() {
        initAll();
        return super.size();
    }
    public boolean contains(Object value) {
        initAll();
        return super.contains(value);
    }
    public boolean containsKey(Object value) {
        initAll();
        return super.containsKey(value);
    }
    public boolean containsValue(Object value) {
        return contains(value);
    }
    public Enumeration keys() {
        initAll();
        return super.keys();
    }
}
