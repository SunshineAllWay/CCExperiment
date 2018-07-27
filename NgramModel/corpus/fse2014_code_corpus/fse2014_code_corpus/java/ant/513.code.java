package org.apache.tools.ant.taskdefs.optional.perforce;
import java.io.File;
import java.util.Vector;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
public class P4Add extends P4Base {
    private static final int DEFAULT_CMD_LENGTH = 450;
    private int changelist;
    private String addCmd = "";
    private Vector filesets = new Vector();
    private int cmdLength = DEFAULT_CMD_LENGTH;
    public void setCommandlength(int len) throws BuildException {
        if (len <= 0) {
            throw new BuildException("P4Add: Commandlength should be a positive number");
        }
        this.cmdLength = len;
    }
    public void setChangelist(int changelist) throws BuildException {
        if (changelist <= 0) {
            throw new BuildException("P4Add: Changelist# should be a positive number");
        }
        this.changelist = changelist;
    }
    public void addFileset(FileSet set) {
        filesets.addElement(set);
    }
    public void execute() throws BuildException {
        if (P4View != null) {
            addCmd = P4View;
        }
        P4CmdOpts = (changelist > 0) ? ("-c " + changelist) : "";
        StringBuffer filelist = new StringBuffer();
        for (int i = 0; i < filesets.size(); i++) {
            FileSet fs = (FileSet) filesets.elementAt(i);
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            String[] srcFiles = ds.getIncludedFiles();
            if (srcFiles != null) {
                for (int j = 0; j < srcFiles.length; j++) {
                    File f = new File(ds.getBasedir(), srcFiles[j]);
                    filelist.append(" ").append('"').append(f.getAbsolutePath()).append('"');
                    if (filelist.length() > cmdLength) {
                        execP4Add(filelist);
                        filelist = new StringBuffer();
                    }
                }
                if (filelist.length() > 0) {
                    execP4Add(filelist);
                }
            } else {
                log("No files specified to add!", Project.MSG_WARN);
            }
        }
    }
    private void execP4Add(StringBuffer list) {
        log("Execing add " + P4CmdOpts + " " + addCmd + list, Project.MSG_INFO);
        execP4Command("-s add " + P4CmdOpts + " " + addCmd + list, new SimpleP4OutputHandler(this));
    }
}
