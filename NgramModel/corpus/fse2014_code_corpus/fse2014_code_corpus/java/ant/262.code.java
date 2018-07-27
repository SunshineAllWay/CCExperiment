package org.apache.tools.ant.taskdefs;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.tools.ant.util.FileUtils;
public class StreamPumper implements Runnable {
    private static final int SMALL_BUFFER_SIZE = 128;
    private final InputStream is;
    private final OutputStream os;
    private volatile boolean finish;
    private volatile boolean finished;
    private final boolean closeWhenExhausted;
    private boolean autoflush = false;
    private Exception exception = null;
    private int bufferSize = SMALL_BUFFER_SIZE;
    private boolean started = false;
    private final boolean useAvailable;
    public StreamPumper(InputStream is, OutputStream os, boolean closeWhenExhausted) {
        this(is, os, closeWhenExhausted, false);
    }
    public StreamPumper(InputStream is, OutputStream os,
                        boolean closeWhenExhausted,
                        boolean useAvailable) {
        this.is = is;
        this.os = os;
        this.closeWhenExhausted = closeWhenExhausted;
        this.useAvailable = useAvailable;
    }
    public StreamPumper(InputStream is, OutputStream os) {
        this(is, os, false);
    }
     void setAutoflush(boolean autoflush) {
        this.autoflush = autoflush;
    }
    public void run() {
        synchronized (this) {
            started = true;
        }
        finished = false;
        finish = false;
        final byte[] buf = new byte[bufferSize];
        int length;
        try {
            while (true) {
                waitForInput(is);
                if (finish || Thread.interrupted()) {
                    break;
                }
                length = is.read(buf);
                if (length <= 0 || finish || Thread.interrupted()) {
                    break;
                }
                os.write(buf, 0, length);
                if (autoflush) {
                    os.flush();
                }
            }
            os.flush();
        } catch (InterruptedException ie) {
        } catch (Exception e) {
            synchronized (this) {
                exception = e;
            }
        } finally {
            if (closeWhenExhausted) {
                FileUtils.close(os);
            }
            finished = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }
    public boolean isFinished() {
        return finished;
    }
    public synchronized void waitFor() throws InterruptedException {
        while (!isFinished()) {
            wait();
        }
    }
    public synchronized void setBufferSize(int bufferSize) {
        if (started) {
            throw new IllegalStateException("Cannot set buffer size on a running StreamPumper");
        }
        this.bufferSize = bufferSize;
    }
    public synchronized int getBufferSize() {
        return bufferSize;
    }
    public synchronized Exception getException() {
        return exception;
    }
     synchronized void stop() {
        finish = true;
        notifyAll();
    }
    private static final long POLL_INTERVAL = 100;
    private void waitForInput(InputStream is)
        throws IOException, InterruptedException {
        if (useAvailable) {
            while (!finish && is.available() == 0) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                synchronized (this) {
                    this.wait(POLL_INTERVAL);
                }
            }
        }
    }
}
