package org.apache.tools.ant.types.resources.comparators;
import org.apache.tools.ant.types.Resource;
public class Date extends ResourceComparator {
    protected int resourceCompare(Resource foo, Resource bar) {
        long diff = foo.getLastModified() - bar.getLastModified();
        if (diff > 0) {
            return +1;
        } else if (diff < 0) {
            return -1;
        } else {
            return 0;
        }
    }
}
