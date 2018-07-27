package org.apache.tools.ant.util;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
public class LazyFileOutputStream extends OutputStream {
    private FileOutputStream fos;
    private File file;
    private boolean append;
    private boolean alwaysCreate;
    private boolean opened = false;
    private boolean closed = false;
    public LazyFileOutputStream(String name) {
        this(name, false);
    }
    public LazyFileOutputStream(String name, boolean append) {
        this(new File(name), append);
    }
    public LazyFileOutputStream(File f) {
        this(f, false);
    }
    public LazyFileOutputStream(File file, boolean append) {
        this(file, append, false);
    }
    public LazyFileOutputStream(File file, boolean append,
                                boolean alwaysCreate) {
        this.file = file;
        this.append = append;
        this.alwaysCreate = alwaysCreate;
    }
    public void open() throws IOException {
        ensureOpened();
    }
    public synchronized void close() throws IOException {
        if (alwaysCreate && !closed) {
            ensureOpened();
        }
        if (opened) {
            fos.close();
        }
        closed = true;
    }
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }
    public synchronized void write(byte[] b, int offset, int len)
        throws IOException {
        ensureOpened();
        fos.write(b, offset, len);
    }
    public synchronized void write(int b) throws IOException {
        ensureOpened();
        fos.write(b);
    }
    private synchronized void ensureOpened() throws IOException {
        if (closed) {
            throw new IOException(file + " has already been closed.");
        }
        if (!opened) {
            fos = new FileOutputStream(file.getAbsolutePath(), append);
            opened = true;
        }
    }
}
