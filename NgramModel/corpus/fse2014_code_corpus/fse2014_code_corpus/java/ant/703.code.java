package org.apache.tools.ant.types.resources.comparators;
import org.apache.tools.ant.types.Resource;
public class Size extends ResourceComparator {
    protected int resourceCompare(Resource foo, Resource bar) {
        return (int) (foo.getSize() - bar.getSize());
    }
}
