package org.apache.tools.ant.types.selectors;
import java.util.Iterator;
import org.apache.tools.ant.types.selectors.modifiedselector.Cache;
public class MockCache implements Cache {
    public boolean debug = false;
    public boolean saved = false;
    public MockCache() {
        log("()");
    }
    public boolean isValid() {
        log(".isValid()");
        return true;
    }
    public void delete() {
        log(".delete()");
    }
    public void load() {
        log(".load()");
    }
    public void save() {
        log(".save()");
        saved = true;
    }
    public Object get(Object key) {
        log(".get("+key+")");
        return key;
    }
    public void put(Object key, Object value) {
        log(".put("+key+", "+value+")");
        saved = false;
    }
    public Iterator iterator() {
        log("iterator()");
        return null;
    }
    public String toString() {
        return "MockCache@" + hashCode();
    }
    private void log(String msg) {
        if (debug) System.out.println(this+msg);
    }
}
