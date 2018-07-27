package org.apache.tools.ant.types.resources;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
public class TarResource extends ArchiveResource {
    private String userName = "";
    private String groupName = "";
    private int    uid;
    private int    gid;
    public TarResource() {
    }
    public TarResource(File a, TarEntry e) {
        super(a, true);
        setEntry(e);
    }
    public TarResource(Resource a, TarEntry e) {
        super(a, true);
        setEntry(e);
    }
    public InputStream getInputStream() throws IOException {
        if (isReference()) {
            return ((Resource) getCheckedRef()).getInputStream();
        }
        Resource archive = getArchive();
        final TarInputStream i = new TarInputStream(archive.getInputStream());
        TarEntry te = null;
        while ((te = i.getNextEntry()) != null) {
            if (te.getName().equals(getName())) {
                return i;
            }
        }
        FileUtils.close(i);
        throw new BuildException("no entry " + getName() + " in "
                                 + getArchive());
    }
    public OutputStream getOutputStream() throws IOException {
        if (isReference()) {
            return ((Resource) getCheckedRef()).getOutputStream();
        }
        throw new UnsupportedOperationException(
            "Use the tar task for tar output.");
    }
    public String getUserName() {
        if (isReference()) {
            return ((TarResource) getCheckedRef()).getUserName();
        }
        checkEntry();
        return userName;
    }
    public String getGroup() {
        if (isReference()) {
            return ((TarResource) getCheckedRef()).getGroup();
        }
        checkEntry();
        return groupName;
    }
    public int getUid() {
        if (isReference()) {
            return ((TarResource) getCheckedRef()).getUid();
        }
        checkEntry();
        return uid;
    }
    public int getGid() {
        if (isReference()) {
            return ((TarResource) getCheckedRef()).getGid();
        }
        checkEntry();
        return gid;
    }
    protected void fetchEntry() {
        Resource archive = getArchive();
        TarInputStream i = null;
        try {
            i = new TarInputStream(archive.getInputStream());
            TarEntry te = null;
            while ((te = i.getNextEntry()) != null) {
                if (te.getName().equals(getName())) {
                    setEntry(te);
                    return;
                }
            }
        } catch (IOException e) {
            log(e.getMessage(), Project.MSG_DEBUG);
            throw new BuildException(e);
        } finally {
            if (i != null) {
                FileUtils.close(i);
            }
        }
        setEntry(null);
    }
    private void setEntry(TarEntry e) {
        if (e == null) {
            setExists(false);
            return;
        }
        setName(e.getName());
        setExists(true);
        setLastModified(e.getModTime().getTime());
        setDirectory(e.isDirectory());
        setSize(e.getSize());
        setMode(e.getMode());
        userName = e.getUserName();
        groupName = e.getGroupName();
        uid = e.getUserId();
        gid = e.getGroupId();
    }
}
