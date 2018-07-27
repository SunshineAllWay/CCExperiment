package org.apache.tools.ant.taskdefs.optional;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.taskdefs.StreamPumper;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;
public class Cab extends MatchingTask {
    private static final int DEFAULT_RESULT = -99;
    private File cabFile;
    private File baseDir;
    private Vector filesets = new Vector();
    private boolean doCompress = true;
    private boolean doVerbose = false;
    private String cmdOptions;
    protected String archiveType = "cab";
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    public void setCabfile(File cabFile) {
        this.cabFile = cabFile;
    }
    public void setBasedir(File baseDir) {
        this.baseDir = baseDir;
    }
    public void setCompress(boolean compress) {
        doCompress = compress;
    }
    public void setVerbose(boolean verbose) {
        doVerbose = verbose;
    }
    public void setOptions(String options) {
        cmdOptions = options;
    }
    public void addFileset(FileSet set) {
        if (filesets.size() > 0) {
            throw new BuildException("Only one nested fileset allowed");
        }
        filesets.addElement(set);
    }
    protected void checkConfiguration() throws BuildException {
        if (baseDir == null && filesets.size() == 0) {
            throw new BuildException("basedir attribute or one "
                                     + "nested fileset is required!",
                                     getLocation());
        }
        if (baseDir != null && !baseDir.exists()) {
            throw new BuildException("basedir does not exist!", getLocation());
        }
        if (baseDir != null && filesets.size() > 0) {
            throw new BuildException(
                "Both basedir attribute and a nested fileset is not allowed");
        }
        if (cabFile == null) {
            throw new BuildException("cabfile attribute must be set!",
                                     getLocation());
        }
    }
    protected ExecTask createExec() throws BuildException {
        ExecTask exec = new ExecTask(this);
        return exec;
    }
    protected boolean isUpToDate(Vector files) {
        boolean upToDate = true;
        for (int i = 0; i < files.size() && upToDate; i++) {
            String file = files.elementAt(i).toString();
            if (FILE_UTILS.resolveFile(baseDir, file).lastModified()
                    > cabFile.lastModified()) {
                upToDate = false;
            }
        }
        return upToDate;
    }
    protected File createListFile(Vector files)
        throws IOException {
        File listFile = FILE_UTILS.createTempFile("ant", "", null, true, true);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(listFile));
            int size = files.size();
            for (int i = 0; i < size; i++) {
                writer.write('\"' + files.elementAt(i).toString() + '\"');
                writer.newLine();
            }
        } finally {
            FileUtils.close(writer);
        }
        return listFile;
    }
    protected void appendFiles(Vector files, DirectoryScanner ds) {
        String[] dsfiles = ds.getIncludedFiles();
        for (int i = 0; i < dsfiles.length; i++) {
            files.addElement(dsfiles[i]);
        }
    }
    protected Vector getFileList() throws BuildException {
        Vector files = new Vector();
        if (baseDir != null) {
            appendFiles(files, super.getDirectoryScanner(baseDir));
        } else {
            FileSet fs = (FileSet) filesets.elementAt(0);
            baseDir = fs.getDir();
            appendFiles(files, fs.getDirectoryScanner(getProject()));
        }
        return files;
    }
    public void execute() throws BuildException {
        checkConfiguration();
        Vector files = getFileList();
        if (isUpToDate(files)) {
            return;
        }
        log("Building " + archiveType + ": " + cabFile.getAbsolutePath());
        if (!Os.isFamily("windows")) {
            log("Using listcab/libcabinet", Project.MSG_VERBOSE);
            StringBuffer sb = new StringBuffer();
            Enumeration fileEnum = files.elements();
            while (fileEnum.hasMoreElements()) {
                sb.append(fileEnum.nextElement()).append("\n");
            }
            sb.append("\n").append(cabFile.getAbsolutePath()).append("\n");
            try {
                Process p = Execute.launch(getProject(),
                                           new String[] {"listcab"}, null,
                                           baseDir != null ? baseDir
                                                   : getProject().getBaseDir(),
                                           true);
                OutputStream out = p.getOutputStream();
                LogOutputStream outLog = new LogOutputStream(this, Project.MSG_VERBOSE);
                LogOutputStream errLog = new LogOutputStream(this, Project.MSG_ERR);
                StreamPumper    outPump = new StreamPumper(p.getInputStream(), outLog);
                StreamPumper    errPump = new StreamPumper(p.getErrorStream(), errLog);
                (new Thread(outPump)).start();
                (new Thread(errPump)).start();
                out.write(sb.toString().getBytes());
                out.flush();
                out.close();
                int result = DEFAULT_RESULT;
                try {
                    result = p.waitFor();
                    outPump.waitFor();
                    outLog.close();
                    errPump.waitFor();
                    errLog.close();
                } catch (InterruptedException ie) {
                    log("Thread interrupted: " + ie);
                }
                if (Execute.isFailure(result)) {
                    log("Error executing listcab; error code: " + result);
                }
            } catch (IOException ex) {
                String msg = "Problem creating " + cabFile + " " + ex.getMessage();
                throw new BuildException(msg, getLocation());
            }
        } else {
            try {
                File listFile = createListFile(files);
                ExecTask exec = createExec();
                File outFile = null;
                exec.setFailonerror(true);
                exec.setDir(baseDir);
                if (!doVerbose) {
                    outFile = FILE_UTILS.createTempFile("ant", "", null, true, true);
                    exec.setOutput(outFile);
                }
                exec.setExecutable("cabarc");
                exec.createArg().setValue("-r");
                exec.createArg().setValue("-p");
                if (!doCompress) {
                    exec.createArg().setValue("-m");
                    exec.createArg().setValue("none");
                }
                if (cmdOptions != null) {
                    exec.createArg().setLine(cmdOptions);
                }
                exec.createArg().setValue("n");
                exec.createArg().setFile(cabFile);
                exec.createArg().setValue("@" + listFile.getAbsolutePath());
                exec.execute();
                if (outFile != null) {
                    outFile.delete();
                }
                listFile.delete();
            } catch (IOException ioe) {
                String msg = "Problem creating " + cabFile + " " + ioe.getMessage();
                throw new BuildException(msg, getLocation());
            }
        }
    }
}
