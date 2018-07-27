package org.apache.tools.ant.types.resources;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FilterOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.Reference;
public class StringResource extends Resource {
    private static final int STRING_MAGIC
        = Resource.getMagicNumber("StringResource".getBytes());
    private String encoding = null;
    public StringResource() {
    }
    public StringResource(String value) {
        this(null, value);
    }
    public StringResource(Project project, String value) {
        setProject(project);
        setValue(project == null ? value : project.replaceProperties(value));
    }
    public synchronized void setName(String s) {
        if (getName() != null) {
            throw new BuildException(new ImmutableResourceException());
        }
        super.setName(s);
    }
    public synchronized void setValue(String s) {
        setName(s);
    }
    public synchronized String getName() {
        return super.getName();
    }
    public synchronized String getValue() {
        return getName();
    }
    public boolean isExists() {
        return getValue() != null;
    }
    public void addText(String text) {
        checkChildrenAllowed();
        setValue(getProject().replaceProperties(text));
    }
    public synchronized void setEncoding(String s) {
        checkAttributesAllowed();
        encoding = s;
    }
    public synchronized String getEncoding() {
        return encoding;
    }
    public synchronized long getSize() {
        return isReference() ? ((Resource) getCheckedRef()).getSize()
                : getContent().length();
    }
    public synchronized int hashCode() {
        if (isReference()) {
            return getCheckedRef().hashCode();
        }
        return super.hashCode() * STRING_MAGIC;
    }
    public String toString() {
        return String.valueOf(getContent());
    }
    public synchronized InputStream getInputStream() throws IOException {
        if (isReference()) {
            return ((Resource) getCheckedRef()).getInputStream();
        }
        String content = getContent();
        if (content == null) {
            throw new IllegalStateException("unset string value");
        }
        return new ByteArrayInputStream(encoding == null
                ? content.getBytes() : content.getBytes(encoding));
    }
    public synchronized OutputStream getOutputStream() throws IOException {
        if (isReference()) {
            return ((Resource) getCheckedRef()).getOutputStream();
        }
        if (getValue() != null) {
            throw new ImmutableResourceException();
        }
        return new StringResourceFilterOutputStream();
    }
    public void setRefid(Reference r) {
        if (encoding != null) {
            throw tooManyAttributes();
        }
        super.setRefid(r);
    }
    protected synchronized String getContent() {
        return getValue();
    }
    private void setValueFromOutputStream(String output) {
        String value;
        if (getProject() != null) {
            value = getProject().replaceProperties(output);
        } else {
            value = output;
        }
        setValue(value);
    }
    private class StringResourceFilterOutputStream extends FilterOutputStream {
        private final ByteArrayOutputStream baos;
        public StringResourceFilterOutputStream() {
            super(new ByteArrayOutputStream());
            baos = (ByteArrayOutputStream) out;
        }
        public void close() throws IOException {
            super.close();
            String result = encoding == null
                    ? baos.toString() : baos.toString(encoding);
            StringResource.this.setValueFromOutputStream(result);
        }
    }
}
