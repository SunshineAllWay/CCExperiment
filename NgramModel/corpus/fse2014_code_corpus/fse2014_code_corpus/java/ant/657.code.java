package org.apache.tools.ant.types.resources;
import java.io.File;
import java.util.List;
import java.util.Stack;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
public abstract class BaseResourceCollectionContainer
        extends DataType implements ResourceCollection, Cloneable {
    private List rc = new ArrayList();
    private Collection coll = null;
    private boolean cache = true;
    public BaseResourceCollectionContainer() {
    }
    public BaseResourceCollectionContainer(Project project) {
        setProject(project);
    }
    public synchronized void setCache(boolean b) {
        cache = b;
    }
    public synchronized boolean isCache() {
        return cache;
    }
    public synchronized void clear() throws BuildException {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        rc.clear();
        FailFast.invalidate(this);
        coll = null;
        setChecked(false);
    }
    public synchronized void add(ResourceCollection c) throws BuildException {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        if (c == null) {
            return;
        }
        if (Project.getProject(c) == null) {
            Project p = getProject();
            if (p != null) {
                p.setProjectReference(c);
            }
        }
        rc.add(c);
        FailFast.invalidate(this);
        coll = null;
        setChecked(false);
    }
    public synchronized void addAll(Collection c) throws BuildException {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        try {
            for (Iterator i = c.iterator(); i.hasNext();) {
                add((ResourceCollection) i.next());
            }
        } catch (ClassCastException e) {
            throw new BuildException(e);
        }
    }
    public final synchronized Iterator iterator() {
        if (isReference()) {
            return ((BaseResourceCollectionContainer) getCheckedRef()).iterator();
        }
        dieOnCircularReference();
        return new FailFast(this, cacheCollection().iterator());
    }
    public synchronized int size() {
        if (isReference()) {
            return ((BaseResourceCollectionContainer) getCheckedRef()).size();
        }
        dieOnCircularReference();
        return cacheCollection().size();
    }
    public synchronized boolean isFilesystemOnly() {
        if (isReference()) {
            return ((BaseResourceCollectionContainer) getCheckedRef()).isFilesystemOnly();
        }
        dieOnCircularReference();
        boolean goEarly = true;
        for (Iterator i = rc.iterator(); goEarly && i.hasNext();) {
            goEarly = ((ResourceCollection) i.next()).isFilesystemOnly();
        }
        if (goEarly) {
            return true;
        }
        for (Iterator i = cacheCollection().iterator(); i.hasNext();) {
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
            for (Iterator i = rc.iterator(); i.hasNext();) {
                Object o = i.next();
                if (o instanceof DataType) {
                    pushAndInvokeCircularReferenceCheck((DataType) o, stk, p);
                }
            }
            setChecked(true);
        }
    }
    public final synchronized List getResourceCollections() {
        dieOnCircularReference();
        return Collections.unmodifiableList(rc);
    }
    protected abstract Collection getCollection();
    public Object clone() {
        try {
            BaseResourceCollectionContainer c
                = (BaseResourceCollectionContainer) super.clone();
            c.rc = new ArrayList(rc);
            c.coll = null;
            return c;
        } catch (CloneNotSupportedException e) {
            throw new BuildException(e);
        }
    }
    public synchronized String toString() {
        if (isReference()) {
            return getCheckedRef().toString();
        }
        if (cacheCollection().size() == 0) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (Iterator i = coll.iterator(); i.hasNext();) {
            if (sb.length() > 0) {
                sb.append(File.pathSeparatorChar);
            }
            sb.append(i.next());
        }
        return sb.toString();
    }
    private synchronized Collection cacheCollection() {
        if (coll == null || !isCache()) {
            coll = getCollection();
        }
        return coll;
    }
}
