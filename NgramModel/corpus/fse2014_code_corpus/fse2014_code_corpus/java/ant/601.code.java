package org.apache.tools.ant.types;
import java.util.Iterator;
import org.apache.tools.ant.types.resources.FileResourceIterator;
public class FileSet extends AbstractFileSet implements ResourceCollection {
    public FileSet() {
        super();
    }
    protected FileSet(FileSet fileset) {
        super(fileset);
    }
    public Object clone() {
        if (isReference()) {
            return ((FileSet) getRef(getProject())).clone();
        } else {
            return super.clone();
        }
    }
    public Iterator iterator() {
        if (isReference()) {
            return ((FileSet) getRef(getProject())).iterator();
        }
        return new FileResourceIterator(getProject(), getDir(getProject()),
            getDirectoryScanner(getProject()).getIncludedFiles());
    }
    public int size() {
        if (isReference()) {
            return ((FileSet) getRef(getProject())).size();
        }
        return getDirectoryScanner(getProject()).getIncludedFilesCount();
    }
    public boolean isFilesystemOnly() {
        return true;
    }
}
