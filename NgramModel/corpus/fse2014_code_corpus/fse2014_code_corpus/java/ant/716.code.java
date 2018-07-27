package org.apache.tools.ant.types.resources.selectors;
import java.util.Stack;
import java.util.Vector;
import java.util.Iterator;
import java.util.Collections;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.DataType;
public class ResourceSelectorContainer extends DataType {
    private Vector v = new Vector();
    public ResourceSelectorContainer() {
    }
    public ResourceSelectorContainer(ResourceSelector[] r) {
        for (int i = 0; i < r.length; i++) {
            add(r[i]);
        }
    }
    public void add(ResourceSelector s) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        if (s == null) {
            return;
        }
        v.add(s);
        setChecked(false);
    }
    public boolean hasSelectors() {
        if (isReference()) {
            return ((ResourceSelectorContainer) getCheckedRef()).hasSelectors();
        }
        dieOnCircularReference();
        return !v.isEmpty();
    }
    public int selectorCount() {
        if (isReference()) {
            return ((ResourceSelectorContainer) getCheckedRef()).selectorCount();
        }
        dieOnCircularReference();
        return v.size();
    }
    public Iterator getSelectors() {
        if (isReference()) {
            return ((ResourceSelectorContainer) getCheckedRef()).getSelectors();
        }
        dieOnCircularReference();
        return Collections.unmodifiableList(v).iterator();
    }
    protected void dieOnCircularReference(Stack stk, Project p)
        throws BuildException {
        if (isChecked()) {
            return;
        }
        if (isReference()) {
            super.dieOnCircularReference(stk, p);
        } else {
            for (Iterator i = v.iterator(); i.hasNext();) {
                Object o = i.next();
                if (o instanceof DataType) {
                    pushAndInvokeCircularReferenceCheck((DataType) o, stk, p);
                }
            }
            setChecked(true);
        }
    }
}
