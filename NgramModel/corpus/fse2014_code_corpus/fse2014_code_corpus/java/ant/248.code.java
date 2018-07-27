package org.apache.tools.ant.taskdefs;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
public class PumpStreamHandler implements ExecuteStreamHandler {
    private Thread outputThread;
    private Thread errorThread;
    private Thread inputThread;
    private OutputStream out;
    private OutputStream err;
    private InputStream input;
    private final boolean nonBlockingRead;
    public PumpStreamHandler(OutputStream out, OutputStream err,
                             InputStream input, boolean nonBlockingRead) {
        this.out = out;
        this.err = err;
        this.input = input;
        this.nonBlockingRead = nonBlockingRead;
    }
    public PumpStreamHandler(OutputStream out, OutputStream err,
                             InputStream input) {
        this(out, err, input, false);
    }
    public PumpStreamHandler(OutputStream out, OutputStream err) {
        this(out, err, null);
    }
    public PumpStreamHandler(OutputStream outAndErr) {
        this(outAndErr, outAndErr);
    }
    public PumpStreamHandler() {
        this(System.out, System.err);
    }
    public void setProcessOutputStream(InputStream is) {
        createProcessOutputPump(is, out);
    }
    public void setProcessErrorStream(InputStream is) {
        if (err != null) {
            createProcessErrorPump(is, err);
        }
    }
    public void setProcessInputStream(OutputStream os) {
        if (input != null) {
            inputThread = createPump(input, os, true, nonBlockingRead);
        } else {
            try {
                os.close();
            } catch (IOException e) {
            }
        }
    }
    public void start() {
        outputThread.start();
        errorThread.start();
        if (inputThread != null) {
            inputThread.start();
        }
    }
    public void stop() {
        finish(inputThread);
        try {
            err.flush();
        } catch (IOException e) {
        }
        try {
            out.flush();
        } catch (IOException e) {
        }
        finish(outputThread);
        finish(errorThread);
    }
    private static final long JOIN_TIMEOUT = 200;
    protected final void finish(Thread t) {
        if (t == null) {
            return;
        }
        try {
            StreamPumper s = null;
            if (t instanceof ThreadWithPumper) {
                s = ((ThreadWithPumper) t).getPumper();
            }
            if (s != null && s.isFinished()) {
                return;
            }
            if (!t.isAlive()) {
                return;
            }
            t.join(JOIN_TIMEOUT);
            if (s != null && !s.isFinished()) {
                s.stop();
            }
            while ((s == null || !s.isFinished()) && t.isAlive()) {
                t.interrupt();
                t.join(JOIN_TIMEOUT);
            }
        } catch (InterruptedException e) {
        }
    }
    protected OutputStream getErr() {
        return err;
    }
    protected OutputStream getOut() {
        return out;
    }
    protected void createProcessOutputPump(InputStream is, OutputStream os) {
        outputThread = createPump(is, os);
    }
    protected void createProcessErrorPump(InputStream is, OutputStream os) {
        errorThread = createPump(is, os);
    }
    protected Thread createPump(InputStream is, OutputStream os) {
        return createPump(is, os, false);
    }
    protected Thread createPump(InputStream is, OutputStream os,
                                boolean closeWhenExhausted) {
        return createPump(is, os, closeWhenExhausted, true);
    }
    protected Thread createPump(InputStream is, OutputStream os,
                                boolean closeWhenExhausted, boolean nonBlockingIO) {
        final Thread result
            = new ThreadWithPumper(new StreamPumper(is, os,
                                                    closeWhenExhausted,
                                                    nonBlockingIO));
        result.setDaemon(true);
        return result;
    }
    protected static class ThreadWithPumper extends Thread {
        private final StreamPumper pumper;
        public ThreadWithPumper(StreamPumper p) {
            super(p);
            pumper = p;
        }
        protected StreamPumper getPumper() {
            return pumper;
        }
    }
}
