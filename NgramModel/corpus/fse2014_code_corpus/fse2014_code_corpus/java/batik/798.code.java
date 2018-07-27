package org.apache.batik.ext.awt.image.codec.util;
import java.io.IOException;
import java.io.InputStream;
public class ForwardSeekableStream extends SeekableStream {
    private InputStream src;
    long pointer = 0L;
    public ForwardSeekableStream(InputStream src) {
        this.src = src;
    }
    public final int read() throws IOException {
        int result = src.read();
        if (result != -1) {
            ++pointer;
        }
        return result;
    }
    public final int read(byte[] b, int off, int len) throws IOException {
        int result = src.read(b, off, len);
        if (result != -1) {
            pointer += result;
        }
        return result;
    }
    public final long skip(long n) throws IOException {
        long skipped = src.skip(n);
        pointer += skipped;
        return skipped;
    }
    public final int available() throws IOException {
        return src.available();
    }
    public final void close() throws IOException {
        src.close();
    }
    public final synchronized void mark(int readLimit) {
        markPos = pointer;
        src.mark(readLimit);
    }
    public final synchronized void reset() throws IOException {
        if (markPos != -1) {
            pointer = markPos;
        }
        src.reset();
    }
    public boolean markSupported() {
        return src.markSupported();
    }
    public final boolean canSeekBackwards() {
        return false;
    }
    public final long getFilePointer() {
        return pointer;
    }
    public final void seek(long pos) throws IOException {
        while (pos - pointer > 0) {
            pointer += src.skip(pos - pointer);
        }
    }
}
