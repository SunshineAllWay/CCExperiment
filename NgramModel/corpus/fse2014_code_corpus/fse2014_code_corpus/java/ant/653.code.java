package org.apache.tools.ant.types.resources;
import java.io.File;
import java.util.Iterator;
import java.util.Stack;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
public abstract class AbstractResourceCollectionWrapper
    extends DataType implements ResourceCollection, Cloneable {
    private static final String ONE_NESTED_MESSAGE
        = " expects exactly one nested resource collection.";
    private ResourceCollection rc;
    private boolean cache = true;
    public synchronized void setCache(boolean b) {
        cache = b;
    }
    public synchronized boolean isCache() {
        return cache;
    }
    public synchronized void add(ResourceCollection c) throws BuildException {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        if (c == null) {
            return;
        }
        if (rc != null) {
            throw oneNested();
        }
        rc = c;
        if (Project.getProject(rc) == null) {
            Project p = getProject();
            if (p != null) {
                p.setProjectReference(rc);
            }
        }
        setChecked(false);
    }
    public final synchronized Iterator iterator() {
        if (isReference()) {
            return ((AbstractResourceCollectionWrapper) getCheckedRef()).iterator();
        }
        dieOnCircularReference();
        return new FailFast(this, createIterator());
    }
    protected abstract Iterator createIterator();
    public synchronized int size() {
        if (isReference()) {
            return ((AbstractResourceCollectionWrapper) getCheckedRef()).size();
        }
        dieOnCircularReference();
        return getSize();
    }
    protected abstract int getSize();
    public synchronized boolean isFilesystemOnly() {
        if (isReference()) {
            return ((BaseResourceCollectionContainer) getCheckedRef()).isFilesystemOnly();
        }
        dieOnCircularReference();
        if (rc == null || rc.isFilesystemOnly()) {
            return true;
        }
        for (Iterator i = createIterator(); i.hasNext();) {
            Resource r = (Resource) i.next();
            if (r.as(FileProvider.class) == null) {
                return false;
            }
        }
        return true;
    }
    protected synchronized void dieOnCircularReference(Stack stk, Project p)
        throws BuildException {
        if (isChecked()) {
            return;
        }
        if (isReference()) {
            super.dieOnCircularReference(stk, p);
        } else {
            if (rc instanceof DataType) {
                pushAndInvokeCircularReferenceCheck((DataType) rc, stk, p);
            }
            setChecked(true);
        }
    }
    protected final synchronized ResourceCollection getResourceCollection() {
        dieOnCircularReference();
        if (rc == null) {
            throw oneNested();
        }
        return rc;
    }
    public synchronized String toString() {
        if (isReference()) {
            return getCheckedRef().toString();
        }
        if (getSize() == 0) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (Iterator i = createIterator(); i.hasNext();) {
            if (sb.length() > 0) {
                sb.append(File.pathSeparatorChar);
            }
            sb.append(i.next());
        }
        return sb.toString();
    }
    private BuildException oneNested() {
        return new BuildException(super.toString() + ONE_NESTED_MESSAGE);
    }
}
