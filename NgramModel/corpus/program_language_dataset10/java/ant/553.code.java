package org.apache.tools.ant.taskdefs.optional.ssh;
import java.io.File;
import java.io.IOException;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpProgressMonitor;
import org.apache.tools.ant.util.FileUtils;
public class ScpFromMessageBySftp extends ScpFromMessage {
    private static final int HUNDRED_KILOBYTES = 102400;
    private String remoteFile;
    private File localFile;
    private boolean isRecursive = false;
    private boolean verbose = false;
    public ScpFromMessageBySftp(boolean verbose,
                                Session session,
                                String aRemoteFile,
                                File aLocalFile,
                                boolean recursive) {
        this(verbose, session, aRemoteFile, aLocalFile, recursive, false);
    }
    public ScpFromMessageBySftp(Session session,
                                String aRemoteFile,
                                File aLocalFile,
                                boolean recursive) {
        this(false, session, aRemoteFile, aLocalFile, recursive);
    }
    public ScpFromMessageBySftp(boolean verbose,
                                Session session,
                                String aRemoteFile,
                                File aLocalFile,
                                boolean recursive,
                                boolean preserveLastModified) {
        super(verbose, session, aRemoteFile, aLocalFile, recursive,
              preserveLastModified);
        this.verbose = verbose;
        this.remoteFile = aRemoteFile;
        this.localFile = aLocalFile;
        this.isRecursive = recursive;
    }
    public void execute() throws IOException, JSchException {
        ChannelSftp channel = openSftpChannel();
        try {
            channel.connect();
            try {
                SftpATTRS attrs = channel.stat(remoteFile);
                if (attrs.isDir() && !remoteFile.endsWith("/")) {
                    remoteFile = remoteFile + "/";
                }
            } catch (SftpException ee) {
            }
            getDir(channel, remoteFile, localFile);
        } catch (SftpException e) {
            JSchException schException = new JSchException("Could not get '"+ remoteFile
                    +"' to '"+localFile+"' - "
                    +e.toString());
            schException.initCause(e);
            throw schException;
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
        log("done\n");
    }
    private void getDir(ChannelSftp channel,
                        String remoteFile,
                        File localFile) throws IOException, SftpException {
        String pwd = remoteFile;
        if (remoteFile.lastIndexOf('/') != -1) {
            if (remoteFile.length() > 1) {
                pwd = remoteFile.substring(0, remoteFile.lastIndexOf('/'));
            }
        }
        channel.cd(pwd);
        if (!localFile.exists()) {
            localFile.mkdirs();
        }
        java.util.Vector files = channel.ls(remoteFile);
        for (int i = 0; i < files.size(); i++) {
            ChannelSftp.LsEntry le = (ChannelSftp.LsEntry) files.elementAt(i);
            String name = le.getFilename();
            if (le.getAttrs().isDir()) {
                if (name.equals(".") || name.equals("..")) {
                    continue;
                }
                getDir(channel,
                       channel.pwd() + "/" + name + "/",
                       new File(localFile, le.getFilename()));
            } else {
                getFile(channel, le, localFile);
            }
        }
        channel.cd("..");
    }
    private void getFile(ChannelSftp channel,
                         ChannelSftp.LsEntry le,
                         File localFile) throws IOException, SftpException {
        String remoteFile = le.getFilename();
        if (!localFile.exists()) {
            String path = localFile.getAbsolutePath();
            int i = path.lastIndexOf(File.pathSeparator);
            if (i != -1) {
                if (path.length() > File.pathSeparator.length()) {
                    new File(path.substring(0, i)).mkdirs();
                }
            }
        }
        if (localFile.isDirectory()) {
            localFile = new File(localFile, remoteFile);
        }
        long startTime = System.currentTimeMillis();
        long totalLength = le.getAttrs().getSize();
        SftpProgressMonitor monitor = null;
        boolean trackProgress = getVerbose() && totalLength > HUNDRED_KILOBYTES;
        if (trackProgress) {
            monitor = getProgressMonitor();
        }
        try {
            log("Receiving: " + remoteFile + " : " + le.getAttrs().getSize());
            channel.get(remoteFile, localFile.getAbsolutePath(), monitor);
        } finally {
            long endTime = System.currentTimeMillis();
            logStats(startTime, endTime, (int) totalLength);
        }
        if (getPreserveLastModified()) {
            FileUtils.getFileUtils().setFileLastModified(localFile,
                                                         ((long) le.getAttrs()
                                                          .getMTime())
                                                         * 1000);
        }
    }
}
