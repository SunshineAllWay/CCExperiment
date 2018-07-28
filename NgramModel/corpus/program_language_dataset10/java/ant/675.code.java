package org.apache.tools.ant.types.resources;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.ResourceCollection;
public class Last extends SizeLimitCollection {
    protected Collection getCollection() {
        int count = getValidCount();
        ResourceCollection rc = getResourceCollection();
        int i = count;
        Iterator iter = rc.iterator();
        int size = rc.size();
        for (; i < size; i++) {
            iter.next();
        }
        ArrayList al = new ArrayList(count);
        for (; iter.hasNext(); i++) {
            al.add(iter.next());
        }
        int found = al.size();
        if (found == count || (size < count && found == size)) {
            return al;
        }
        String msg = "Resource collection " + rc + " reports size " + size
            + " but returns " + i + " elements.";
        if (found > count) {
            log(msg, Project.MSG_WARN);
            return al.subList(found - count, found);
        }
        throw new BuildException(msg);
    }
}
