package org.apache.tools.ant.types.resources;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.ResourceFactory;
public class FileResource extends Resource implements Touchable, FileProvider,
        ResourceFactory, Appendable {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private static final int NULL_FILE
        = Resource.getMagicNumber("null file".getBytes());
    private File file;
    private File baseDir;
    public FileResource() {
    }
    public FileResource(File b, String name) {
        setFile(FILE_UTILS.resolveFile(b, name));
        setBaseDir(b);
    }
    public FileResource(File f) {
        setFile(f);
    }
    public FileResource(Project p, File f) {
        setProject(p);
        setFile(f);
    }
    public FileResource(Project p, String s) {
        this(p, p.resolveFile(s));
    }
    public void setFile(File f) {
        checkAttributesAllowed();
        file = f;
    }
    public File getFile() {
        if (isReference()) {
            return ((FileResource) getCheckedRef()).getFile();
        }
        dieOnCircularReference();
        return file;
    }
    public void setBaseDir(File b) {
        checkAttributesAllowed();
        baseDir = b;
    }
    public File getBaseDir() {
        if (isReference()) {
            return ((FileResource) getCheckedRef()).getBaseDir();
        }
        dieOnCircularReference();
        return baseDir;
    }
    public void setRefid(Reference r) {
        if (file != null || baseDir != null) {
            throw tooManyAttributes();
        }
        super.setRefid(r);
    }
    public String getName() {
        if (isReference()) {
            return ((Resource) getCheckedRef()).getName();
        }
        File b = getBaseDir();
        return b == null ? getNotNullFile().getName()
            : FILE_UTILS.removeLeadingPath(b, getNotNullFile());
    }
    public boolean isExists() {
        return isReference() ? ((Resource) getCheckedRef()).isExists()
            : getNotNullFile().exists();
    }
    public long getLastModified() {
        return isReference()
            ? ((Resource) getCheckedRef()).getLastModified()
            : getNotNullFile().lastModified();
    }
    public boolean isDirectory() {
        return isReference() ? ((Resource) getCheckedRef()).isDirectory()
            : getNotNullFile().isDirectory();
    }
    public long getSize() {
        return isReference() ? ((Resource) getCheckedRef()).getSize()
            : getNotNullFile().length();
    }
    public InputStream getInputStream() throws IOException {
        return isReference()
            ? ((Resource) getCheckedRef()).getInputStream()
            : new FileInputStream(getNotNullFile());
    }
    public OutputStream getOutputStream() throws IOException {
        if (isReference()) {
            return ((FileResource) getCheckedRef()).getOutputStream();
        }
        return getOutputStream(false);
    }
    public OutputStream getAppendOutputStream() throws IOException {
        if (isReference()) {
            return ((FileResource) getCheckedRef()).getAppendOutputStream();
        }
        return getOutputStream(true);
    }
    private OutputStream getOutputStream(boolean append) throws IOException {
        File f = getNotNullFile();
        if (f.exists()) {
            if (f.isFile() && !append) {
                f.delete();
            }
        } else {
            File p = f.getParentFile();
            if (p != null && !(p.exists())) {
                p.mkdirs();
            }
        }
        return append ? new FileOutputStream(f.getAbsolutePath(), true) : new FileOutputStream(f);
    }
    public int compareTo(Object another) {
        if (isReference()) {
            return ((Comparable) getCheckedRef()).compareTo(another);
        }
        if (this.equals(another)) {
            return 0;
        }
        if (another instanceof Resource) {
            Resource r = (Resource) another;
            FileProvider otherFP = (FileProvider) r.as(FileProvider.class);
            if (otherFP != null) {
                File f = getFile();
                if (f == null) {
                    return -1;
                }
                File of = otherFP.getFile();
                if (of == null) {
                    return 1;
                }
                return f.compareTo(of);
            }
        }
        return super.compareTo(another);
    }
    public boolean equals(Object another) {
        if (this == another) {
            return true;
        }
        if (isReference()) {
            return getCheckedRef().equals(another);
        }
        if (!(another.getClass().equals(getClass()))) {
            return false;
        }
        FileResource otherfr = (FileResource) another;
        return getFile() == null
            ? otherfr.getFile() == null
            : getFile().equals(otherfr.getFile());
    }
    public int hashCode() {
        if (isReference()) {
            return getCheckedRef().hashCode();
        }
        return MAGIC * (getFile() == null ? NULL_FILE : getFile().hashCode());
    }
    public String toString() {
        if (isReference()) {
            return getCheckedRef().toString();
        }
        if (file == null) {
            return "(unbound file resource)";
        }
        String absolutePath = file.getAbsolutePath();
        return FILE_UTILS.normalize(absolutePath).getAbsolutePath();
    }
    public boolean isFilesystemOnly() {
        if (isReference()) {
            return ((FileResource) getCheckedRef()).isFilesystemOnly();
        }
        dieOnCircularReference();
        return true;
    }
    public void touch(long modTime) {
        if (isReference()) {
            ((FileResource) getCheckedRef()).touch(modTime);
            return;
        }
        if (!getNotNullFile().setLastModified(modTime)) {
            log("Failed to change file modification time", Project.MSG_WARN);
        }
    }
    protected File getNotNullFile() {
        if (getFile() == null) {
            throw new BuildException("file attribute is null!");
        }
        dieOnCircularReference();
        return getFile();
    }
    public Resource getResource(String path) {
        File newfile = FILE_UTILS.resolveFile(getFile(), path);
        FileResource fileResource = new FileResource(newfile);
        fileResource.setBaseDir(getBaseDir());
        return fileResource;
    }
}
