package org.apache.tools.ant.types.resources.comparators;
import java.util.Comparator;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Resource;
public abstract class ResourceComparator extends DataType implements Comparator {
    public final int compare(Object foo, Object bar) {
        dieOnCircularReference();
        ResourceComparator c =
            isReference() ? (ResourceComparator) getCheckedRef() : this;
        return c.resourceCompare((Resource) foo, (Resource) bar);
    }
    public boolean equals(Object o) {
        if (isReference()) {
            return getCheckedRef().equals(o);
        }
        if (o == null) {
            return false;
        }
        return o == this || o.getClass().equals(getClass());
    }
    public synchronized int hashCode() {
        if (isReference()) {
            return getCheckedRef().hashCode();
        }
        return getClass().hashCode();
    }
    protected abstract int resourceCompare(Resource foo, Resource bar);
}
