package org.apache.tools.ant.types;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.tools.ant.types.resources.FileProvider;
public class Resource extends DataType implements Cloneable, Comparable, ResourceCollection {
    public static final long UNKNOWN_SIZE = -1;
    public static final long UNKNOWN_DATETIME = 0L;
    protected static final int MAGIC = getMagicNumber("Resource".getBytes());
    private static final int NULL_NAME = getMagicNumber("null name".getBytes());
    protected static int getMagicNumber(byte[] seed) {
        return new BigInteger(seed).intValue();
    }
    private String name = null;
    private Boolean exists = null;
    private Long lastmodified = null;
    private Boolean directory = null;
    private Long size = null;
    public Resource() {
    }
    public Resource(String name) {
        this(name, false, 0, false);
    }
    public Resource(String name, boolean exists, long lastmodified) {
        this(name, exists, lastmodified, false);
    }
    public Resource(String name, boolean exists, long lastmodified, boolean directory) {
        this(name, exists, lastmodified, directory, UNKNOWN_SIZE);
    }
    public Resource(String name, boolean exists, long lastmodified, boolean directory, long size) {
        this.name = name;
        setName(name);
        setExists(exists);
        setLastModified(lastmodified);
        setDirectory(directory);
        setSize(size);
    }
    public String getName() {
        return isReference() ? ((Resource) getCheckedRef()).getName() : name;
    }
    public void setName(String name) {
        checkAttributesAllowed();
        this.name = name;
    }
    public boolean isExists() {
        if (isReference()) {
            return ((Resource) getCheckedRef()).isExists();
        }
        return exists == null || exists.booleanValue();
    }
    public void setExists(boolean exists) {
        checkAttributesAllowed();
        this.exists = exists ? Boolean.TRUE : Boolean.FALSE;
    }
    public long getLastModified() {
        if (isReference()) {
            return ((Resource) getCheckedRef()).getLastModified();
        }
        if (!isExists() || lastmodified == null) {
            return UNKNOWN_DATETIME;
        }
        long result = lastmodified.longValue();
        return result < UNKNOWN_DATETIME ? UNKNOWN_DATETIME : result;
    }
    public void setLastModified(long lastmodified) {
        checkAttributesAllowed();
        this.lastmodified = new Long(lastmodified);
    }
    public boolean isDirectory() {
        if (isReference()) {
            return ((Resource) getCheckedRef()).isDirectory();
        }
        return directory != null && directory.booleanValue();
    }
    public void setDirectory(boolean directory) {
        checkAttributesAllowed();
        this.directory = directory ? Boolean.TRUE : Boolean.FALSE;
    }
    public void setSize(long size) {
        checkAttributesAllowed();
        this.size = new Long(size > UNKNOWN_SIZE ? size : UNKNOWN_SIZE);
    }
    public long getSize() {
        if (isReference()) {
            return ((Resource) getCheckedRef()).getSize();
        }
        return isExists()
            ? (size != null ? size.longValue() : UNKNOWN_SIZE)
            : 0L;
    }
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new UnsupportedOperationException(
                    "CloneNotSupportedException for a Resource caught. "
                    + "Derived classes must support cloning.");
        }
    }
    public int compareTo(Object other) {
        if (isReference()) {
            return ((Comparable) getCheckedRef()).compareTo(other);
        }
        if (!(other instanceof Resource)) {
            throw new IllegalArgumentException(
                "Can only be compared with Resources");
        }
        return toString().compareTo(other.toString());
    }
    public boolean equals(Object other) {
        if (isReference()) {
            return getCheckedRef().equals(other);
        }
        return other.getClass().equals(getClass()) && compareTo(other) == 0;
    }
    public int hashCode() {
        if (isReference()) {
            return getCheckedRef().hashCode();
        }
        String name = getName();
        return MAGIC * (name == null ? NULL_NAME : name.hashCode());
    }
    public InputStream getInputStream() throws IOException {
        if (isReference()) {
            return ((Resource) getCheckedRef()).getInputStream();
        }
        throw new UnsupportedOperationException();
    }
    public OutputStream getOutputStream() throws IOException {
        if (isReference()) {
            return ((Resource) getCheckedRef()).getOutputStream();
        }
        throw new UnsupportedOperationException();
    }
    public Iterator iterator() {
        return isReference() ? ((Resource) getCheckedRef()).iterator()
            : new Iterator() {
            private boolean done = false;
            public boolean hasNext() {
                return !done;
            }
            public Object next() {
                if (done) {
                    throw new NoSuchElementException();
                }
                done = true;
                return Resource.this;
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    public int size() {
        return isReference() ? ((Resource) getCheckedRef()).size() : 1;
    }
    public boolean isFilesystemOnly() {
        return (isReference() && ((Resource) getCheckedRef()).isFilesystemOnly())
            || this.as(FileProvider.class) != null;
    }
    public String toString() {
        if (isReference()) {
            return getCheckedRef().toString();
        }
        String n = getName();
        return n == null ? "(anonymous)" : n;
    }
    public final String toLongString() {
        return isReference() ? ((Resource) getCheckedRef()).toLongString()
            : getDataTypeName() + " \"" + toString() + '"';
    }
    public void setRefid(Reference r) {
        if (name != null
            || exists != null
            || lastmodified != null
            || directory != null
            || size != null) {
            throw tooManyAttributes();
        }
        super.setRefid(r);
    }
    public Object as(Class clazz) {
        return clazz.isAssignableFrom(getClass()) ? this : null;
    }
}
