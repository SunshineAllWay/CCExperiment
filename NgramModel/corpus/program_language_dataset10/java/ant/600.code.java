package org.apache.tools.ant.types;
import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Iterator;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.resources.FileResourceIterator;
public class FileList extends DataType implements ResourceCollection {
    private Vector filenames = new Vector();
    private File dir;
    public FileList() {
        super();
    }
    protected FileList(FileList filelist) {
        this.dir       = filelist.dir;
        this.filenames = filelist.filenames;
        setProject(filelist.getProject());
    }
    public void setRefid(Reference r) throws BuildException {
        if ((dir != null) || (filenames.size() != 0)) {
            throw tooManyAttributes();
        }
        super.setRefid(r);
    }
    public void setDir(File dir) throws BuildException {
        checkAttributesAllowed();
        this.dir = dir;
    }
    public File getDir(Project p) {
        if (isReference()) {
            return getRef(p).getDir(p);
        }
        return dir;
    }
    public void setFiles(String filenames) {
        checkAttributesAllowed();
        if (filenames != null && filenames.length() > 0) {
            StringTokenizer tok = new StringTokenizer(
                filenames, ", \t\n\r\f", false);
            while (tok.hasMoreTokens()) {
               this.filenames.addElement(tok.nextToken());
            }
        }
    }
    public String[] getFiles(Project p) {
        if (isReference()) {
            return getRef(p).getFiles(p);
        }
        if (dir == null) {
            throw new BuildException("No directory specified for filelist.");
        }
        if (filenames.size() == 0) {
            throw new BuildException("No files specified for filelist.");
        }
        String[] result = new String[filenames.size()];
        filenames.copyInto(result);
        return result;
    }
    protected FileList getRef(Project p) {
        return (FileList) getCheckedRef(p);
    }
    public static class FileName {
        private String name;
        public void setName(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
    }
    public void addConfiguredFile(FileName name) {
        if (name.getName() == null) {
            throw new BuildException(
                "No name specified in nested file element");
        }
        filenames.addElement(name.getName());
    }
    public Iterator iterator() {
        if (isReference()) {
            return ((FileList) getRef(getProject())).iterator();
        }
        return new FileResourceIterator(getProject(), dir,
            (String[]) (filenames.toArray(new String[filenames.size()])));
    }
    public int size() {
        if (isReference()) {
            return ((FileList) getRef(getProject())).size();
        }
        return filenames.size();
    }
    public boolean isFilesystemOnly() {
        return true;
    }
}