package org.apache.tools.ant.types.resources;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.util.PropertyOutputStream;
public class PropertyResource extends Resource {
    private static final int PROPERTY_MAGIC
        = Resource.getMagicNumber("PropertyResource".getBytes());
    private static final InputStream UNSET = new InputStream() {
        public int read() {
            return -1;
        }
    };
    public PropertyResource() {
    }
    public PropertyResource(Project p, String n) {
        super(n);
        setProject(p);
    }
    public String getValue() {
        if (isReference()) {
            return ((PropertyResource) getCheckedRef()).getValue();
        }
        Project p = getProject();
        return p == null ? null : p.getProperty(getName());
    }
    public Object getObjectValue() {
        if (isReference()) {
            return ((PropertyResource) getCheckedRef()).getObjectValue();
        }
        Project p = getProject();
        return p == null ? null : PropertyHelper.getProperty(p, getName());
    }
    public boolean isExists() {
        if (isReferenceOrProxy()) {
            return getReferencedOrProxied().isExists();
        }
        return getObjectValue() != null;
    }
    public long getSize() {
        if (isReferenceOrProxy()) {
            return getReferencedOrProxied().getSize();
        }
        Object o = getObjectValue();
        return o == null ? 0L : (long) String.valueOf(o).length();
    }
    public boolean equals(Object o) {
        if (super.equals(o)) {
            return true;
        }
        return isReferenceOrProxy() && getReferencedOrProxied().equals(o);
    }
    public int hashCode() {
        if (isReferenceOrProxy()) {
            return getReferencedOrProxied().hashCode();
        }
        return super.hashCode() * PROPERTY_MAGIC;
    }
    public String toString() {
        if (isReferenceOrProxy()) {
            return getReferencedOrProxied().toString();
        }
        return getValue();
    }
    public InputStream getInputStream() throws IOException {
        if (isReferenceOrProxy()) {
            return getReferencedOrProxied().getInputStream();
        }
        Object o = getObjectValue();
        return o == null ? UNSET : new ByteArrayInputStream(String.valueOf(o).getBytes());
    }
    public OutputStream getOutputStream() throws IOException {
        if (isReferenceOrProxy()) {
            return getReferencedOrProxied().getOutputStream();
        }
        if (isExists()) {
            throw new ImmutableResourceException();
        }
        return new PropertyOutputStream(getProject(), getName());
    }
    protected boolean isReferenceOrProxy() {
        return isReference() || getObjectValue() instanceof Resource;
    }
    protected Resource getReferencedOrProxied() {
        if (isReference()) {
            return (Resource) getCheckedRef(Resource.class, "resource");
        }
        Object o = getObjectValue();
        if (o instanceof Resource) {
            return (Resource) o;
        }
        throw new IllegalStateException(
                "This PropertyResource does not reference or proxy another Resource");
    }
}
