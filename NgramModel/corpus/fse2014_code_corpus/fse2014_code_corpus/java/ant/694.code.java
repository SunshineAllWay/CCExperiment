package org.apache.tools.ant.types.resources;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.FilterInputStream;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipExtraField;
public class ZipResource extends ArchiveResource {
    private String encoding;
    private ZipExtraField[] extras;
    private int method;
    public ZipResource() {
    }
    public ZipResource(File z, String enc, ZipEntry e) {
        super(z, true);
        setEncoding(enc);
        setEntry(e);
    }
    public void setZipfile(File z) {
        setArchive(z);
    }
    public File getZipfile() {
        FileProvider fp = (FileProvider) getArchive().as(FileProvider.class);
        return fp.getFile();
    }
    public void addConfigured(ResourceCollection a) {
        super.addConfigured(a);
        if (!a.isFilesystemOnly()) {
            throw new BuildException("only filesystem resources are supported");
        }
    }
    public void setEncoding(String enc) {
        checkAttributesAllowed();
        encoding = enc;
    }
    public String getEncoding() {
        return isReference()
            ? ((ZipResource) getCheckedRef()).getEncoding() : encoding;
    }
    public void setRefid(Reference r) {
        if (encoding != null) {
            throw tooManyAttributes();
        }
        super.setRefid(r);
    }
    public InputStream getInputStream() throws IOException {
        if (isReference()) {
            return ((Resource) getCheckedRef()).getInputStream();
        }
        final ZipFile z = new ZipFile(getZipfile(), getEncoding());
        ZipEntry ze = z.getEntry(getName());
        if (ze == null) {
            z.close();
            throw new BuildException("no entry " + getName() + " in "
                                     + getArchive());
        }
        return new FilterInputStream(z.getInputStream(ze)) {
            public void close() throws IOException {
                FileUtils.close(in);
                z.close();
            }
            protected void finalize() throws Throwable {
                try {
                    close();
                } finally {
                    super.finalize();
                }
            }
        };
    }
    public OutputStream getOutputStream() throws IOException {
        if (isReference()) {
            return ((Resource) getCheckedRef()).getOutputStream();
        }
        throw new UnsupportedOperationException(
            "Use the zip task for zip output.");
    }
    public ZipExtraField[] getExtraFields() {
        if (isReference()) {
            return ((ZipResource) getCheckedRef()).getExtraFields();
        }
        checkEntry();
        if (extras == null) {
            return new ZipExtraField[0];
        }
        return extras;
    }
    public int getMethod() {
        return method;
    }
    protected void fetchEntry() {
        ZipFile z = null;
        try {
            z = new ZipFile(getZipfile(), getEncoding());
            setEntry(z.getEntry(getName()));
        } catch (IOException e) {
            log(e.getMessage(), Project.MSG_DEBUG);
            throw new BuildException(e);
        } finally {
            ZipFile.closeQuietly(z);
        }
    }
    private void setEntry(ZipEntry e) {
        if (e == null) {
            setExists(false);
            return;
        }
        setName(e.getName());
        setExists(true);
        setLastModified(e.getTime());
        setDirectory(e.isDirectory());
        setSize(e.getSize());
        setMode(e.getUnixMode());
        extras = e.getExtraFields(true);
        method = e.getMethod();
    }
}
