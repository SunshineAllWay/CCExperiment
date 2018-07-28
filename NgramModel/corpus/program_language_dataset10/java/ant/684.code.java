package org.apache.tools.ant.types.resources;
import java.util.Iterator;
import java.util.Stack;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.selectors.ResourceSelector;
import org.apache.tools.ant.types.resources.selectors.ResourceSelectorContainer;
public class Restrict
    extends ResourceSelectorContainer implements ResourceCollection {
    private LazyResourceCollectionWrapper w = new  LazyResourceCollectionWrapper() {
        protected boolean filterResource(Resource r) {
            for (Iterator i = getSelectors(); i.hasNext();) {
                if (!((ResourceSelector) (i.next())).isSelected(r)) {
                    return true;
                }
            }
            return false;
        }
    };
    public synchronized void add(ResourceCollection c) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        if (c == null) {
            return;
        }
        w.add(c);
        setChecked(false);
    }
    public synchronized void setCache(boolean b) {
        w.setCache(b);
    }
    public synchronized boolean isCache() {
        return w.isCache();
    }
    public synchronized void add(ResourceSelector s) {
        if (s == null) {
            return;
        }
        super.add(s);
        FailFast.invalidate(this);
    }
    public final synchronized Iterator iterator() {
        if (isReference()) {
            return ((Restrict) getCheckedRef()).iterator();
        }
        dieOnCircularReference();
        return w.iterator();
    }
    public synchronized int size() {
        if (isReference()) {
            return ((Restrict) getCheckedRef()).size();
        }
        dieOnCircularReference();
        return w.size();
    }
    public synchronized boolean isFilesystemOnly() {
        if (isReference()) {
            return ((Restrict) getCheckedRef()).isFilesystemOnly();
        }
        dieOnCircularReference();
        return w.isFilesystemOnly();
    }
    public synchronized String toString() {
        if (isReference()) {
            return getCheckedRef().toString();
        }
        dieOnCircularReference();
        return w.toString();
    }
    protected synchronized void dieOnCircularReference(Stack stk, Project p) {
        if (isChecked()) {
            return;
        }
        super.dieOnCircularReference(stk, p);
        if (!isReference()) {
            pushAndInvokeCircularReferenceCheck(w, stk, p);
            setChecked(true);
        }
    }
}
