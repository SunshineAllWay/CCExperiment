package org.apache.tools.ant.types.resources;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.bzip2.CBZip2OutputStream;
public class BZip2Resource extends CompressedResource {
    private static final char[] MAGIC = new char[] {'B', 'Z'};
    public BZip2Resource() {
    }
    public BZip2Resource(org.apache.tools.ant.types.ResourceCollection other) {
        super(other);
    }
    protected InputStream wrapStream(InputStream in) throws IOException {
        for (int i = 0; i < MAGIC.length; i++) {
            if (in.read() != MAGIC[i]) {
                throw new IOException("Invalid bz2 stream.");
            }
        }
        return new CBZip2InputStream(in);
    }
    protected OutputStream wrapStream(OutputStream out) throws IOException {
        for (int i = 0; i < MAGIC.length; i++) {
            out.write(MAGIC[i]);
        }
        return new CBZip2OutputStream(out);
    }
    protected String getCompressionName() {
        return "Bzip2";
    }
}
