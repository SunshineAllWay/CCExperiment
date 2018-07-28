package org.apache.batik.dom.svg;
import org.apache.batik.util.DoublyIndexedTable;
public class AttributeInitializer {
    protected String[] keys;
    protected int length;
    protected DoublyIndexedTable values = new DoublyIndexedTable();
    public AttributeInitializer(int capacity) {
        keys = new String[capacity * 3];
    }
    public void addAttribute(String ns, String prefix, String ln, String val) {
        int len = keys.length;
        if (length == len) {
            String[] t = new String[len * 2];
            System.arraycopy( keys, 0, t, 0, len );
            keys = t;
        }
        keys[length++] = ns;
        keys[length++] = prefix;
        keys[length++] = ln;
        values.put(ns, ln, val);
    }
    public void initializeAttributes(AbstractElement elt) {
        for (int i = length - 1; i >= 2; i -= 3) {
            resetAttribute(elt, keys[i - 2], keys[i - 1], keys[i]);
        }
    }
    public boolean resetAttribute(AbstractElement elt,
                                  String ns, String prefix, String ln) {
        String val = (String)values.get(ns, ln);
        if (val == null) {
            return false;
        }
        if (prefix != null) {
            ln = prefix + ':' + ln;
        }
        elt.setUnspecifiedAttribute(ns, ln, val);
        return true;
    }
}
