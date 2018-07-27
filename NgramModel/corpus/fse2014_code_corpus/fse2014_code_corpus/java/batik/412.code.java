package org.apache.batik.css.parser;
import org.w3c.css.sac.SACMediaList;
public class CSSSACMediaList implements SACMediaList {
    protected String[] list = new String[3];
    protected int length;
    public int getLength() {
        return length;
    }
    public String item(int index) {
        if (index < 0 || index >= length) {
            return null;
        }
        return list[index];
    }
    public void append(String item) {
        if (length == list.length) {
            String[] tmp = list;
            list = new String[ 1 + list.length + list.length / 2];
            System.arraycopy( tmp, 0, list, 0, tmp.length );
        }
        list[length++] = item;
    }
}
