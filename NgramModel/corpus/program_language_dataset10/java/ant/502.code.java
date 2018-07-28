package org.apache.tools.ant.taskdefs.optional.net;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.RetryHandler;
import org.apache.tools.ant.util.Retryable;
import org.apache.tools.ant.util.VectorSet;
public class FTP extends Task implements FTPTaskConfig {
    protected static final int SEND_FILES = 0;
    protected static final int GET_FILES = 1;
    protected static final int DEL_FILES = 2;
    protected static final int LIST_FILES = 3;
    protected static final int MK_DIR = 4;
    protected static final int CHMOD = 5;
    protected static final int RM_DIR = 6;
    protected static final int SITE_CMD = 7;
    private static final int CODE_521 = 521;
    private static final long GRANULARITY_MINUTE = 60000L;
    private static final SimpleDateFormat TIMESTAMP_LOGGING_SDF =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final int DEFAULT_FTP_PORT = 21;
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private String remotedir;
    private String server;
    private String userid;
    private String password;
    private String account;
    private File listing;
    private boolean binary = true;
    private boolean passive = false;
    private boolean verbose = false;
    private boolean newerOnly = false;
    private long timeDiffMillis = 0;
    private long granularityMillis = 0L;
    private boolean timeDiffAuto = false;
    private int action = SEND_FILES;
    private Vector filesets = new Vector();
    private Set dirCache = new HashSet();
    private int transferred = 0;
    private String remoteFileSep = "/";
    private int port = DEFAULT_FTP_PORT;
    private boolean skipFailedTransfers = false;
    private int skipped = 0;
    private boolean ignoreNoncriticalErrors = false;
    private boolean preserveLastModified = false;
    private String chmod = null;
    private String umask = null;
    private FTPSystemType systemTypeKey = FTPSystemType.getDefault();
    private String defaultDateFormatConfig = null;
    private String recentDateFormatConfig = null;
    private LanguageCode serverLanguageCodeConfig = LanguageCode.getDefault();
    private String serverTimeZoneConfig = null;
    private String shortMonthNamesConfig = null;
    private Granularity timestampGranularity = Granularity.getDefault();
    private boolean isConfigurationSet = false;
    private int retriesAllowed = 0;
    private String siteCommand = null;
    private String initialSiteCommand = null;
    private boolean enableRemoteVerification = true;
    protected static final String[] ACTION_STRS = {
        "sending",
        "getting",
        "deleting",
        "listing",
        "making directory",
        "chmod",
        "removing",
        "site"
    };
    protected static final String[] COMPLETED_ACTION_STRS = {
        "sent",
        "retrieved",
        "deleted",
        "listed",
        "created directory",
        "mode changed",
        "removed",
        "site command executed"
    };
    protected static final String[] ACTION_TARGET_STRS = {
        "files",
        "files",
        "files",
        "files",
        "directory",
        "files",
        "directories",
        "site command"
    };
    protected static class FTPFileProxy extends File {
        private final FTPFile file;
        private final String[] parts;
        private final String name;
        public FTPFileProxy(FTPFile file) {
            super(file.getName());
            name = file.getName();
            this.file = file;
            parts = FileUtils.getPathStack(name);
        }
        public FTPFileProxy(String completePath) {
            super(completePath);
            file = null;
            name = completePath;
            parts = FileUtils.getPathStack(completePath);
        }
        public boolean exists() {
            return true;
        }
        public String getAbsolutePath() {
            return name;
        }
        public String getName() {
            return parts.length > 0 ? parts[parts.length - 1] : name;
        }
        public String getParent() {
            String result = "";
            for(int i = 0; i < parts.length - 1; i++){
                result += File.separatorChar + parts[i];
            }
            return result;
        }
        public String getPath() {
            return name;
        }
        public boolean isAbsolute() {
            return true;
        }
        public boolean isDirectory() {
            return file == null;
        }
        public boolean isFile() {
            return file != null;
        }
        public boolean isHidden() {
            return false;
        }
        public long lastModified() {
            if (file != null) {
                return file.getTimestamp().getTimeInMillis();
            }
            return 0;
        }
        public long length() {
            if (file != null) {
                return file.getSize();
            }
            return 0;
        }
    }
    protected class FTPDirectoryScanner extends DirectoryScanner {
        protected FTPClient ftp = null;
        private String rootPath = null;
        private boolean remoteSystemCaseSensitive = false;
        private boolean remoteSensitivityChecked = false;
        public FTPDirectoryScanner(FTPClient ftp) {
            super();
            this.ftp = ftp;
            this.setFollowSymlinks(false);
        }
        public void scan() {
            if (includes == null) {
                includes = new String[1];
                includes[0] = "**";
            }
            if (excludes == null) {
                excludes = new String[0];
            }
            filesIncluded = new VectorSet();
            filesNotIncluded = new Vector();
            filesExcluded = new VectorSet();
            dirsIncluded = new VectorSet();
            dirsNotIncluded = new Vector();
            dirsExcluded = new VectorSet();
            try {
                String cwd = ftp.printWorkingDirectory();
                forceRemoteSensitivityCheck();
                checkIncludePatterns();
                clearCaches();
                ftp.changeWorkingDirectory(cwd);
            } catch (IOException e) {
                throw new BuildException("Unable to scan FTP server: ", e);
            }
        }
        private void checkIncludePatterns() {
            Hashtable newroots = new Hashtable();
            for (int icounter = 0; icounter < includes.length; icounter++) {
                String newpattern =
                    SelectorUtils.rtrimWildcardTokens(includes[icounter]);
                newroots.put(newpattern, includes[icounter]);
            }
            if (remotedir == null) {
                try {
                    remotedir = ftp.printWorkingDirectory();
                } catch (IOException e) {
                    throw new BuildException("could not read current ftp directory",
                                             getLocation());
                }
            }
            AntFTPFile baseFTPFile = new AntFTPRootFile(ftp, remotedir);
            rootPath = baseFTPFile.getAbsolutePath();
            if (newroots.containsKey("")) {
                scandir(rootPath, "", true);
            } else {
                Enumeration enum2 = newroots.keys();
                while (enum2.hasMoreElements()) {
                    String currentelement = (String) enum2.nextElement();
                    String originalpattern = (String) newroots.get(currentelement);
                    AntFTPFile myfile = new AntFTPFile(baseFTPFile, currentelement);
                    boolean isOK = true;
                    boolean traversesSymlinks = false;
                    String path = null;
                    if (myfile.exists()) {
                        forceRemoteSensitivityCheck();
                        if (remoteSensitivityChecked
                            && remoteSystemCaseSensitive && isFollowSymlinks()) {
                            path = myfile.getFastRelativePath();
                        } else {
                            try {
                                path = myfile.getRelativePath();
                                traversesSymlinks = myfile.isTraverseSymlinks();
                            }  catch (IOException be) {
                                throw new BuildException(be, getLocation());
                            } catch (BuildException be) {
                                isOK = false;
                            }
                        }
                    } else {
                        isOK = false;
                    }
                    if (isOK) {
                        currentelement = path.replace(remoteFileSep.charAt(0), File.separatorChar);
                        if (!isFollowSymlinks()
                            && traversesSymlinks) {
                            continue;
                        }
                        if (myfile.isDirectory()) {
                            if (isIncluded(currentelement)
                                && currentelement.length() > 0) {
                                accountForIncludedDir(currentelement, myfile, true);
                            }  else {
                                if (currentelement.length() > 0) {
                                    if (currentelement.charAt(currentelement
                                                              .length() - 1)
                                        != File.separatorChar) {
                                        currentelement =
                                            currentelement + File.separatorChar;
                                    }
                                }
                                scandir(myfile.getAbsolutePath(), currentelement, true);
                            }
                        } else {
                            if (isCaseSensitive
                                && originalpattern.equals(currentelement)) {
                                accountForIncludedFile(currentelement);
                            } else if (!isCaseSensitive
                                       && originalpattern
                                       .equalsIgnoreCase(currentelement)) {
                                accountForIncludedFile(currentelement);
                            }
                        }
                    }
                }
            }
        }
        protected void scandir(String dir, String vpath, boolean fast) {
            if (fast && hasBeenScanned(vpath)) {
                return;
            }
            try {
                if (!ftp.changeWorkingDirectory(dir)) {
                    return;
                }
                String completePath = null;
                if (!vpath.equals("")) {
                    completePath = rootPath + remoteFileSep
                        + vpath.replace(File.separatorChar, remoteFileSep.charAt(0));
                } else {
                    completePath = rootPath;
                }
                FTPFile[] newfiles = listFiles(completePath, false);
                if (newfiles == null) {
                    ftp.changeToParentDirectory();
                    return;
                }
                for (int i = 0; i < newfiles.length; i++) {
                    FTPFile file = newfiles[i];
                    if (file != null
                        && !file.getName().equals(".")
                        && !file.getName().equals("..")) {
                        String name = vpath + file.getName();
                        scannedDirs.put(name, new FTPFileProxy(file));
                        if (isFunctioningAsDirectory(ftp, dir, file)) {
                            boolean slowScanAllowed = true;
                            if (!isFollowSymlinks() && file.isSymbolicLink()) {
                                dirsExcluded.addElement(name);
                                slowScanAllowed = false;
                            } else if (isIncluded(name)) {
                                accountForIncludedDir(name,
                                                      new AntFTPFile(ftp, file, completePath) , fast);
                            } else {
                                dirsNotIncluded.addElement(name);
                                if (fast && couldHoldIncluded(name)) {
                                    scandir(file.getName(),
                                            name + File.separator, fast);
                                }
                            }
                            if (!fast && slowScanAllowed) {
                                scandir(file.getName(),
                                        name + File.separator, fast);
                            }
                        } else {
                            if (!isFollowSymlinks() && file.isSymbolicLink()) {
                                filesExcluded.addElement(name);
                            } else if (isFunctioningAsFile(ftp, dir, file)) {
                                accountForIncludedFile(name);
                            }
                        }
                    }
                }
                ftp.changeToParentDirectory();
            } catch (IOException e) {
                throw new BuildException("Error while communicating with FTP "
                                         + "server: ", e);
            }
        }
        private void accountForIncludedFile(String name) {
            if (!filesIncluded.contains(name)
                && !filesExcluded.contains(name)) {
                if (isIncluded(name)) {
                    if (!isExcluded(name)
                        && isSelected(name, (File) scannedDirs.get(name))) {
                        filesIncluded.addElement(name);
                    } else {
                        filesExcluded.addElement(name);
                    }
                } else {
                    filesNotIncluded.addElement(name);
                }
            }
        }
        private void accountForIncludedDir(String name, AntFTPFile file, boolean fast) {
            if (!dirsIncluded.contains(name)
                && !dirsExcluded.contains(name)) {
                if (!isExcluded(name)) {
                    if (fast) {
                        if (file.isSymbolicLink()) {
                            try {
                                file.getClient().changeWorkingDirectory(file.curpwd);
                            } catch (IOException ioe) {
                                throw new BuildException("could not change directory to curpwd");
                            }
                            scandir(file.getLink(),
                                    name + File.separator, fast);
                        } else {
                            try {
                                file.getClient().changeWorkingDirectory(file.curpwd);
                            } catch (IOException ioe) {
                                throw new BuildException("could not change directory to curpwd");
                            }
                            scandir(file.getName(),
                                    name + File.separator, fast);
                        }
                    }
                    dirsIncluded.addElement(name);
                } else {
                    dirsExcluded.addElement(name);
                    if (fast && couldHoldIncluded(name)) {
                        try {
                            file.getClient().changeWorkingDirectory(file.curpwd);
                        } catch (IOException ioe) {
                            throw new BuildException("could not change directory to curpwd");
                        }
                        scandir(file.getName(),
                                name + File.separator, fast);
                    }
                }
            }
        }
        private Map fileListMap = new HashMap();
        private Map scannedDirs = new HashMap();
        private boolean hasBeenScanned(String vpath) {
            return scannedDirs.containsKey(vpath);
        }
        private void clearCaches() {
            fileListMap.clear();
            scannedDirs.clear();
        }
        public FTPFile[] listFiles(String directory, boolean changedir) {
            String currentPath = directory;
            if (changedir) {
                try {
                    boolean result = ftp.changeWorkingDirectory(directory);
                    if (!result) {
                        return null;
                    }
                    currentPath = ftp.printWorkingDirectory();
                } catch (IOException ioe) {
                    throw new BuildException(ioe, getLocation());
                }
            }
            if (fileListMap.containsKey(currentPath)) {
                getProject().log("filelist map used in listing files", Project.MSG_DEBUG);
                return ((FTPFile[]) fileListMap.get(currentPath));
            }
            FTPFile[] result = null;
            try {
                result = ftp.listFiles();
            } catch (IOException ioe) {
                throw new BuildException(ioe, getLocation());
            }
            fileListMap.put(currentPath, result);
            if (!remoteSensitivityChecked) {
                checkRemoteSensitivity(result, directory);
            }
            return result;
        }
        private void forceRemoteSensitivityCheck() {
            if (!remoteSensitivityChecked) {
                try {
                    checkRemoteSensitivity(ftp.listFiles(), ftp.printWorkingDirectory());
                } catch (IOException ioe) {
                    throw new BuildException(ioe, getLocation());
                }
            }
        }
        public FTPFile[] listFiles(String directory) {
            return listFiles(directory, true);
        }
        private void checkRemoteSensitivity(FTPFile[] array, String directory) {
            if (array == null) {
                return;
            }
            boolean candidateFound = false;
            String target = null;
            for (int icounter = 0; icounter < array.length; icounter++) {
                if (array[icounter] != null && array[icounter].isDirectory()) {
                    if (!array[icounter].getName().equals(".")
                        && !array[icounter].getName().equals("..")) {
                        candidateFound = true;
                        target = fiddleName(array[icounter].getName());
                        getProject().log("will try to cd to "
                                         + target + " where a directory called " + array[icounter].getName()
                                         + " exists", Project.MSG_DEBUG);
                        for (int pcounter = 0; pcounter < array.length; pcounter++) {
                            if (array[pcounter] != null
                                && pcounter != icounter
                                && target.equals(array[pcounter].getName())) {
                                candidateFound = false;
                            }
                        }
                        if (candidateFound) {
                            break;
                        }
                    }
                }
            }
            if (candidateFound) {
                try {
                    getProject().log("testing case sensitivity, attempting to cd to "
                                     + target, Project.MSG_DEBUG);
                    remoteSystemCaseSensitive  = !ftp.changeWorkingDirectory(target);
                } catch (IOException ioe) {
                    remoteSystemCaseSensitive = true;
                } finally {
                    try {
                        ftp.changeWorkingDirectory(directory);
                    } catch (IOException ioe) {
                        throw new BuildException(ioe, getLocation());
                    }
                }
                getProject().log("remote system is case sensitive : " + remoteSystemCaseSensitive,
                                 Project.MSG_VERBOSE);
                remoteSensitivityChecked = true;
            }
        }
        private String fiddleName(String origin) {
            StringBuffer result = new StringBuffer();
            for (int icounter = 0; icounter < origin.length(); icounter++) {
                if (Character.isLowerCase(origin.charAt(icounter))) {
                    result.append(Character.toUpperCase(origin.charAt(icounter)));
                } else if (Character.isUpperCase(origin.charAt(icounter))) {
                    result.append(Character.toLowerCase(origin.charAt(icounter)));
                } else {
                    result.append(origin.charAt(icounter));
                }
            }
            return result.toString();
        }
        protected class AntFTPFile {
            private FTPClient client;
            private String curpwd;
            private FTPFile ftpFile;
            private AntFTPFile parent = null;
            private boolean relativePathCalculated = false;
            private boolean traversesSymlinks = false;
            private String relativePath = "";
            public AntFTPFile(FTPClient client, FTPFile ftpFile, String curpwd) {
                this.client = client;
                this.ftpFile = ftpFile;
                this.curpwd = curpwd;
            }
            public AntFTPFile(AntFTPFile parent, String path) {
                this.parent = parent;
                this.client = parent.client;
                Vector pathElements = SelectorUtils.tokenizePath(path);
                try {
                    boolean result = this.client.changeWorkingDirectory(parent.getAbsolutePath());
                    if (!result) {
                        return;
                    }
                    this.curpwd = parent.getAbsolutePath();
                } catch (IOException ioe) {
                    throw new BuildException("could not change working dir to "
                                             + parent.curpwd);
                }
                for (int fcount = 0; fcount < pathElements.size() - 1; fcount++) {
                    String currentPathElement = (String) pathElements.elementAt(fcount);
                    try {
                        boolean result = this.client.changeWorkingDirectory(currentPathElement);
                        if (!result && !isCaseSensitive()
                            && (remoteSystemCaseSensitive || !remoteSensitivityChecked)) {
                            currentPathElement = findPathElementCaseUnsensitive(this.curpwd,
                                                                                currentPathElement);
                            if (currentPathElement == null) {
                                return;
                            }
                        } else if (!result) {
                            return;
                        }
                        this.curpwd = getCurpwdPlusFileSep()
                            + currentPathElement;
                    } catch (IOException ioe) {
                        throw new BuildException("could not change working dir to "
                                                 + (String) pathElements.elementAt(fcount)
                                                 + " from " + this.curpwd);
                    }
                }
                String lastpathelement = (String) pathElements.elementAt(pathElements.size() - 1);
                FTPFile [] theFiles = listFiles(this.curpwd);
                this.ftpFile = getFile(theFiles, lastpathelement);
            }
            private String findPathElementCaseUnsensitive(String parentPath,
                                                          String soughtPathElement) {
                FTPFile[] theFiles = listFiles(parentPath, false);
                if (theFiles == null) {
                    return null;
                }
                for (int icounter = 0; icounter < theFiles.length; icounter++) {
                    if (theFiles[icounter] != null
                        && theFiles[icounter].getName().equalsIgnoreCase(soughtPathElement)) {
                        return theFiles[icounter].getName();
                    }
                }
                return null;
            }
            public boolean exists() {
                return (ftpFile != null);
            }
            public String getLink() {
                return ftpFile.getLink();
            }
            public String getName() {
                return ftpFile.getName();
            }
            public String getAbsolutePath() {
                return getCurpwdPlusFileSep() + ftpFile.getName();
            }
            public String getFastRelativePath() {
                String absPath = getAbsolutePath();
                if (absPath.indexOf(rootPath + remoteFileSep) == 0) {
                    return absPath.substring(rootPath.length() + remoteFileSep.length());
                }
                return null;
            }
            public String getRelativePath() throws IOException, BuildException {
                if (!relativePathCalculated) {
                    if (parent != null) {
                        traversesSymlinks = parent.isTraverseSymlinks();
                        relativePath = getRelativePath(parent.getAbsolutePath(),
                                                       parent.getRelativePath());
                    } else {
                        relativePath = getRelativePath(rootPath, "");
                        relativePathCalculated = true;
                    }
                }
                return relativePath;
            }
            private String getRelativePath(String currentPath, String currentRelativePath) {
                Vector pathElements = SelectorUtils.tokenizePath(getAbsolutePath(), remoteFileSep);
                Vector pathElements2 = SelectorUtils.tokenizePath(currentPath, remoteFileSep);
                String relPath = currentRelativePath;
                for (int pcount = pathElements2.size(); pcount < pathElements.size(); pcount++) {
                    String currentElement = (String) pathElements.elementAt(pcount);
                    FTPFile[] theFiles = listFiles(currentPath);
                    FTPFile theFile = null;
                    if (theFiles != null) {
                        theFile = getFile(theFiles, currentElement);
                    }
                    if (!relPath.equals("")) {
                        relPath = relPath + remoteFileSep;
                    }
                    if (theFile == null) {
                        relPath = relPath + currentElement;
                        currentPath = currentPath + remoteFileSep + currentElement;
                        log("Hidden file " + relPath
                            + " assumed to not be a symlink.",
                            Project.MSG_VERBOSE);
                    } else {
                        traversesSymlinks = traversesSymlinks || theFile.isSymbolicLink();
                        relPath = relPath + theFile.getName();
                        currentPath = currentPath + remoteFileSep + theFile.getName();
                    }
                }
                return relPath;
            }
            public FTPFile getFile(FTPFile[] theFiles, String lastpathelement) {
                if (theFiles == null) {
                    return null;
                }
                for (int fcount = 0; fcount < theFiles.length; fcount++) {
                    if (theFiles[fcount] != null) {
                        if (theFiles[fcount].getName().equals(lastpathelement)) {
                            return theFiles[fcount];
                        } else if (!isCaseSensitive()
                                   && theFiles[fcount].getName().equalsIgnoreCase(
                                                                                  lastpathelement)) {
                            return theFiles[fcount];
                        }
                    }
                }
                return null;
            }
            public boolean isDirectory() {
                return ftpFile.isDirectory();
            }
            public boolean isSymbolicLink() {
                return ftpFile.isSymbolicLink();
            }
            protected FTPClient getClient() {
                return client;
            }
            protected void setCurpwd(String curpwd) {
                this.curpwd = curpwd;
            }
            public String getCurpwd() {
                return curpwd;
            }
            public String getCurpwdPlusFileSep() {
                return curpwd.endsWith(remoteFileSep) ? curpwd
                    : curpwd + remoteFileSep;
            }
            public boolean isTraverseSymlinks() throws IOException, BuildException {
                if (!relativePathCalculated) {
                    getRelativePath();
                }
                return traversesSymlinks;
            }
            public String toString() {
                return "AntFtpFile: " + curpwd + "%" + ftpFile;
            }
        }
        protected class AntFTPRootFile extends AntFTPFile {
            private String remotedir;
            public AntFTPRootFile(FTPClient aclient, String remotedir) {
                super(aclient, null, remotedir);
                this.remotedir = remotedir;
                try {
                    this.getClient().changeWorkingDirectory(this.remotedir);
                    this.setCurpwd(this.getClient().printWorkingDirectory());
                } catch (IOException ioe) {
                    throw new BuildException(ioe, getLocation());
                }
            }
            public String getAbsolutePath() {
                return this.getCurpwd();
            }
            public String getRelativePath() throws BuildException, IOException {
                return "";
            }
        }
    }
    private boolean isFunctioningAsDirectory(FTPClient ftp, String dir, FTPFile file) {
        boolean result = false;
        String currentWorkingDir = null;
        if (file.isDirectory()) {
            return true;
        } else if (file.isFile()) {
            return false;
        }
        try {
            currentWorkingDir = ftp.printWorkingDirectory();
        } catch (IOException ioe) {
            getProject().log("could not find current working directory " + dir
                             + " while checking a symlink",
                             Project.MSG_DEBUG);
        }
        if (currentWorkingDir != null) {
            try {
                result = ftp.changeWorkingDirectory(file.getLink());
            } catch (IOException ioe) {
                getProject().log("could not cd to " + file.getLink() + " while checking a symlink",
                                 Project.MSG_DEBUG);
            }
            if (result) {
                boolean comeback = false;
                try {
                    comeback = ftp.changeWorkingDirectory(currentWorkingDir);
                } catch (IOException ioe) {
                    getProject().log("could not cd back to " + dir + " while checking a symlink",
                                     Project.MSG_ERR);
                } finally {
                    if (!comeback) {
                        throw new BuildException("could not cd back to " + dir
                                                 + " while checking a symlink");
                    }
                }
            }
        }
        return result;
    }
    private boolean isFunctioningAsFile(FTPClient ftp, String dir, FTPFile file) {
        if (file.isDirectory()) {
            return false;
        } else if (file.isFile()) {
            return true;
        }
        return !isFunctioningAsDirectory(ftp, dir, file);
    }
    public void setRemotedir(String dir) {
        this.remotedir = dir;
    }
    public void setServer(String server) {
        this.server = server;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public void setUserid(String userid) {
        this.userid = userid;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setAccount(String pAccount) {
        this.account = pAccount;
    }
    public void setBinary(boolean binary) {
        this.binary = binary;
    }
    public void setPassive(boolean passive) {
        this.passive = passive;
    }
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    public void setNewer(boolean newer) {
        this.newerOnly = newer;
    }
    public void setTimeDiffMillis(long timeDiffMillis) {
        this.timeDiffMillis = timeDiffMillis;
    }
    public void setTimeDiffAuto(boolean timeDiffAuto) {
        this.timeDiffAuto = timeDiffAuto;
    }
    public void setPreserveLastModified(boolean preserveLastModified) {
        this.preserveLastModified = preserveLastModified;
    }
    public void setDepends(boolean depends) {
        this.newerOnly = depends;
    }
    public void setSeparator(String separator) {
        remoteFileSep = separator;
    }
    public void setChmod(String theMode) {
        this.chmod = theMode;
    }
    public void setUmask(String theUmask) {
        this.umask = theUmask;
    }
    public void addFileset(FileSet set) {
        filesets.addElement(set);
    }
    public void setAction(String action) throws BuildException {
        log("DEPRECATED - The setAction(String) method has been deprecated."
            + " Use setAction(FTP.Action) instead.");
        Action a = new Action();
        a.setValue(action);
        this.action = a.getAction();
    }
    public void setAction(Action action) throws BuildException {
        this.action = action.getAction();
    }
    public void setListing(File listing) {
        this.listing = listing;
    }
    public void setSkipFailedTransfers(boolean skipFailedTransfers) {
        this.skipFailedTransfers = skipFailedTransfers;
    }
    public void setIgnoreNoncriticalErrors(boolean ignoreNoncriticalErrors) {
        this.ignoreNoncriticalErrors = ignoreNoncriticalErrors;
    }
    private void configurationHasBeenSet() {
        this.isConfigurationSet = true;
    }
    public void setSystemTypeKey(FTPSystemType systemKey) {
        if (systemKey != null && !systemKey.getValue().equals("")) {
            this.systemTypeKey = systemKey;
            configurationHasBeenSet();
        }
    }
    public void setDefaultDateFormatConfig(String defaultDateFormat) {
        if (defaultDateFormat != null && !defaultDateFormat.equals("")) {
            this.defaultDateFormatConfig = defaultDateFormat;
            configurationHasBeenSet();
        }
    }
    public void setRecentDateFormatConfig(String recentDateFormat) {
        if (recentDateFormat != null && !recentDateFormat.equals("")) {
            this.recentDateFormatConfig = recentDateFormat;
            configurationHasBeenSet();
        }
    }
    public void setServerLanguageCodeConfig(LanguageCode serverLanguageCode) {
        if (serverLanguageCode != null && !"".equals(serverLanguageCode.getValue())) {
            this.serverLanguageCodeConfig = serverLanguageCode;
            configurationHasBeenSet();
        }
    }
    public void setServerTimeZoneConfig(String serverTimeZoneId) {
        if (serverTimeZoneId != null && !serverTimeZoneId.equals("")) {
            this.serverTimeZoneConfig = serverTimeZoneId;
            configurationHasBeenSet();
        }
    }
    public void setShortMonthNamesConfig(String shortMonthNames) {
        if (shortMonthNames != null && !shortMonthNames.equals("")) {
            this.shortMonthNamesConfig = shortMonthNames;
            configurationHasBeenSet();
        }
    }
    public void setRetriesAllowed(String retriesAllowed) {
        if ("FOREVER".equalsIgnoreCase(retriesAllowed)) {
            this.retriesAllowed = Retryable.RETRY_FOREVER;
        } else {
            try {
                int retries = Integer.parseInt(retriesAllowed);
                if (retries < Retryable.RETRY_FOREVER) {
                    throw new BuildException(
                                             "Invalid value for retriesAllowed attribute: "
                                             + retriesAllowed);
                }
                this.retriesAllowed = retries;
            } catch (NumberFormatException px) {
                throw new BuildException(
                                         "Invalid value for retriesAllowed attribute: "
                                         + retriesAllowed);
            }
        }
    }
    public String getSystemTypeKey() {
        return systemTypeKey.getValue();
    }
    public String getDefaultDateFormatConfig() {
        return defaultDateFormatConfig;
    }
    public String getRecentDateFormatConfig() {
        return recentDateFormatConfig;
    }
    public String getServerLanguageCodeConfig() {
        return serverLanguageCodeConfig.getValue();
    }
    public String getServerTimeZoneConfig() {
        return serverTimeZoneConfig;
    }
    public String getShortMonthNamesConfig() {
        return shortMonthNamesConfig;
    }
    Granularity getTimestampGranularity() {
        return timestampGranularity;
    }
    public void setTimestampGranularity(Granularity timestampGranularity) {
        if (null == timestampGranularity || "".equals(timestampGranularity.getValue())) {
            return;
        }
        this.timestampGranularity = timestampGranularity;
    }
    public void setSiteCommand(String siteCommand) {
        this.siteCommand = siteCommand;
    }
    public void setInitialSiteCommand(String initialCommand) {
        this.initialSiteCommand = initialCommand;
    }
    public void setEnableRemoteVerification(boolean b) {
        enableRemoteVerification = b;
    }
    protected void checkAttributes() throws BuildException {
        if (server == null) {
            throw new BuildException("server attribute must be set!");
        }
        if (userid == null) {
            throw new BuildException("userid attribute must be set!");
        }
        if (password == null) {
            throw new BuildException("password attribute must be set!");
        }
        if ((action == LIST_FILES) && (listing == null)) {
            throw new BuildException("listing attribute must be set for list "
                                     + "action!");
        }
        if (action == MK_DIR && remotedir == null) {
            throw new BuildException("remotedir attribute must be set for "
                                     + "mkdir action!");
        }
        if (action == CHMOD && chmod == null) {
            throw new BuildException("chmod attribute must be set for chmod "
                                     + "action!");
        }
        if (action == SITE_CMD && siteCommand == null) {
            throw new BuildException("sitecommand attribute must be set for site "
                                     + "action!");
        }
        if (this.isConfigurationSet) {
            try {
                Class.forName("org.apache.commons.net.ftp.FTPClientConfig");
            } catch (ClassNotFoundException e) {
                throw new BuildException(
                                         "commons-net.jar >= 1.4.0 is required for at least one"
                                         + " of the attributes specified.");
            }
        }
    }
    protected void executeRetryable(RetryHandler h, Retryable r, String descr)
        throws IOException {
        h.execute(r, descr);
    }
    protected int transferFiles(final FTPClient ftp, FileSet fs)
        throws IOException, BuildException {
        DirectoryScanner ds;
        if (action == SEND_FILES) {
            ds = fs.getDirectoryScanner(getProject());
        } else {
            ds = new FTPDirectoryScanner(ftp);
            fs.setupDirectoryScanner(ds, getProject());
            ds.setFollowSymlinks(fs.isFollowSymlinks());
            ds.scan();
        }
        String[] dsfiles = null;
        if (action == RM_DIR) {
            dsfiles = ds.getIncludedDirectories();
        } else {
            dsfiles = ds.getIncludedFiles();
        }
        String dir = null;
        if ((ds.getBasedir() == null)
            && ((action == SEND_FILES) || (action == GET_FILES))) {
            throw new BuildException("the dir attribute must be set for send "
                                     + "and get actions");
        } else {
            if ((action == SEND_FILES) || (action == GET_FILES)) {
                dir = ds.getBasedir().getAbsolutePath();
            }
        }
        BufferedWriter bw = null;
        try {
            if (action == LIST_FILES) {
                File pd = listing.getParentFile();
                if (!pd.exists()) {
                    pd.mkdirs();
                }
                bw = new BufferedWriter(new FileWriter(listing));
            }
            RetryHandler h = new RetryHandler(this.retriesAllowed, this);
            if (action == RM_DIR) {
                for (int i = dsfiles.length - 1; i >= 0; i--) {
                    final String dsfile = dsfiles[i];
                    executeRetryable(h, new Retryable() {
                            public void execute() throws IOException {
                                rmDir(ftp, dsfile);
                            }
                        }, dsfile);
                }
            } else {
                final BufferedWriter fbw = bw;
                final String fdir = dir;
                if (this.newerOnly) {
                    this.granularityMillis =
                        this.timestampGranularity.getMilliseconds(action);
                }
                for (int i = 0; i < dsfiles.length; i++) {
                    final String dsfile = dsfiles[i];
                    executeRetryable(h, new Retryable() {
                            public void execute() throws IOException {
                                switch (action) {
                                case SEND_FILES:
                                    sendFile(ftp, fdir, dsfile);
                                    break;
                                case GET_FILES:
                                    getFile(ftp, fdir, dsfile);
                                    break;
                                case DEL_FILES:
                                    delFile(ftp, dsfile);
                                    break;
                                case LIST_FILES:
                                    listFile(ftp, fbw, dsfile);
                                    break;
                                case CHMOD:
                                    doSiteCommand(ftp, "chmod " + chmod
                                                  + " " + resolveFile(dsfile));
                                    transferred++;
                                    break;
                                default:
                                    throw new BuildException("unknown ftp action " + action);
                                }
                            }
                        }, dsfile);
                }
            }
        } finally {
            FileUtils.close(bw);
        }
        return dsfiles.length;
    }
    protected void transferFiles(FTPClient ftp)
        throws IOException, BuildException {
        transferred = 0;
        skipped = 0;
        if (filesets.size() == 0) {
            throw new BuildException("at least one fileset must be specified.");
        } else {
            for (int i = 0; i < filesets.size(); i++) {
                FileSet fs = (FileSet) filesets.elementAt(i);
                if (fs != null) {
                    transferFiles(ftp, fs);
                }
            }
        }
        log(transferred + " " + ACTION_TARGET_STRS[action] + " "
            + COMPLETED_ACTION_STRS[action]);
        if (skipped != 0) {
            log(skipped + " " + ACTION_TARGET_STRS[action]
                + " were not successfully " + COMPLETED_ACTION_STRS[action]);
        }
    }
    protected String resolveFile(String file) {
        return file.replace(System.getProperty("file.separator").charAt(0),
                            remoteFileSep.charAt(0));
    }
    protected void createParents(FTPClient ftp, String filename)
        throws IOException, BuildException {
        File dir = new File(filename);
        if (dirCache.contains(dir)) {
            return;
        }
        Vector parents = new Vector();
        String dirname;
        while ((dirname = dir.getParent()) != null) {
            File checkDir = new File(dirname);
            if (dirCache.contains(checkDir)) {
                break;
            }
            dir = checkDir;
            parents.addElement(dir);
        }
        int i = parents.size() - 1;
        if (i >= 0) {
            String cwd = ftp.printWorkingDirectory();
            String parent = dir.getParent();
            if (parent != null) {
                if (!ftp.changeWorkingDirectory(resolveFile(parent))) {
                    throw new BuildException("could not change to "
                                             + "directory: " + ftp.getReplyString());
                }
            }
            while (i >= 0) {
                dir = (File) parents.elementAt(i--);
                if (!ftp.changeWorkingDirectory(dir.getName())) {
                    log("creating remote directory "
                        + resolveFile(dir.getPath()), Project.MSG_VERBOSE);
                    if (!ftp.makeDirectory(dir.getName())) {
                        handleMkDirFailure(ftp);
                    }
                    if (!ftp.changeWorkingDirectory(dir.getName())) {
                        throw new BuildException("could not change to "
                                                 + "directory: " + ftp.getReplyString());
                    }
                }
                dirCache.add(dir);
            }
            ftp.changeWorkingDirectory(cwd);
        }
    }
    private long getTimeDiff(FTPClient ftp) {
        long returnValue = 0;
        File tempFile = findFileName(ftp);
        try {
            FILE_UTILS.createNewFile(tempFile);
            long localTimeStamp = tempFile.lastModified();
            BufferedInputStream instream = new BufferedInputStream(new FileInputStream(tempFile));
            ftp.storeFile(tempFile.getName(), instream);
            instream.close();
            boolean success = FTPReply.isPositiveCompletion(ftp.getReplyCode());
            if (success) {
                FTPFile [] ftpFiles = ftp.listFiles(tempFile.getName());
                if (ftpFiles.length == 1) {
                    long remoteTimeStamp = ftpFiles[0].getTimestamp().getTime().getTime();
                    returnValue = localTimeStamp - remoteTimeStamp;
                }
                ftp.deleteFile(ftpFiles[0].getName());
            }
            Delete mydelete = new Delete();
            mydelete.bindToOwner(this);
            mydelete.setFile(tempFile.getCanonicalFile());
            mydelete.execute();
        } catch (Exception e) {
            throw new BuildException(e, getLocation());
        }
        return returnValue;
    }
    private File findFileName(FTPClient ftp) {
        FTPFile [] theFiles = null;
        final int maxIterations = 1000;
        for (int counter = 1; counter < maxIterations; counter++) {
            File localFile = FILE_UTILS.createTempFile(
                                                       "ant" + Integer.toString(counter), ".tmp",
                                                       null, false, false);
            String fileName = localFile.getName();
            boolean found = false;
            try {
                if (theFiles == null) {
                    theFiles = ftp.listFiles();
                }
                for (int counter2 = 0; counter2 < theFiles.length; counter2++) {
                    if (theFiles[counter2] != null
                        && theFiles[counter2].getName().equals(fileName)) {
                        found = true;
                        break;
                    }
                }
            } catch (IOException ioe) {
                throw new BuildException(ioe, getLocation());
            }
            if (!found) {
                localFile.deleteOnExit();
                return localFile;
            }
        }
        return null;
    }
    protected boolean isUpToDate(FTPClient ftp, File localFile,
                                 String remoteFile)
        throws IOException, BuildException {
        log("checking date for " + remoteFile, Project.MSG_VERBOSE);
        FTPFile[] files = ftp.listFiles(remoteFile);
        if (files == null || files.length == 0) {
            if (action == SEND_FILES) {
                log("Could not date test remote file: " + remoteFile
                    + "assuming out of date.", Project.MSG_VERBOSE);
                return false;
            } else {
                throw new BuildException("could not date test remote file: "
                                         + ftp.getReplyString());
            }
        }
        long remoteTimestamp = files[0].getTimestamp().getTime().getTime();
        long localTimestamp = localFile.lastModified();
        long adjustedRemoteTimestamp =
            remoteTimestamp + this.timeDiffMillis + this.granularityMillis;
        StringBuffer msg;
        synchronized(TIMESTAMP_LOGGING_SDF) {
            msg = new StringBuffer("   [")
                .append(TIMESTAMP_LOGGING_SDF.format(new Date(localTimestamp)))
                .append("] local");
        }
        log(msg.toString(), Project.MSG_VERBOSE);
        synchronized(TIMESTAMP_LOGGING_SDF) {
            msg = new StringBuffer("   [")
                .append(TIMESTAMP_LOGGING_SDF.format(new Date(adjustedRemoteTimestamp)))
                .append("] remote");
        }
        if (remoteTimestamp != adjustedRemoteTimestamp) {
            synchronized(TIMESTAMP_LOGGING_SDF) {
                msg.append(" - (raw: ")
                    .append(TIMESTAMP_LOGGING_SDF.format(new Date(remoteTimestamp)))
                    .append(")");
            }
        }
        log(msg.toString(), Project.MSG_VERBOSE);
        if (this.action == SEND_FILES) {
            return adjustedRemoteTimestamp >= localTimestamp;
        } else {
            return localTimestamp >= adjustedRemoteTimestamp;
        }
    }
    protected void doSiteCommand(FTPClient ftp, String theCMD)
        throws IOException, BuildException {
        boolean rc;
        String[] myReply = null;
        log("Doing Site Command: " + theCMD, Project.MSG_VERBOSE);
        rc = ftp.sendSiteCommand(theCMD);
        if (!rc) {
            log("Failed to issue Site Command: " + theCMD, Project.MSG_WARN);
        } else {
            myReply = ftp.getReplyStrings();
            for (int x = 0; x < myReply.length; x++) {
                if (myReply[x] != null && myReply[x].indexOf("200") == -1) {
                    log(myReply[x], Project.MSG_WARN);
                }
            }
        }
    }
    protected void sendFile(FTPClient ftp, String dir, String filename)
        throws IOException, BuildException {
        InputStream instream = null;
        try {
            File file = getProject().resolveFile(new File(dir, filename).getPath());
            if (newerOnly && isUpToDate(ftp, file, resolveFile(filename))) {
                return;
            }
            if (verbose) {
                log("transferring " + file.getAbsolutePath());
            }
            instream = new BufferedInputStream(new FileInputStream(file));
            createParents(ftp, filename);
            ftp.storeFile(resolveFile(filename), instream);
            boolean success = FTPReply.isPositiveCompletion(ftp.getReplyCode());
            if (!success) {
                String s = "could not put file: " + ftp.getReplyString();
                if (skipFailedTransfers) {
                    log(s, Project.MSG_WARN);
                    skipped++;
                } else {
                    throw new BuildException(s);
                }
            } else {
                if (chmod != null) {
                    doSiteCommand(ftp, "chmod " + chmod + " " + resolveFile(filename));
                }
                log("File " + file.getAbsolutePath() + " copied to " + server,
                    Project.MSG_VERBOSE);
                transferred++;
            }
        } finally {
            FileUtils.close(instream);
        }
    }
    protected void delFile(FTPClient ftp, String filename)
        throws IOException, BuildException {
        if (verbose) {
            log("deleting " + filename);
        }
        if (!ftp.deleteFile(resolveFile(filename))) {
            String s = "could not delete file: " + ftp.getReplyString();
            if (skipFailedTransfers) {
                log(s, Project.MSG_WARN);
                skipped++;
            } else {
                throw new BuildException(s);
            }
        } else {
            log("File " + filename + " deleted from " + server,
                Project.MSG_VERBOSE);
            transferred++;
        }
    }
    protected void rmDir(FTPClient ftp, String dirname)
        throws IOException, BuildException {
        if (verbose) {
            log("removing " + dirname);
        }
        if (!ftp.removeDirectory(resolveFile(dirname))) {
            String s = "could not remove directory: " + ftp.getReplyString();
            if (skipFailedTransfers) {
                log(s, Project.MSG_WARN);
                skipped++;
            } else {
                throw new BuildException(s);
            }
        } else {
            log("Directory " + dirname + " removed from " + server,
                Project.MSG_VERBOSE);
            transferred++;
        }
    }
    protected void getFile(FTPClient ftp, String dir, String filename)
        throws IOException, BuildException {
        OutputStream outstream = null;
        try {
            File file = getProject().resolveFile(new File(dir, filename).getPath());
            if (newerOnly && isUpToDate(ftp, file, resolveFile(filename))) {
                return;
            }
            if (verbose) {
                log("transferring " + filename + " to "
                    + file.getAbsolutePath());
            }
            File pdir = file.getParentFile();
            if (!pdir.exists()) {
                pdir.mkdirs();
            }
            outstream = new BufferedOutputStream(new FileOutputStream(file));
            ftp.retrieveFile(resolveFile(filename), outstream);
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                String s = "could not get file: " + ftp.getReplyString();
                if (skipFailedTransfers) {
                    log(s, Project.MSG_WARN);
                    skipped++;
                } else {
                    throw new BuildException(s);
                }
            } else {
                log("File " + file.getAbsolutePath() + " copied from "
                    + server, Project.MSG_VERBOSE);
                transferred++;
                if (preserveLastModified) {
                    outstream.close();
                    outstream = null;
                    FTPFile[] remote = ftp.listFiles(resolveFile(filename));
                    if (remote.length > 0) {
                        FILE_UTILS.setFileLastModified(file,
                                                       remote[0].getTimestamp()
                                                       .getTime().getTime());
                    }
                }
            }
        } finally {
            FileUtils.close(outstream);
        }
    }
    protected void listFile(FTPClient ftp, BufferedWriter bw, String filename)
        throws IOException, BuildException {
        if (verbose) {
            log("listing " + filename);
        }
        FTPFile[] ftpfiles = ftp.listFiles(resolveFile(filename));
        if (ftpfiles != null && ftpfiles.length > 0) {
            bw.write(ftpfiles[0].toString());
            bw.newLine();
            transferred++;
        }
    }
    protected void makeRemoteDir(FTPClient ftp, String dir)
        throws IOException, BuildException {
        String workingDirectory = ftp.printWorkingDirectory();
        if (verbose) {
            if (dir.indexOf("/") == 0 || workingDirectory == null) {
                log("Creating directory: " + dir + " in /");
            } else {
                log("Creating directory: " + dir + " in " + workingDirectory);
            }
        }
        if (dir.indexOf("/") == 0) {
            ftp.changeWorkingDirectory("/");
        }
        String subdir = "";
        StringTokenizer st = new StringTokenizer(dir, "/");
        while (st.hasMoreTokens()) {
            subdir = st.nextToken();
            log("Checking " + subdir, Project.MSG_DEBUG);
            if (!ftp.changeWorkingDirectory(subdir)) {
                if (!ftp.makeDirectory(subdir)) {
                    int rc = ftp.getReplyCode();
                    if (!(ignoreNoncriticalErrors
                          && (rc == FTPReply.CODE_550 || rc == FTPReply.CODE_553
                              || rc == CODE_521))) {
                        throw new BuildException("could not create directory: "
                                                 + ftp.getReplyString());
                    }
                    if (verbose) {
                        log("Directory already exists");
                    }
                } else {
                    if (verbose) {
                        log("Directory created OK");
                    }
                    ftp.changeWorkingDirectory(subdir);
                }
            }
        }
        if (workingDirectory != null) {
            ftp.changeWorkingDirectory(workingDirectory);
        }
    }
    private void handleMkDirFailure(FTPClient ftp)
        throws BuildException {
        int rc = ftp.getReplyCode();
        if (!(ignoreNoncriticalErrors
              && (rc == FTPReply.CODE_550 || rc == FTPReply.CODE_553 || rc == CODE_521))) {
            throw new BuildException("could not create directory: "
                                     + ftp.getReplyString());
        }
    }
    public void execute() throws BuildException {
        checkAttributes();
        FTPClient ftp = null;
        try {
            log("Opening FTP connection to " + server, Project.MSG_VERBOSE);
            ftp = new FTPClient();
            if (this.isConfigurationSet) {
                ftp = FTPConfigurator.configure(ftp, this);
            }
            ftp.setRemoteVerificationEnabled(enableRemoteVerification);
            ftp.connect(server, port);
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                throw new BuildException("FTP connection failed: "
                                         + ftp.getReplyString());
            }
            log("connected", Project.MSG_VERBOSE);
            log("logging in to FTP server", Project.MSG_VERBOSE);
            if ((this.account != null && !ftp.login(userid, password, account))
                || (this.account == null && !ftp.login(userid, password))) {
                throw new BuildException("Could not login to FTP server");
            }
            log("login succeeded", Project.MSG_VERBOSE);
            if (binary) {
                ftp.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
                if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                    throw new BuildException("could not set transfer type: "
                                             + ftp.getReplyString());
                }
            } else {
                ftp.setFileType(org.apache.commons.net.ftp.FTP.ASCII_FILE_TYPE);
                if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                    throw new BuildException("could not set transfer type: "
                                             + ftp.getReplyString());
                }
            }
            if (passive) {
                log("entering passive mode", Project.MSG_VERBOSE);
                ftp.enterLocalPassiveMode();
                if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                    throw new BuildException("could not enter into passive "
                                             + "mode: " + ftp.getReplyString());
                }
            }
            if (this.initialSiteCommand != null) {
                RetryHandler h = new RetryHandler(this.retriesAllowed, this);
                final FTPClient lftp = ftp;
                executeRetryable(h, new Retryable() {
                        public void execute() throws IOException {
                            doSiteCommand(lftp, FTP.this.initialSiteCommand);
                        }
                    }, "initial site command: " + this.initialSiteCommand);
            }
            if (umask != null) {
                RetryHandler h = new RetryHandler(this.retriesAllowed, this);
                final FTPClient lftp = ftp;
                executeRetryable(h, new Retryable() {
                        public void execute() throws IOException {
                            doSiteCommand(lftp, "umask " + umask);
                        }
                    }, "umask " + umask);
            }
            if (action == MK_DIR) {
                RetryHandler h = new RetryHandler(this.retriesAllowed, this);
                final FTPClient lftp = ftp;
                executeRetryable(h, new Retryable() {
                        public void execute() throws IOException {
                            makeRemoteDir(lftp, remotedir);
                        }
                    }, remotedir);
            } else if (action == SITE_CMD) {
                RetryHandler h = new RetryHandler(this.retriesAllowed, this);
                final FTPClient lftp = ftp;
                executeRetryable(h, new Retryable() {
                        public void execute() throws IOException {
                            doSiteCommand(lftp, FTP.this.siteCommand);
                        }
                    }, "Site Command: " + this.siteCommand);
            } else {
                if (remotedir != null) {
                    log("changing the remote directory to " + remotedir,
                        Project.MSG_VERBOSE);
                    ftp.changeWorkingDirectory(remotedir);
                    if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                        throw new BuildException("could not change remote "
                                                 + "directory: " + ftp.getReplyString());
                    }
                }
                if (newerOnly && timeDiffAuto) {
                    timeDiffMillis = getTimeDiff(ftp);
                }
                log(ACTION_STRS[action] + " " + ACTION_TARGET_STRS[action]);
                transferFiles(ftp);
            }
        } catch (IOException ex) {
            throw new BuildException("error during FTP transfer: " + ex, ex);
        } finally {
            if (ftp != null && ftp.isConnected()) {
                try {
                    log("disconnecting", Project.MSG_VERBOSE);
                    ftp.logout();
                    ftp.disconnect();
                } catch (IOException ex) {
                }
            }
        }
    }
    public static class Action extends EnumeratedAttribute {
        private static final String[] VALID_ACTIONS = {
            "send", "put", "recv", "get", "del", "delete", "list", "mkdir",
            "chmod", "rmdir", "site"
        };
        public String[] getValues() {
            return VALID_ACTIONS;
        }
        public int getAction() {
            String actionL = getValue().toLowerCase(Locale.ENGLISH);
            if (actionL.equals("send") || actionL.equals("put")) {
                return SEND_FILES;
            } else if (actionL.equals("recv") || actionL.equals("get")) {
                return GET_FILES;
            } else if (actionL.equals("del") || actionL.equals("delete")) {
                return DEL_FILES;
            } else if (actionL.equals("list")) {
                return LIST_FILES;
            } else if (actionL.equals("chmod")) {
                return CHMOD;
            } else if (actionL.equals("mkdir")) {
                return MK_DIR;
            } else if (actionL.equals("rmdir")) {
                return RM_DIR;
            } else if (actionL.equals("site")) {
                return SITE_CMD;
            }
            return SEND_FILES;
        }
    }
    public static class Granularity extends EnumeratedAttribute {
        private static final String[] VALID_GRANULARITIES = {
            "", "MINUTE", "NONE"
        };
        public String[] getValues() {
            return VALID_GRANULARITIES;
        }
        public long getMilliseconds(int action) {
            String granularityU = getValue().toUpperCase(Locale.ENGLISH);
            if ("".equals(granularityU)) {
                if (action == SEND_FILES) {
                    return GRANULARITY_MINUTE;
                }
            } else if ("MINUTE".equals(granularityU)) {
                return GRANULARITY_MINUTE;
            }
            return 0L;
        }
        static final Granularity getDefault() {
            Granularity g = new Granularity();
            g.setValue("");
            return g;
        }
    }
    public static class FTPSystemType extends EnumeratedAttribute {
        private static final String[] VALID_SYSTEM_TYPES = {
            "", "UNIX", "VMS", "WINDOWS", "OS/2", "OS/400",
            "MVS"
        };
        public String[] getValues() {
            return VALID_SYSTEM_TYPES;
        }
        static final FTPSystemType getDefault() {
            FTPSystemType ftpst = new FTPSystemType();
            ftpst.setValue("");
            return ftpst;
        }
    }
    public static class LanguageCode extends EnumeratedAttribute {
        private static final String[] VALID_LANGUAGE_CODES =
            getValidLanguageCodes();
        private static String[] getValidLanguageCodes() {
            Collection c = FTPClientConfig.getSupportedLanguageCodes();
            String[] ret = new String[c.size() + 1];
            int i = 0;
            ret[i++] = "";
            for (Iterator it = c.iterator(); it.hasNext(); i++) {
                ret[i] = (String) it.next();
            }
            return ret;
        }
        public String[] getValues() {
            return VALID_LANGUAGE_CODES;
        }
        static final LanguageCode getDefault() {
            LanguageCode lc = new LanguageCode();
            lc.setValue("");
            return lc;
        }
    }
}
