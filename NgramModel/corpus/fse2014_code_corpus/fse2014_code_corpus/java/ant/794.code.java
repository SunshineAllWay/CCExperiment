package org.apache.tools.ant.util;
import java.io.IOException;
import java.io.OutputStream;
public class OutputStreamFunneler {
    public static final long DEFAULT_TIMEOUT_MILLIS = 1000;
    private final class Funnel extends OutputStream {
        private boolean closed = false;
        private Funnel() {
            synchronized (OutputStreamFunneler.this) {
                ++count;
            }
        }
        public void flush() throws IOException {
            synchronized (OutputStreamFunneler.this) {
                dieIfClosed();
                out.flush();
            }
        }
        public void write(int b) throws IOException {
            synchronized (OutputStreamFunneler.this) {
                dieIfClosed();
                out.write(b);
            }
        }
        public void write(byte[] b) throws IOException {
            synchronized (OutputStreamFunneler.this) {
                dieIfClosed();
                out.write(b);
            }
        }
        public void write(byte[] b, int off, int len) throws IOException {
            synchronized (OutputStreamFunneler.this) {
                dieIfClosed();
                out.write(b, off, len);
            }
        }
        public void close() throws IOException {
            release(this);
        }
    }
    private OutputStream out;
    private int count = 0;
    private boolean closed;
    private long timeoutMillis;
    public OutputStreamFunneler(OutputStream out) {
        this(out, DEFAULT_TIMEOUT_MILLIS);
    }
    public OutputStreamFunneler(OutputStream out, long timeoutMillis) {
        if (out == null) {
            throw new IllegalArgumentException(
                "OutputStreamFunneler.<init>:  out == null");
        }
        this.out = out;
        this.closed = false; 
        setTimeout(timeoutMillis);
    }
    public synchronized void setTimeout(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }
    public synchronized OutputStream getFunnelInstance()
        throws IOException {
        dieIfClosed();
        try {
            return new Funnel();
        } finally {
            notifyAll();
        }
    }
    private synchronized void release(Funnel funnel) throws IOException {
        if (!funnel.closed) {
            try {
                if (timeoutMillis > 0) {
                    try {
                        wait(timeoutMillis);
                    } catch (InterruptedException eyeEx) {
                    }
                }
                if (--count == 0) {
                    close();
                }
            } finally {
                funnel.closed = true;
            }
        }
   }
    private synchronized void close() throws IOException {
        try {
            dieIfClosed();
            out.close();
        } finally {
            closed = true;
        }
    }
    private synchronized void dieIfClosed() throws IOException {
        if (closed) {
            throw new IOException("The funneled OutputStream has been closed.");
        }
    }
}
