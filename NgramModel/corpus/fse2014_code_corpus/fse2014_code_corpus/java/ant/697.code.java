package org.apache.tools.ant.types.resources.comparators;
import java.util.Stack;
import java.util.Vector;
import java.util.Iterator;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Resource;
public class DelegatedResourceComparator extends ResourceComparator {
    private Vector v = null;
    public synchronized void add(ResourceComparator c) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        if (c == null) {
            return;
        }
        v = (v == null) ? new Vector() : v;
        v.add(c);
        setChecked(false);
    }
    public synchronized boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (isReference()) {
            return getCheckedRef().equals(o);
        }
        if (!(o instanceof DelegatedResourceComparator)) {
            return false;
        }
        Vector ov = ((DelegatedResourceComparator) o).v;
        return v == null ? ov == null : v.equals(ov);
    }
    public synchronized int hashCode() {
        if (isReference()) {
            return getCheckedRef().hashCode();
        }
        return v == null ? 0 : v.hashCode();
    }
    protected synchronized int resourceCompare(Resource foo, Resource bar) {
        if (v == null || v.isEmpty()) {
            return foo.compareTo(bar);
        }
        int result = 0;
        for (Iterator i = v.iterator(); result == 0 && i.hasNext();) {
            result = ((ResourceComparator) i.next()).resourceCompare(foo, bar);
        }
        return result;
    }
    protected void dieOnCircularReference(Stack stk, Project p)
        throws BuildException {
        if (isChecked()) {
            return;
        }
        if (isReference()) {
            super.dieOnCircularReference(stk, p);
        } else {
            if (!(v == null || v.isEmpty())) {
                for (Iterator i = v.iterator(); i.hasNext();) {
                    Object o = i.next();
                    if (o instanceof DataType) {
                        pushAndInvokeCircularReferenceCheck((DataType) o, stk,
                                                            p);
                    }
                }
            }
            setChecked(true);
        }
    }
}
