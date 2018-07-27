package org.apache.tools.ant.types;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
public class ZipFileSet extends ArchiveFileSet {
    private String encoding = null;
    public ZipFileSet() {
        super();
    }
    protected ZipFileSet(FileSet fileset) {
        super(fileset);
    }
    protected ZipFileSet(ZipFileSet fileset) {
        super(fileset);
        encoding = fileset.encoding;
    }
    public void setEncoding(String enc) {
        checkZipFileSetAttributesAllowed();
        this.encoding = enc;
    }
    public String getEncoding() {
        if (isReference()) {
            AbstractFileSet ref = getRef(getProject());
            if (ref instanceof ZipFileSet) {
                return ((ZipFileSet) ref).getEncoding();
            } else {
                return null;
            }
        }
        return encoding;
    }
    protected ArchiveScanner newArchiveScanner() {
        ZipScanner zs = new ZipScanner();
        zs.setEncoding(encoding);
        return zs;
    }
    protected AbstractFileSet getRef(Project p) {
        dieOnCircularReference(p);
        Object o = getRefid().getReferencedObject(p);
        if (o instanceof ZipFileSet) {
            return (AbstractFileSet) o;
        } else if (o instanceof FileSet) {
            ZipFileSet zfs = new ZipFileSet((FileSet) o);
            configureFileSet(zfs);
            return zfs;
        } else {
            String msg = getRefid().getRefId() + " doesn\'t denote a zipfileset or a fileset";
            throw new BuildException(msg);
        }
    }
    public Object clone() {
        if (isReference()) {
            return ((ZipFileSet) getRef(getProject())).clone();
        } else {
            return super.clone();
        }
    }
    private void checkZipFileSetAttributesAllowed() {
        if (getProject() == null
            || (isReference()
                && (getRefid().getReferencedObject(
                        getProject())
                    instanceof ZipFileSet))) {
            checkAttributesAllowed();
        }
    }
}
