package org.apache.tools.ant.types;
import java.util.Iterator;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.resources.FileResourceIterator;
public class DirSet extends AbstractFileSet implements ResourceCollection {
    public DirSet() {
        super();
    }
    protected DirSet(DirSet dirset) {
        super(dirset);
    }
    public Object clone() {
        if (isReference()) {
            return ((DirSet) getRef(getProject())).clone();
        } else {
            return super.clone();
        }
    }
    public Iterator iterator() {
        if (isReference()) {
            return ((DirSet) getRef(getProject())).iterator();
        }
        return new FileResourceIterator(getProject(), getDir(getProject()),
            getDirectoryScanner(getProject()).getIncludedDirectories());
    }
    public int size() {
        if (isReference()) {
            return ((DirSet) getRef(getProject())).size();
        }
        return getDirectoryScanner(getProject()).getIncludedDirsCount();
    }
    public boolean isFilesystemOnly() {
        return true;
    }
    public String toString() {
        DirectoryScanner ds = getDirectoryScanner(getProject());
        String[] dirs = ds.getIncludedDirectories();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < dirs.length; i++) {
            if (i > 0) {
                sb.append(';');
            }
            sb.append(dirs[i]);
        }
        return sb.toString();
    }
}
