package org.apache.tools.ant.taskdefs.optional.perforce;
import java.io.File;
import java.util.Vector;
import java.util.ArrayList;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
public class P4Fstat extends P4Base {
    private int changelist;
    private String addCmd = "";
    private Vector filesets = new Vector();
    private static final int DEFAULT_CMD_LENGTH = 300;
    private int cmdLength = DEFAULT_CMD_LENGTH;
    private static final int SHOW_ALL = 0;
    private static final int SHOW_EXISTING = 1;
    private static final int SHOW_NON_EXISTING = 2;
    private int show = SHOW_NON_EXISTING;
    private FStatP4OutputHandler handler;
    private StringBuffer filelist;
    private int fileNum = 0;
    private int doneFileNum = 0;
    private boolean debug = false;
    private static final String EXISTING_HEADER
        = "Following files exist in perforce";
    private static final String NONEXISTING_HEADER
        = "Following files do not exist in perforce";
    public void setShowFilter(String filter) {
        if (filter.equalsIgnoreCase("all")) {
            show = SHOW_ALL;
        } else if (filter.equalsIgnoreCase("existing")) {
            show = SHOW_EXISTING;
        } else if (filter.equalsIgnoreCase("non-existing")) {
            show = SHOW_NON_EXISTING;
        } else {
            throw new BuildException("P4Fstat: ShowFilter should be one of: "
                + "all, existing, non-existing");
        }
    }
    public void setChangelist(int changelist) throws BuildException {
        if (changelist <= 0) {
            throw new BuildException("P4FStat: Changelist# should be a "
                + "positive number");
        }
        this.changelist = changelist;
    }
    public void addFileset(FileSet set) {
        filesets.addElement(set);
    }
    public void execute() throws BuildException {
        handler = new FStatP4OutputHandler(this);
        if (P4View != null) {
            addCmd = P4View;
        }
        P4CmdOpts = (changelist > 0) ? ("-c " + changelist) : "";
        filelist = new StringBuffer();
        for (int i = 0; i < filesets.size(); i++) {
            FileSet fs = (FileSet) filesets.elementAt(i);
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            String[] srcFiles = ds.getIncludedFiles();
            if (srcFiles != null) {
                fileNum = srcFiles.length;
                for (int j = 0; j < srcFiles.length; j++) {
                    File f = new File(ds.getBasedir(), srcFiles[j]);
                    filelist.append(" ").append('"').append(f.getAbsolutePath()).append('"');
                    doneFileNum++;
                    if (filelist.length() > cmdLength) {
                        execP4Fstat(filelist);
                        filelist = new StringBuffer();
                    }
                }
                if (filelist.length() > 0) {
                    execP4Fstat(filelist);
                }
            } else {
                log("No files specified to query status on!", Project.MSG_WARN);
            }
        }
        if (show == SHOW_ALL || show == SHOW_EXISTING) {
            printRes(handler.getExisting(), EXISTING_HEADER);
        }
        if (show == SHOW_ALL || show == SHOW_NON_EXISTING) {
            printRes(handler.getNonExisting(), NONEXISTING_HEADER);
        }
    }
    public int getLengthOfTask() {
        return fileNum;
    }
    int getPasses() {
        return filesets.size();
    }
    private void printRes(ArrayList ar, String header) {
        log(header, Project.MSG_INFO);
        for (int i = 0; i < ar.size(); i++) {
            log((String) ar.get(i), Project.MSG_INFO);
        }
    }
    private void execP4Fstat(StringBuffer list) {
        String l = list.substring(0);
        if (debug) {
            log("Executing fstat " + P4CmdOpts + " " + addCmd + l + "\n",
                Project.MSG_INFO);
        }
        execP4Command("fstat " + P4CmdOpts + " " + addCmd + l, handler);
    }
}
