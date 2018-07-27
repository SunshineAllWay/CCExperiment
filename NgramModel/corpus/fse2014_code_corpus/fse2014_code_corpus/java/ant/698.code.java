package org.apache.tools.ant.types.resources.comparators;
import org.apache.tools.ant.types.Resource;
public class Exists extends ResourceComparator {
    protected int resourceCompare(Resource foo, Resource bar) {
        boolean f = foo.isExists();
        if (f == bar.isExists()) {
            return 0;
        }
        return f ? 1 : -1;
    }
}
