package org.apache.tools.ant.taskdefs.optional.ssh;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Iterator;
public class ScpToMessageBySftp extends ScpToMessage {
    private static final int HUNDRED_KILOBYTES = 102400;
    private File localFile;
    private String remotePath;
    private List directoryList;
    public ScpToMessageBySftp(boolean verbose,
                              Session session,
                              File aLocalFile,
                              String aRemotePath) {
        this(verbose, session, aRemotePath);
        this.localFile = aLocalFile;
    }
    public ScpToMessageBySftp(boolean verbose,
                              Session session,
                              List aDirectoryList,
                              String aRemotePath) {
        this(verbose, session, aRemotePath);
        this.directoryList = aDirectoryList;
    }
    private ScpToMessageBySftp(boolean verbose,
                               Session session,
                               String aRemotePath) {
        super(verbose, session);
        this.remotePath = aRemotePath;
    }
    public ScpToMessageBySftp(Session session,
                              File aLocalFile,
                              String aRemotePath) {
        this(false, session, aLocalFile, aRemotePath);
    }
    public ScpToMessageBySftp(Session session,
                              List aDirectoryList,
                              String aRemotePath) {
        this(false, session, aDirectoryList, aRemotePath);
    }
    public void execute() throws IOException, JSchException {
        if (directoryList != null) {
            doMultipleTransfer();
        }
        if (localFile != null) {
            doSingleTransfer();
        }
        log("done.\n");
    }
    private void doSingleTransfer() throws IOException, JSchException {
        ChannelSftp channel = openSftpChannel();
        try {
            channel.connect();
            try {
                sendFileToRemote(channel, localFile, remotePath);
            } catch (SftpException e) {
                JSchException schException = new JSchException("Could not send '" + localFile
                        + "' to '" + remotePath + "' - "
                        + e.toString());
                schException.initCause(e);
                throw schException;
            }
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }
    private void doMultipleTransfer() throws IOException, JSchException {
        ChannelSftp channel = openSftpChannel();
        try {
            channel.connect();
            try {
                try {
                    channel.stat(remotePath);
                } catch (SftpException e) {
                    if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                        channel.mkdir(remotePath);
                    } else {
                        throw new JSchException("failed to access remote dir '"
                                                + remotePath + "'", e);
                    }
                }
                channel.cd(remotePath);
            } catch (SftpException e) {
                throw new JSchException("Could not CD to '" + remotePath
                                        + "' - " + e.toString(), e);
            }
            Directory current = null;
            try {
                for (Iterator i = directoryList.iterator(); i.hasNext();) {
                    current = (Directory) i.next();
                    if (getVerbose()) {
                        log("Sending directory " + current);
                    }
                    sendDirectory(channel, current);
                }
            } catch (SftpException e) {
                String msg = "Error sending directory";
                if (current != null && current.getDirectory() != null) {
                    msg += " '" + current.getDirectory().getName() + "'";
                }
                throw new JSchException(msg, e);
            }
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }
    private void sendDirectory(ChannelSftp channel,
                               Directory current)
        throws IOException, SftpException {
        for (Iterator fileIt = current.filesIterator(); fileIt.hasNext();) {
            sendFileToRemote(channel, (File) fileIt.next(), null);
        }
        for (Iterator dirIt = current.directoryIterator(); dirIt.hasNext();) {
            Directory dir = (Directory) dirIt.next();
            sendDirectoryToRemote(channel, dir);
        }
    }
    private void sendDirectoryToRemote(ChannelSftp channel,
                                       Directory directory)
        throws IOException, SftpException {
        String dir = directory.getDirectory().getName();
        try {
            channel.stat(dir);
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                channel.mkdir(dir);
            }
        }
        channel.cd(dir);
        sendDirectory(channel, directory);
        channel.cd("..");
    }
    private void sendFileToRemote(ChannelSftp channel,
                                  File localFile,
                                  String remotePath)
        throws IOException, SftpException {
        long filesize = localFile.length();
        if (remotePath == null) {
            remotePath = localFile.getName();
        }
        long startTime = System.currentTimeMillis();
        long totalLength = filesize;
        boolean trackProgress = getVerbose() && filesize > HUNDRED_KILOBYTES;
        SftpProgressMonitor monitor = null;
        if (trackProgress) {
            monitor = getProgressMonitor();
        }
        try {
            if (this.getVerbose()) {
                log("Sending: " + localFile.getName() + " : " + filesize);
            }
            channel.put(localFile.getAbsolutePath(), remotePath, monitor);
        } finally {
            if (this.getVerbose()) {
                long endTime = System.currentTimeMillis();
                logStats(startTime, endTime, (int) totalLength);
            }
        }
    }
    public File getLocalFile() {
        return localFile;
    }
    public String getRemotePath() {
        return remotePath;
    }
}
