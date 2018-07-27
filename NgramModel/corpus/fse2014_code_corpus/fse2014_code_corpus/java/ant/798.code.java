package org.apache.tools.ant.util;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
public class ReaderInputStream extends InputStream {
    private static final int BYTE_MASK = 0xFF;
    private Reader in;
    private String encoding = System.getProperty("file.encoding");
    private byte[] slack;
    private int begin;
    public ReaderInputStream(Reader reader) {
        in = reader;
    }
    public ReaderInputStream(Reader reader, String encoding) {
        this(reader);
        if (encoding == null) {
            throw new IllegalArgumentException("encoding must not be null");
        } else {
            this.encoding = encoding;
        }
    }
    public synchronized int read() throws IOException {
        if (in == null) {
            throw new IOException("Stream Closed");
        }
        byte result;
        if (slack != null && begin < slack.length) {
            result = slack[begin];
            if (++begin == slack.length) {
                slack = null;
            }
        } else {
            byte[] buf = new byte[1];
            if (read(buf, 0, 1) <= 0) {
                return -1;
            } else {
                result = buf[0];
            }
        }
        return result & BYTE_MASK;
    }
    public synchronized int read(byte[] b, int off, int len)
        throws IOException {
        if (in == null) {
            throw new IOException("Stream Closed");
        }
        if (len == 0) {
            return 0;
        }
        while (slack == null) {
            char[] buf = new char[len]; 
            int n = in.read(buf);
            if (n == -1) {
                return -1;
            }
            if (n > 0) {
                slack = new String(buf, 0, n).getBytes(encoding);
                begin = 0;
            }
        }
        if (len > slack.length - begin) {
            len = slack.length - begin;
        }
        System.arraycopy(slack, begin, b, off, len);
        begin += len;
        if (begin >= slack.length) {
            slack = null;
        }
        return len;
    }
    public synchronized void mark(final int limit) {
        try {
            in.mark(limit);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe.getMessage());
        }
    }
    public synchronized int available() throws IOException {
        if (in == null) {
            throw new IOException("Stream Closed");
        }
        if (slack != null) {
            return slack.length - begin;
        }
        if (in.ready()) {
            return 1;
        }
        return 0;
    }
    public boolean markSupported () {
        return false;   
    }
    public synchronized void reset() throws IOException {
        if (in == null) {
            throw new IOException("Stream Closed");
        }
        slack = null;
        in.reset();
    }
    public synchronized void close() throws IOException {
        if (in != null) {
            in.close();
            slack = null;
            in = null;
        }
    }
}
