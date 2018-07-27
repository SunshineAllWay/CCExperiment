package org.apache.xerces.xni;
import java.util.Enumeration;
public interface Augmentations {
    public Object putItem (String key, Object item);
    public Object getItem(String key);
    public Object removeItem (String key);
    public Enumeration keys ();
    public void removeAllItems ();
}
