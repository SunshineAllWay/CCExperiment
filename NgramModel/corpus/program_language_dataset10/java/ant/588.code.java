package org.apache.tools.ant.types;
import java.io.File;
import java.util.Iterator;
import java.util.Stack;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.zip.UnixStat;
public abstract class ArchiveFileSet extends FileSet {
    private static final int BASE_OCTAL = 8;
    public static final int DEFAULT_DIR_MODE =
        UnixStat.DIR_FLAG | UnixStat.DEFAULT_DIR_PERM;
    public static final int DEFAULT_FILE_MODE =
        UnixStat.FILE_FLAG | UnixStat.DEFAULT_FILE_PERM;
    private Resource src          = null;
    private String prefix         = "";
    private String fullpath       = "";
    private boolean hasDir        = false;
    private int fileMode          = DEFAULT_FILE_MODE;
    private int dirMode           = DEFAULT_DIR_MODE;
    private boolean fileModeHasBeenSet = false;
    private boolean dirModeHasBeenSet  = false;
    private static final String ERROR_DIR_AND_SRC_ATTRIBUTES = "Cannot set both dir and src attributes";
    private static final String ERROR_PATH_AND_PREFIX = "Cannot set both fullpath and prefix attributes";
    private boolean errorOnMissingArchive = true;
    public ArchiveFileSet() {
        super();
    }
    protected ArchiveFileSet(FileSet fileset) {
        super(fileset);
    }
    protected ArchiveFileSet(ArchiveFileSet fileset) {
        super(fileset);
        src = fileset.src;
        prefix = fileset.prefix;
        fullpath = fileset.fullpath;
        hasDir = fileset.hasDir;
        fileMode = fileset.fileMode;
        dirMode = fileset.dirMode;
        fileModeHasBeenSet = fileset.fileModeHasBeenSet;
        dirModeHasBeenSet = fileset.dirModeHasBeenSet;
        errorOnMissingArchive = fileset.errorOnMissingArchive;
    }
    public void setDir(File dir) throws BuildException {
        checkAttributesAllowed();
        if (src != null) {
            throw new BuildException(ERROR_DIR_AND_SRC_ATTRIBUTES);
        }
        super.setDir(dir);
        hasDir = true;
    }
    public void addConfigured(ResourceCollection a) {
        checkChildrenAllowed();
        if (a.size() != 1) {
            throw new BuildException("only single argument resource collections"
                                     + " are supported as archives");
        }
        setSrcResource((Resource) a.iterator().next());
    }
    public void setSrc(File srcFile) {
        setSrcResource(new FileResource(srcFile));
    }
    public void setSrcResource(Resource src) {
        checkArchiveAttributesAllowed();
        if (hasDir) {
            throw new BuildException(ERROR_DIR_AND_SRC_ATTRIBUTES);
        }
        this.src = src;
        setChecked(false);
    }
    public File getSrc(Project p) {
        if (isReference()) {
            return ((ArchiveFileSet) getRef(p)).getSrc(p);
        }
        return getSrc();
    }
    public void setErrorOnMissingArchive(boolean errorOnMissingArchive) {
        checkAttributesAllowed();
        this.errorOnMissingArchive = errorOnMissingArchive;
    }
    public File getSrc() {
        if (isReference()) {
            return ((ArchiveFileSet) getCheckedRef()).getSrc();
        }
        dieOnCircularReference();
        if (src != null) {
            FileProvider fp = (FileProvider) src.as(FileProvider.class);
            if (fp != null) {
                return fp.getFile();
            }
        }
        return null;
    }
    protected Object getCheckedRef(Project p) {
        return getRef(p);
    }
    public void setPrefix(String prefix) {
        checkArchiveAttributesAllowed();
        if (!"".equals(prefix) && !"".equals(fullpath)) {
            throw new BuildException(ERROR_PATH_AND_PREFIX);
        }
        this.prefix = prefix;
    }
    public String getPrefix(Project p) {
        if (isReference()) {
            return ((ArchiveFileSet) getRef(p)).getPrefix(p);
        }
        dieOnCircularReference(p);
        return prefix;
    }
    public void setFullpath(String fullpath) {
        checkArchiveAttributesAllowed();
        if (!"".equals(prefix) && !"".equals(fullpath)) {
            throw new BuildException(ERROR_PATH_AND_PREFIX);
        }
        this.fullpath = fullpath;
    }
    public String getFullpath(Project p) {
        if (isReference()) {
            return ((ArchiveFileSet) getRef(p)).getFullpath(p);
        }
        dieOnCircularReference(p);
        return fullpath;
    }
    protected abstract ArchiveScanner newArchiveScanner();
    public DirectoryScanner getDirectoryScanner(Project p) {
        if (isReference()) {
            return getRef(p).getDirectoryScanner(p);
        }
        dieOnCircularReference();
        if (src == null) {
            return super.getDirectoryScanner(p);
        }
        if (!src.isExists() && errorOnMissingArchive) {
            throw new BuildException(
                "The archive " + src.getName() + " doesn't exist");
        }
        if (src.isDirectory()) {
            throw new BuildException("The archive " + src.getName()
                                     + " can't be a directory");
        }
        ArchiveScanner as = newArchiveScanner();
        as.setErrorOnMissingArchive(errorOnMissingArchive);
        as.setSrc(src);
        super.setDir(p.getBaseDir());
        setupDirectoryScanner(as, p);
        as.init();
        return as;
    }
    public Iterator iterator() {
        if (isReference()) {
            return ((ResourceCollection) (getRef(getProject()))).iterator();
        }
        if (src == null) {
            return super.iterator();
        }
        ArchiveScanner as = (ArchiveScanner) getDirectoryScanner(getProject());
        return as.getResourceFiles(getProject());
    }
    public int size() {
        if (isReference()) {
            return ((ResourceCollection) (getRef(getProject()))).size();
        }
        if (src == null) {
            return super.size();
        }
        ArchiveScanner as = (ArchiveScanner) getDirectoryScanner(getProject());
        return as.getIncludedFilesCount();
    }
    public boolean isFilesystemOnly() {
        if (isReference()) {
            return ((ArchiveFileSet) getCheckedRef()).isFilesystemOnly();
        }
        dieOnCircularReference();
        return src == null;
    }
    public void setFileMode(String octalString) {
        checkArchiveAttributesAllowed();
        integerSetFileMode(Integer.parseInt(octalString, BASE_OCTAL));
    }
    public void integerSetFileMode(int mode) {
        fileModeHasBeenSet = true;
        this.fileMode = UnixStat.FILE_FLAG | mode;
    }
    public int getFileMode(Project p) {
        if (isReference()) {
            return ((ArchiveFileSet) getRef(p)).getFileMode(p);
        }
        dieOnCircularReference();
        return fileMode;
    }
    public boolean hasFileModeBeenSet() {
        if (isReference()) {
            return ((ArchiveFileSet) getRef(getProject())).hasFileModeBeenSet();
        }
        dieOnCircularReference();
        return fileModeHasBeenSet;
    }
    public void setDirMode(String octalString) {
        checkArchiveAttributesAllowed();
        integerSetDirMode(Integer.parseInt(octalString, BASE_OCTAL));
    }
    public void integerSetDirMode(int mode) {
        dirModeHasBeenSet = true;
        this.dirMode = UnixStat.DIR_FLAG | mode;
    }
    public int getDirMode(Project p) {
        if (isReference()) {
            return ((ArchiveFileSet) getRef(p)).getDirMode(p);
        }
        dieOnCircularReference();
        return dirMode;
    }
    public boolean hasDirModeBeenSet() {
        if (isReference()) {
            return ((ArchiveFileSet) getRef(getProject())).hasDirModeBeenSet();
        }
        dieOnCircularReference();
        return dirModeHasBeenSet;
    }
    protected void configureFileSet(ArchiveFileSet zfs) {
        zfs.setPrefix(prefix);
        zfs.setFullpath(fullpath);
        zfs.fileModeHasBeenSet = fileModeHasBeenSet;
        zfs.fileMode = fileMode;
        zfs.dirModeHasBeenSet = dirModeHasBeenSet;
        zfs.dirMode = dirMode;
    }
    public Object clone() {
        if (isReference()) {
            return ((ArchiveFileSet) getRef(getProject())).clone();
        }
        return super.clone();
    }
    public String toString() {
        if (hasDir && getProject() != null) {
            return super.toString();
        }
        return src == null ? null : src.getName();
    }
    public String getPrefix() {
        return prefix;
    }
    public String getFullpath() {
        return fullpath;
    }
    public int getFileMode() {
        return fileMode;
    }
    public int getDirMode() {
        return dirMode;
    }
    private void checkArchiveAttributesAllowed() {
        if (getProject() == null
            || (isReference()
                && (getRefid().getReferencedObject(
                        getProject())
                    instanceof ArchiveFileSet))) {
            checkAttributesAllowed();
        }
    }
    protected synchronized void dieOnCircularReference(Stack stk, Project p)
        throws BuildException {
        if (isChecked()) {
            return;
        }
        super.dieOnCircularReference(stk, p);
        if (!isReference()) {
            if (src != null) {
                pushAndInvokeCircularReferenceCheck(src, stk, p);
            }
            setChecked(true);
        }
    }
}
