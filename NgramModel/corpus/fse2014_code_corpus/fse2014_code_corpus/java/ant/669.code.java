package org.apache.tools.ant.types.resources;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
public class First extends SizeLimitCollection {
    protected Collection getCollection() {
        int ct = getValidCount();
        Iterator iter = getResourceCollection().iterator();
        ArrayList al = new ArrayList(ct);
        for (int i = 0; i < ct && iter.hasNext(); i++) {
            al.add(iter.next());
        }
        return al;
    }
}
