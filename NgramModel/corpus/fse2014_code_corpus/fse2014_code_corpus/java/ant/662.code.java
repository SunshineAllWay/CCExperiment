package org.apache.tools.ant.types.resources;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.util.FileUtils;
public abstract class ContentTransformingResource extends ResourceDecorator {
    private static final int BUFFER_SIZE = 8192;
    protected ContentTransformingResource() {
    }
    protected ContentTransformingResource(ResourceCollection other) {
        super(other);
    }
    public long getSize() {
        if (isExists()) {
            InputStream in = null;
            try {
                in = getInputStream();
                byte[] buf = new byte[BUFFER_SIZE];
                int size = 0;
                int readNow;
                while ((readNow = in.read(buf, 0, buf.length)) > 0) {
                    size += readNow;
                }
                return size;
            } catch (IOException ex) {
                throw new BuildException("caught exception while reading "
                                         + getName(), ex);
            } finally {
                FileUtils.close(in);
            }
        } else {
            return 0;
        }
    }
    public InputStream getInputStream() throws IOException {
        InputStream in = getResource().getInputStream();
        if (in != null) {
            in = wrapStream(in);
        }
        return in;
    }
    public OutputStream getOutputStream() throws IOException {
        OutputStream out = getResource().getOutputStream();
        if (out != null) {
            out = wrapStream(out);
        }
        return out;
    }
    public Object as(Class clazz) {
        if (Appendable.class.isAssignableFrom(clazz)) {
            if (isAppendSupported()) {
                final Appendable a =
                    (Appendable) getResource().as(Appendable.class);
                if (a != null) {
                    return new Appendable() {
                        public OutputStream getAppendOutputStream()
                                throws IOException {
                            OutputStream out = a.getAppendOutputStream();
                            if (out != null) {
                                out = wrapStream(out);
                            }
                            return out;
                        }
                    };
                }
            }
            return null;
        }
        return FileProvider.class.isAssignableFrom(clazz) 
            ? null : getResource().as(clazz);
    }
    protected boolean isAppendSupported() {
        return false;
    }    
    protected abstract InputStream wrapStream(InputStream in)
        throws IOException;
    protected abstract OutputStream wrapStream(OutputStream out)
        throws IOException;
}
