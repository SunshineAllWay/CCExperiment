package org.apache.tools.ant.taskdefs.optional.ssh;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpProgressMonitor;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.text.NumberFormat;
import org.apache.tools.ant.BuildException;
public abstract class AbstractSshMessage {
    private static final double ONE_SECOND = 1000.0;
    private Session session;
    private boolean verbose;
    private LogListener listener = new LogListener() {
        public void log(String message) {
        }
    };
    public AbstractSshMessage(Session session) {
        this(false, session);
    }
    public AbstractSshMessage(boolean verbose, Session session) {
        this.verbose = verbose;
        this.session = session;
    }
    protected Channel openExecChannel(String command) throws JSchException {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        return channel;
    }
    protected ChannelSftp openSftpChannel() throws JSchException {
        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        return channel;
    }
    protected void sendAck(OutputStream out) throws IOException {
        byte[] buf = new byte[1];
        buf[0] = 0;
        out.write(buf);
        out.flush();
    }
    protected void waitForAck(InputStream in)
        throws IOException, BuildException {
        int b = in.read();
        if (b == -1) {
            throw new BuildException("No response from server");
        } else if (b != 0) {
            StringBuffer sb = new StringBuffer();
            int c = in.read();
            while (c > 0 && c != '\n') {
                sb.append((char) c);
                c = in.read();
            }
            if (b == 1) {
                throw new BuildException("server indicated an error: "
                                         + sb.toString());
            } else if (b == 2) {
                throw new BuildException("server indicated a fatal error: "
                                         + sb.toString());
            } else {
                throw new BuildException("unknown response, code " + b
                                         + " message: " + sb.toString());
            }
        }
    }
    public abstract void execute() throws IOException, JSchException;
    public void setLogListener(LogListener aListener) {
        listener = aListener;
    }
    protected void log(String message) {
        listener.log(message);
    }
    protected void logStats(long timeStarted,
                             long timeEnded,
                             long totalLength) {
        double duration = (timeEnded - timeStarted) / ONE_SECOND;
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(1);
        listener.log("File transfer time: " + format.format(duration)
            + " Average Rate: " + format.format(totalLength / duration)
            + " B/s");
    }
    protected final boolean getVerbose() {
        return verbose;
    }
    protected final int trackProgress(long filesize, long totalLength,
                                      int percentTransmitted) {
        int percent = (int) Math.round(Math.floor((totalLength
                                                   / (double) filesize) * 100));
        if (percent > percentTransmitted) {
            if (filesize < 1048576) {
                if (percent % 10 == 0) {
                    if (percent == 100) {
                        System.out.println(" 100%");
                    } else {
                        System.out.print("*");
                    }
                }
            } else {
                if (percent == 50) {
                    System.out.println(" 50%");
                } else if (percent == 100) {
                    System.out.println(" 100%");
                } else {
                    System.out.print(".");
                }
            }
        }
        return percent;
    }
    private ProgressMonitor monitor = null;
    protected SftpProgressMonitor getProgressMonitor() {
        if (monitor == null) {
            monitor = new ProgressMonitor();
        }
        return monitor;
    }
    private class ProgressMonitor implements SftpProgressMonitor {
        private long initFileSize = 0;
        private long totalLength = 0;
        private int percentTransmitted = 0;
        public void init(int op, String src, String dest, long max) {
            initFileSize = max;
            totalLength = 0;
            percentTransmitted = 0;
        }
        public boolean count(long len) {
            totalLength += len;
            percentTransmitted = trackProgress(initFileSize,
                                               totalLength,
                                               percentTransmitted);
            return true;
        }
        public void end() {
        }
        public long getTotalLength() {
            return totalLength;
        }
    }
}
