package org.apache.tools.ant.taskdefs.optional.perforce;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
public class P4OutputStream extends OutputStream {
    private P4Handler handler;
    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private boolean skip = false;
    public P4OutputStream(P4Handler handler) {
        this.handler = handler;
    }
    public void write(int cc) throws IOException {
        final byte c = (byte) cc;
        if ((c == '\n') || (c == '\r')) {
            if (!skip) {
                processBuffer();
            }
        } else {
            buffer.write(cc);
        }
        skip = (c == '\r');
    }
    protected void processBuffer() {
        handler.process(buffer.toString());
        buffer.reset();
    }
    public void close() throws IOException {
        if (buffer.size() > 0) {
            processBuffer();
        }
        super.close();
    }
}
