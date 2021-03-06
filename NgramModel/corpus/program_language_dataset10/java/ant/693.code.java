package org.apache.tools.ant.types.resources;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.FileUtils;
public class URLResource extends Resource implements URLProvider {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private static final int NULL_URL
        = Resource.getMagicNumber("null URL".getBytes());
    private URL url;
    private URLConnection conn;
    private URL baseURL;
    private String relPath;
    public URLResource() {
    }
    public URLResource(URL u) {
        setURL(u);
    }
    public URLResource(URLProvider u) {
        setURL(u.getURL());
    }
    public URLResource(File f) {
        setFile(f);
    }
    public URLResource(String u) {
        this(newURL(u));
    }
    public synchronized void setURL(URL u) {
        checkAttributesAllowed();
        url = u;
    }
    public synchronized void setFile(File f) {
        try {
            setURL(FILE_UTILS.getFileURL(f));
        } catch (MalformedURLException e) {
            throw new BuildException(e);
        }
    }
    public synchronized void setBaseURL(URL base) {
        checkAttributesAllowed();
        if (url != null) {
            throw new BuildException("can't define URL and baseURL attribute");
        }
        baseURL = base;
    }
    public synchronized void setRelativePath(String r) {
        checkAttributesAllowed();
        if (url != null) {
            throw new BuildException("can't define URL and relativePath"
                                     + " attribute");
        }
        relPath = r;
    }
    public synchronized URL getURL() {
        if (isReference()) {
            return ((URLResource) getCheckedRef()).getURL();
        }
        if (url == null) {
            if (baseURL != null) {
                if (relPath == null) {
                    throw new BuildException("must provide relativePath"
                                             + " attribute when using baseURL.");
                }
                try {
                    url = new URL(baseURL, relPath);
                } catch (MalformedURLException e) {
                    throw new BuildException(e);
                }
            }
        }
        return url;
     }
    public synchronized void setRefid(Reference r) {
        if (url != null || baseURL != null || relPath != null) {
            throw tooManyAttributes();
        }
        super.setRefid(r);
    }
    public synchronized String getName() {
        if (isReference()) {
            return ((Resource) getCheckedRef()).getName();
        }
        String name = getURL().getFile();
        return "".equals(name) ? name : name.substring(1);
    }
    public synchronized String toString() {
        return isReference()
            ? getCheckedRef().toString() : String.valueOf(getURL());
    }
    public synchronized boolean isExists() {
        if (isReference()) {
            return ((Resource) getCheckedRef()).isExists();
        }
        return isExists(false);
    }
    private synchronized boolean isExists(boolean closeConnection) {
        if (getURL() == null) {
            return false;
        }
        try {
            connect();
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (closeConnection) {
                close();
            }
        }
    }
    public synchronized long getLastModified() {
        if (isReference()) {
            return ((Resource) getCheckedRef()).getLastModified();
        }
        if (!isExists(false)) {
            return 0L;
        }
        return conn.getLastModified();
    }
    public synchronized boolean isDirectory() {
        return isReference()
            ? ((Resource) getCheckedRef()).isDirectory()
            : getName().endsWith("/");
    }
    public synchronized long getSize() {
        if (isReference()) {
            return ((Resource) getCheckedRef()).getSize();
        }
        if (!isExists(false)) {
            return 0L;
        }
        try {
            connect();
            long contentlength = conn.getContentLength();
            close();
            return contentlength;
        } catch (IOException e) {
            return UNKNOWN_SIZE;
        }
    }
    public synchronized boolean equals(Object another) {
        if (this == another) {
            return true;
        }
        if (isReference()) {
            return getCheckedRef().equals(another);
        }
        if (!(another.getClass().equals(getClass()))) {
            return false;
        }
        URLResource otheru = (URLResource) another;
        return getURL() == null
            ? otheru.getURL() == null
            : getURL().equals(otheru.getURL());
    }
    public synchronized int hashCode() {
        if (isReference()) {
            return getCheckedRef().hashCode();
        }
        return MAGIC * ((getURL() == null) ? NULL_URL : getURL().hashCode());
    }
    public synchronized InputStream getInputStream() throws IOException {
        if (isReference()) {
            return ((Resource) getCheckedRef()).getInputStream();
        }
        connect();
        try {
            return conn.getInputStream();
        } finally {
            conn = null;
        }
    }
    public synchronized OutputStream getOutputStream() throws IOException {
        if (isReference()) {
            return ((Resource) getCheckedRef()).getOutputStream();
        }
        connect();
        try {
            return conn.getOutputStream();
        } finally {
            conn = null;
        }
    }
    protected synchronized void connect() throws IOException {
        URL u = getURL();
        if (u == null) {
            throw new BuildException("URL not set");
        }
        if (conn == null) {
            try {
                conn = u.openConnection();
                conn.connect();
            } catch (IOException e) {
                log(e.toString(), Project.MSG_ERR);
                conn = null;
                throw e;
            }
        }
    }
    private synchronized void close() {
        try {
            FileUtils.close(conn);
        } finally {
            conn = null;
        }
    }
    private static URL newURL(String u) {
        try {
            return new URL(u);
        } catch (MalformedURLException e) {
            throw new BuildException(e);
        }
    }
}
