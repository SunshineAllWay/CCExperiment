package org.apache.tools.ant.types.resources.comparators;
import java.util.Stack;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Resource;
public class Reverse extends ResourceComparator {
    private static final String ONE_NESTED
        = "You must not nest more than one ResourceComparator for reversal.";
    private ResourceComparator nested;
    public Reverse() {
    }
    public Reverse(ResourceComparator c) {
        add(c);
    }
    public void add(ResourceComparator c) {
        if (nested != null) {
            throw new BuildException(ONE_NESTED);
        }
        nested = c;
        setChecked(false);
    }
    protected int resourceCompare(Resource foo, Resource bar) {
        return -1 * (nested == null
            ? foo.compareTo(bar) : nested.compare(foo, bar));
    }
    protected void dieOnCircularReference(Stack stk, Project p)
        throws BuildException {
        if (isChecked()) {
            return;
        }
        if (isReference()) {
            super.dieOnCircularReference(stk, p);
        } else {
            if (nested instanceof DataType) {
                pushAndInvokeCircularReferenceCheck((DataType) nested, stk,
                                                    p);
            }
            setChecked(true);
        }
    }
}
