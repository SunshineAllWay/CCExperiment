package org.apache.tools.ant.types.resources;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.ArchiveFileSet;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.TarFileSet;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.util.CollectionUtils;
public class Archives extends DataType
    implements ResourceCollection, Cloneable {
    private Union zips = new Union();
    private Union tars = new Union();
    public Union createZips() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        return zips;
    }
    public Union createTars() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        return tars;
    }
    public int size() {
        if (isReference()) {
            return ((Archives) getCheckedRef()).size();
        }
        dieOnCircularReference();
        int total = 0;
        for (Iterator i = grabArchives(); i.hasNext(); ) {
            total += ((ResourceCollection) i.next()).size();
        }
        return total;
    }
    public Iterator iterator() {
        if (isReference()) {
            return ((Archives) getCheckedRef()).iterator();
        }
        dieOnCircularReference();
        List l = new LinkedList();
        for (Iterator i = grabArchives(); i.hasNext(); ) {
            l.addAll(CollectionUtils
                     .asCollection(((ResourceCollection) i.next()).iterator()));
        }
        return l.iterator();
    }
    public boolean isFilesystemOnly() {
        if (isReference()) {
            return ((Archives) getCheckedRef()).isFilesystemOnly();
        }
        dieOnCircularReference();
        return false;
    }
    public void setRefid(Reference r) {
        if (zips.getResourceCollections().size() > 0
            || tars.getResourceCollections().size() > 0) {
            throw tooManyAttributes();
        }
        super.setRefid(r);
    }
    public Object clone() {
        try {
            Archives a = (Archives) super.clone();
            a.zips = (Union) zips.clone();
            a.tars = (Union) tars.clone();
            return a;
        } catch (CloneNotSupportedException e) {
            throw new BuildException(e);
        }
    }
    protected Iterator grabArchives() {
        List l = new LinkedList();
        for (Iterator iter = zips.iterator(); iter.hasNext(); ) {
            l.add(configureArchive(new ZipFileSet(),
                                   (Resource) iter.next()));
        }
        for (Iterator iter = tars.iterator(); iter.hasNext(); ) {
            l.add(configureArchive(new TarFileSet(),
                                   (Resource) iter.next()));
        }
        return l.iterator();
    }
    protected ArchiveFileSet configureArchive(ArchiveFileSet afs,
                                              Resource src) {
        afs.setProject(getProject());
        afs.setSrcResource(src);
        return afs;
    }
    protected synchronized void dieOnCircularReference(Stack stk, Project p)
        throws BuildException {
        if (isChecked()) {
            return;
        }
        if (isReference()) {
            super.dieOnCircularReference(stk, p);
        } else {
            pushAndInvokeCircularReferenceCheck(zips, stk, p);
            pushAndInvokeCircularReferenceCheck(tars, stk, p);
            setChecked(true);
        }
    }
}