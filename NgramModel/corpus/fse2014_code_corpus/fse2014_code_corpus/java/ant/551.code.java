package org.apache.tools.ant.taskdefs.optional.ssh;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.ArrayList;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
public class Scp extends SSHBase {
    private static final String[] FROM_ATTRS = {
        "file", "localfile", "remotefile" };
    private static final String[] TO_ATTRS = {
        "todir", "localtodir", "remotetodir", "localtofile", "remotetofile" };
    private String fromUri;
    private String toUri;
    private boolean preserveLastModified = false;
    private List fileSets = null;
    private boolean isFromRemote, isToRemote;
    private boolean isSftp = false;
    public void setFile(String aFromUri) {
        setFromUri(aFromUri);
        this.isFromRemote = isRemoteUri(this.fromUri);
    }
    public void setTodir(String aToUri) {
        setToUri(aToUri);
        this.isToRemote = isRemoteUri(this.toUri);
    }
    public void setLocalFile(String aFromUri) {
        setFromUri(aFromUri);
        this.isFromRemote = false;
    }
    public void setRemoteFile(String aFromUri) {
        validateRemoteUri("remoteFile", aFromUri);
        setFromUri(aFromUri);
        this.isFromRemote = true;
     }
    public void setLocalTodir(String aToUri) {
        setToUri(aToUri);
        this.isToRemote = false;
    }
    public void setPreservelastmodified(boolean yesOrNo) {
    	this.preserveLastModified = yesOrNo;
    }    
    public void setRemoteTodir(String aToUri) {
        validateRemoteUri("remoteToDir", aToUri);
        setToUri(aToUri);
        this.isToRemote = true;
    }
    private static void validateRemoteUri(String type, String aToUri) {
    	if (!isRemoteUri(aToUri)) {
            throw new BuildException(type + " '" + aToUri + "' is invalid. "
                                     + "The 'remoteToDir' attribute must "
                                     + "have syntax like the "
                                     + "following: user:password@host:/path"
                                     + " - the :password part is optional");
    	}
    } 
    public void setLocalTofile(String aToUri) {
        setToUri(aToUri);
        this.isToRemote = false;
    }
    public void setRemoteTofile(String aToUri) {
        validateRemoteUri("remoteToFile", aToUri);
        setToUri(aToUri);
        this.isToRemote = true;
    }
    public void setSftp(boolean yesOrNo) {
        isSftp = yesOrNo;
    }
    public void addFileset(FileSet set) {
        if (fileSets == null) {
            fileSets = new LinkedList();
        }
        fileSets.add(set);
    }
    public void init() throws BuildException {
        super.init();
        this.toUri = null;
        this.fromUri = null;
        this.fileSets = null;
    }
    public void execute() throws BuildException {
        if (toUri == null) {
            throw exactlyOne(TO_ATTRS);
        }
        if (fromUri == null && fileSets == null) {
            throw exactlyOne(FROM_ATTRS, "one or more nested filesets");
        }
        try {
            if (isFromRemote && !isToRemote) {
                download(fromUri, toUri);
            } else if (!isFromRemote && isToRemote) {
                if (fileSets != null) {
                    upload(fileSets, toUri);
                } else {
                    upload(fromUri, toUri);
                }
            } else if (isFromRemote && isToRemote) {
                throw new BuildException(
                    "Copying from a remote server to a remote server is not supported.");
            } else {
                throw new BuildException("'todir' and 'file' attributes "
                    + "must have syntax like the following: "
                    + "user:password@host:/path");
            }
        } catch (Exception e) {
            if (getFailonerror()) {
                if(e instanceof BuildException) {
                    BuildException be = (BuildException) e;
                    if(be.getLocation() == null) {
                        be.setLocation(getLocation());
                    }
                    throw be;
                } else {
                    throw new BuildException(e);
                }
            } else {
                log("Caught exception: " + e.getMessage(), Project.MSG_ERR);
            }
        }
    }
    private void download(String fromSshUri, String toPath)
        throws JSchException, IOException {
        String file = parseUri(fromSshUri);
        Session session = null;
        try {
            session = openSession();
            ScpFromMessage message = null;
            if (!isSftp) {
                message =
                    new ScpFromMessage(getVerbose(), session, file,
                                       getProject().resolveFile(toPath),
                                       fromSshUri.endsWith("*"),
                                       preserveLastModified);
            } else {
                message =
                    new ScpFromMessageBySftp(getVerbose(), session, file,
                                             getProject().resolveFile(toPath),
                                             fromSshUri.endsWith("*"),
                                             preserveLastModified);
            }
            log("Receiving file: " + file);
            message.setLogListener(this);
            message.execute();
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }
    private void upload(List fileSet, String toSshUri)
        throws IOException, JSchException {
        String file = parseUri(toSshUri);
        Session session = null;
        try {
            List list = new ArrayList(fileSet.size());
            for (Iterator i = fileSet.iterator(); i.hasNext();) {
                FileSet set = (FileSet) i.next();
                Directory d = createDirectory(set);
                if (d != null) {
                    list.add(d);
                }
            }
            if (!list.isEmpty()) {
                session = openSession();
                ScpToMessage message = null;
                if (!isSftp) {
                    message = new ScpToMessage(getVerbose(), session,
                                               list, file);
                } else {
                    message = new ScpToMessageBySftp(getVerbose(), session,
                                                     list, file);
                }
                message.setLogListener(this);
                message.execute();
            }
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }
    private void upload(String fromPath, String toSshUri)
        throws IOException, JSchException {
        String file = parseUri(toSshUri);
        Session session = null;
        try {
            session = openSession();
            ScpToMessage message = null;
            if (!isSftp) {
                message =
                    new ScpToMessage(getVerbose(), session,
                                     getProject().resolveFile(fromPath), file);
            } else {
                message =
                    new ScpToMessageBySftp(getVerbose(), session,
                                           getProject().resolveFile(fromPath),
                                           file);
            }
            message.setLogListener(this);
            message.execute();
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }
    private String parseUri(String uri) {
        int indexOfAt = uri.indexOf('@');
        int indexOfColon = uri.indexOf(':');
        if (indexOfColon > -1 && indexOfColon < indexOfAt) {
            int indexOfCurrentAt = indexOfAt;
            int indexOfLastColon = uri.lastIndexOf(':');
            while (indexOfCurrentAt > -1 && indexOfCurrentAt < indexOfLastColon)
            {
                indexOfAt = indexOfCurrentAt;
                indexOfCurrentAt = uri.indexOf('@', indexOfCurrentAt + 1);
            }
            setUsername(uri.substring(0, indexOfColon));
            setPassword(uri.substring(indexOfColon + 1, indexOfAt));
        } else if (indexOfAt > -1) {
            setUsername(uri.substring(0, indexOfAt));
        } else {
            throw new BuildException("no username was given.  Can't authenticate."); 
        }
        if (getUserInfo().getPassword() == null
            && getUserInfo().getKeyfile() == null) {
            throw new BuildException("neither password nor keyfile for user "
                                     + getUserInfo().getName() + " has been "
                                     + "given.  Can't authenticate.");
        }
        int indexOfPath = uri.indexOf(':', indexOfAt + 1);
        if (indexOfPath == -1) {
            throw new BuildException("no remote path in " + uri);
        }
        setHost(uri.substring(indexOfAt + 1, indexOfPath));
        String remotePath = uri.substring(indexOfPath + 1);
        if (remotePath.equals("")) {
            remotePath = ".";
        }
        return remotePath;
    }
    private static boolean isRemoteUri(String uri) {
        boolean isRemote = true;
        int indexOfAt = uri.indexOf('@');
        if (indexOfAt < 0) {
            isRemote = false;
        }
        return isRemote;
    }
    private Directory createDirectory(FileSet set) {
        DirectoryScanner scanner = set.getDirectoryScanner(getProject());
        Directory root = new Directory(scanner.getBasedir());
        String[] files = scanner.getIncludedFiles();
        if (files.length != 0) {
            for (int j = 0; j < files.length; j++) {
                String[] path = Directory.getPath(files[j]);
                Directory current = root;
                File currentParent = scanner.getBasedir();
                for (int i = 0; i < path.length; i++) {
                    File file = new File(currentParent, path[i]);
                    if (file.isDirectory()) {
                        current.addDirectory(new Directory(file));
                        current = current.getChild(file);
                        currentParent = current.getDirectory();
                    } else if (file.isFile()) {
                        current.addFile(file);
                    }
                }
            }
        } else {
            root = null;
        }
        return root;
    }
    private void setFromUri(String fromUri) {
        if (this.fromUri != null) {
            throw exactlyOne(FROM_ATTRS);
        }
        this.fromUri = fromUri;
    }
    private void setToUri(String toUri) {
        if (this.toUri != null) {
            throw exactlyOne(TO_ATTRS);
        }
        this.toUri = toUri;
    }
    private BuildException exactlyOne(String[] attrs) {
        return exactlyOne(attrs, null);
    }
    private BuildException exactlyOne(String[] attrs, String alt) {
        StringBuffer buf = new StringBuffer("Exactly one of ").append(
                '[').append(attrs[0]);
        for (int i = 1; i < attrs.length; i++) {
            buf.append('|').append(attrs[i]);
        }
        buf.append(']');
        if (alt != null) {
            buf.append(" or ").append(alt);
        }
        return new BuildException(buf.append(" is required.").toString());
    }
}
