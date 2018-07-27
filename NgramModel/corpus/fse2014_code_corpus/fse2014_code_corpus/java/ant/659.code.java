package org.apache.tools.ant.types.resources;
import java.util.Iterator;
import org.apache.tools.ant.types.FileSet;
public class BCFileSet extends FileSet {
    public BCFileSet() {
    }
    public BCFileSet(FileSet fs) {
        super(fs);
    }
    public Iterator iterator() {
        if (isReference()) {
            return ((FileSet) getRef(getProject())).iterator();
        }
        FileResourceIterator result = new FileResourceIterator(getProject(), getDir());
        result.addFiles(getDirectoryScanner().getIncludedFiles());
        result.addFiles(getDirectoryScanner().getIncludedDirectories());
        return result;
    }
    public int size() {
        if (isReference()) {
            return ((FileSet) getRef(getProject())).size();
        }
        return getDirectoryScanner().getIncludedFilesCount()
            + getDirectoryScanner().getIncludedDirsCount();
    }
}
