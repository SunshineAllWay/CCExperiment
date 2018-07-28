package org.apache.tools.ant.taskdefs.optional.unix;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.dispatch.DispatchTask;
import org.apache.tools.ant.dispatch.DispatchUtils;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.SymbolicLinkUtils;
public class Symlink extends DispatchTask {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private static final SymbolicLinkUtils SYMLINK_UTILS =
        SymbolicLinkUtils.getSymbolicLinkUtils();
    private String resource;
    private String link;
    private Vector fileSets = new Vector();
    private String linkFileName;
    private boolean overwrite;
    private boolean failonerror;
    private boolean executing = false;
    public void init() throws BuildException {
        super.init();
        setDefaults();
    }
    public synchronized void execute() throws BuildException {
        if (executing) {
            throw new BuildException(
                "Infinite recursion detected in Symlink.execute()");
        }
        try {
            executing = true;
            DispatchUtils.execute(this);
        } finally {
            executing = false;
        }
    }
    public void single() throws BuildException {
        try {
            if (resource == null) {
                handleError("Must define the resource to symlink to!");
                return;
            }
            if (link == null) {
                handleError("Must define the link name for symlink!");
                return;
            }
            doLink(resource, link);
        } finally {
            setDefaults();
        }
    }
    public void delete() throws BuildException {
        try {
            if (link == null) {
                handleError("Must define the link name for symlink!");
                return;
            }
            log("Removing symlink: " + link);
            SYMLINK_UTILS.deleteSymbolicLink(FILE_UTILS
                                             .resolveFile(new File("."), link),
                                             this);
        } catch (FileNotFoundException fnfe) {
            handleError(fnfe.toString());
        } catch (IOException ioe) {
            handleError(ioe.toString());
        } finally {
            setDefaults();
        }
    }
    public void recreate() throws BuildException {
        try {
            if (fileSets.isEmpty()) {
                handleError("File set identifying link file(s) "
                            + "required for action recreate");
                return;
            }
            Properties links = loadLinks(fileSets);
            for (Iterator kitr = links.keySet().iterator(); kitr.hasNext();) {
                String lnk = (String) kitr.next();
                String res = links.getProperty(lnk);
                try {
                    File test = new File(lnk);
                    if (!SYMLINK_UTILS.isSymbolicLink(lnk)) {
                        doLink(res, lnk);
                    } else if (!test.getCanonicalPath().equals(
                        new File(res).getCanonicalPath())) {
                        SYMLINK_UTILS.deleteSymbolicLink(test, this);
                        doLink(res, lnk);
                    } 
                } catch (IOException ioe) {
                    handleError("IO exception while creating link");
                }
            }
        } finally {
            setDefaults();
        }
    }
    public void record() throws BuildException {
        try {
            if (fileSets.isEmpty()) {
                handleError("Fileset identifying links to record required");
                return;
            }
            if (linkFileName == null) {
                handleError("Name of file to record links in required");
                return;
            }
            Hashtable byDir = new Hashtable();
            for (Iterator litr = findLinks(fileSets).iterator();
                litr.hasNext();) {
                File thisLink = (File) litr.next();
                File parent = thisLink.getParentFile();
                Vector v = (Vector) byDir.get(parent);
                if (v == null) {
                    v = new Vector();
                    byDir.put(parent, v);
                }
                v.addElement(thisLink);
            }
            for (Iterator dirs = byDir.keySet().iterator(); dirs.hasNext();) {
                File dir = (File) dirs.next();
                Vector linksInDir = (Vector) byDir.get(dir);
                Properties linksToStore = new Properties();
                for (Iterator dlnk = linksInDir.iterator(); dlnk.hasNext();) {
                    File lnk = (File) dlnk.next();
                    try {
                        linksToStore.put(lnk.getName(), lnk.getCanonicalPath());
                    } catch (IOException ioe) {
                        handleError("Couldn't get canonical name of parent link");
                    }
                }
                writePropertyFile(linksToStore, dir);
            }
        } finally {
            setDefaults();
        }
    }
    private void setDefaults() {
        resource = null;
        link = null;
        linkFileName = null;
        failonerror = true;   
        overwrite = false;    
        setAction("single");      
        fileSets.clear();
    }
    public void setOverwrite(boolean owrite) {
        this.overwrite = owrite;
    }
    public void setFailOnError(boolean foe) {
        this.failonerror = foe;
    }
    public void setAction(String action) {
        super.setAction(action);
    }
    public void setLink(String lnk) {
        this.link = lnk;
    }
    public void setResource(String src) {
        this.resource = src;
    }
    public void setLinkfilename(String lf) {
        this.linkFileName = lf;
    }
    public void addFileset(FileSet set) {
        fileSets.addElement(set);
    }
    public static void deleteSymlink(String path)
        throws IOException, FileNotFoundException {
        SYMLINK_UTILS.deleteSymbolicLink(new File(path), null);
    }
    public static void deleteSymlink(File linkfil)
        throws IOException {
        SYMLINK_UTILS.deleteSymbolicLink(linkfil, null);
    }
    private void writePropertyFile(Properties properties, File dir)
        throws BuildException {
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(
                new FileOutputStream(new File(dir, linkFileName)));
            properties.store(bos, "Symlinks from " + dir);
        } catch (IOException ioe) {
            throw new BuildException(ioe, getLocation());
        } finally {
            FileUtils.close(bos);
        }
    }
    private void handleError(String msg) {
        if (failonerror) {
            throw new BuildException(msg);
        }
        log(msg);
    }
    private void doLink(String res, String lnk) throws BuildException {
        File linkfil = new File(lnk);
        String options = "-s";
        if (overwrite) {
            options += "f";
            if (linkfil.exists()) {
                try {
                    SYMLINK_UTILS.deleteSymbolicLink(linkfil, this);
                } catch (FileNotFoundException fnfe) {
                    log("Symlink disappeared before it was deleted: " + lnk);
                } catch (IOException ioe) {
                    log("Unable to overwrite preexisting link or file: " + lnk,
                        ioe, Project.MSG_INFO);
                }
            }
        }
        String[] cmd = new String[] {"ln", options, res, lnk};
        try {
            Execute.runCommand(this, cmd);
        } catch (BuildException failedToExecute) {
            if (failonerror) {
                throw failedToExecute;
            } else {
                log(failedToExecute.getMessage(), failedToExecute, Project.MSG_INFO);
            }
        }
    }
    private HashSet findLinks(Vector v) {
        HashSet result = new HashSet();
        for (int i = 0; i < v.size(); i++) {
            FileSet fs = (FileSet) v.get(i);
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            String[][] fnd = new String[][]
                {ds.getIncludedFiles(), ds.getIncludedDirectories()};
            File dir = fs.getDir(getProject());
            for (int j = 0; j < fnd.length; j++) {
                for (int k = 0; k < fnd[j].length; k++) {
                    try {
                        File f = new File(dir, fnd[j][k]);
                        File pf = f.getParentFile();
                        String name = f.getName();
                        if (SYMLINK_UTILS.isSymbolicLink(pf, name)) {
                            result.add(new File(pf.getCanonicalFile(), name));
                        }
                    } catch (IOException e) {
                        handleError("IOException: " + fnd[j][k] + " omitted");
                    }
                }
            }
        }
        return result;
    }
    private Properties loadLinks(Vector v) {
        Properties finalList = new Properties();
        for (int i = 0; i < v.size(); i++) {
            FileSet fs = (FileSet) v.elementAt(i);
            DirectoryScanner ds = new DirectoryScanner();
            fs.setupDirectoryScanner(ds, getProject());
            ds.setFollowSymlinks(false);
            ds.scan();
            String[] incs = ds.getIncludedFiles();
            File dir = fs.getDir(getProject());
            for (int j = 0; j < incs.length; j++) {
                File inc = new File(dir, incs[j]);
                File pf = inc.getParentFile();
                Properties lnks = new Properties();
                InputStream is = null;
                try {
                    is = new BufferedInputStream(new FileInputStream(inc));
                    lnks.load(is);
                    pf = pf.getCanonicalFile();
                } catch (FileNotFoundException fnfe) {
                    handleError("Unable to find " + incs[j] + "; skipping it.");
                    continue;
                } catch (IOException ioe) {
                    handleError("Unable to open " + incs[j]
                                + " or its parent dir; skipping it.");
                    continue;
                } finally {
                    FileUtils.close(is);
                }
                lnks.list(new PrintStream(
                    new LogOutputStream(this, Project.MSG_INFO)));
                for (Iterator kitr = lnks.keySet().iterator(); kitr.hasNext();) {
                    String key = (String) kitr.next();
                    finalList.put(new File(pf, key).getAbsolutePath(),
                        lnks.getProperty(key));
                }
            }
        }
        return finalList;
    }
}
