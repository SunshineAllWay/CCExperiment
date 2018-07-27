package org.apache.tools.ant.taskdefs.optional.pvcs;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteStreamHandler;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.taskdefs.PumpStreamHandler;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.util.FileUtils;
public class Pvcs extends org.apache.tools.ant.Task {
    private static final int POS_1 = 1;
    private static final int POS_2 = 2;
    private static final int POS_3 = 3;
    private String pvcsbin;
    private String repository;
    private String pvcsProject;
    private Vector pvcsProjects;
    private String workspace;
    private String force;
    private String promotiongroup;
    private String label;
    private String revision;
    private boolean ignorerc;
    private boolean updateOnly;
    private String filenameFormat;
    private String lineStart;
    private String userId;
    private String config;
    private static final String PCLI_EXE = "pcli";
    private static final String GET_EXE = "get";
    protected int runCmd(Commandline cmd, ExecuteStreamHandler out) {
        try {
            Project aProj = getProject();
            Execute exe = new Execute(out);
            exe.setAntRun(aProj);
            exe.setWorkingDirectory(aProj.getBaseDir());
            exe.setCommandline(cmd.getCommandline());
            return exe.execute();
        } catch (java.io.IOException e) {
            String msg = "Failed executing: " + cmd.toString()
                + ". Exception: " + e.getMessage();
            throw new BuildException(msg, getLocation());
        }
    }
    private String getExecutable(String exe) {
        StringBuffer correctedExe = new StringBuffer();
        if (getPvcsbin() != null) {
            if (pvcsbin.endsWith(File.separator)) {
                correctedExe.append(pvcsbin);
            } else {
                correctedExe.append(pvcsbin).append(File.separator);
            }
        }
        return correctedExe.append(exe).toString();
    }
    public void execute() throws org.apache.tools.ant.BuildException {
        int result = 0;
        if (repository == null || repository.trim().equals("")) {
            throw new BuildException("Required argument repository not specified");
        }
        Commandline commandLine = new Commandline();
        commandLine.setExecutable(getExecutable(PCLI_EXE));
        commandLine.createArgument().setValue("lvf");
        commandLine.createArgument().setValue("-z");
        commandLine.createArgument().setValue("-aw");
        if (getWorkspace() != null) {
            commandLine.createArgument().setValue("-sp" + getWorkspace());
        }
        commandLine.createArgument().setValue("-pr" + getRepository());
        String uid = getUserId();
        if (uid != null) {
            commandLine.createArgument().setValue("-id" + uid);
        }
        if (getPvcsproject() == null && getPvcsprojects().isEmpty()) {
            pvcsProject = "/";
        }
        if (getPvcsproject() != null) {
            commandLine.createArgument().setValue(getPvcsproject());
        }
        if (!getPvcsprojects().isEmpty()) {
            Enumeration e = getPvcsprojects().elements();
            while (e.hasMoreElements()) {
                String projectName = ((PvcsProject) e.nextElement()).getName();
                if (projectName == null || (projectName.trim()).equals("")) {
                    throw new BuildException("name is a required attribute "
                        + "of pvcsproject");
                }
                commandLine.createArgument().setValue(projectName);
            }
        }
        File tmp = null;
        File tmp2 = null;
        try {
            Random rand = new Random(System.currentTimeMillis());
            tmp = new File("pvcs_ant_" + rand.nextLong() + ".log");
            FileOutputStream fos = new FileOutputStream(tmp);
            tmp2 = new File("pvcs_ant_" + rand.nextLong() + ".log");
            log(commandLine.describeCommand(), Project.MSG_VERBOSE);
            try {
                result = runCmd(commandLine,
                                new PumpStreamHandler(fos,
                                    new LogOutputStream(this,
                                                        Project.MSG_WARN)));
            } finally {
                FileUtils.close(fos);
            }
            if (Execute.isFailure(result) && !ignorerc) {
                String msg = "Failed executing: " + commandLine.toString();
                throw new BuildException(msg, getLocation());
            }
            if (!tmp.exists()) {
                throw new BuildException("Communication between ant and pvcs "
                    + "failed. No output generated from executing PVCS "
                    + "commandline interface \"pcli\" and \"get\"");
            }
            log("Creating folders", Project.MSG_INFO);
            createFolders(tmp);
            massagePCLI(tmp, tmp2);
            commandLine.clearArgs();
            commandLine.setExecutable(getExecutable(GET_EXE));
            if (getConfig() != null && getConfig().length() > 0) {
                commandLine.createArgument().setValue("-c" + getConfig());
            }
            if (getForce() != null && getForce().equals("yes")) {
                commandLine.createArgument().setValue("-Y");
            } else {
                commandLine.createArgument().setValue("-N");
            }
            if (getPromotiongroup() != null) {
                commandLine.createArgument().setValue("-G"
                    + getPromotiongroup());
            } else {
                if (getLabel() != null) {
                    commandLine.createArgument().setValue("-v" + getLabel());
                } else {
                    if (getRevision() != null) {
                        commandLine.createArgument().setValue("-r"
                            + getRevision());
                    }
                }
            }
            if (updateOnly) {
                commandLine.createArgument().setValue("-U");
            }
            commandLine.createArgument().setValue("@" + tmp2.getAbsolutePath());
            log("Getting files", Project.MSG_INFO);
            log("Executing " + commandLine.toString(), Project.MSG_VERBOSE);
            result = runCmd(commandLine,
                new LogStreamHandler(this, Project.MSG_INFO, Project.MSG_WARN));
            if (result != 0 && !ignorerc) {
                String msg = "Failed executing: " + commandLine.toString()
                    + ". Return code was " + result;
                throw new BuildException(msg, getLocation());
            }
        } catch (FileNotFoundException e) {
            String msg = "Failed executing: " + commandLine.toString()
                + ". Exception: " + e.getMessage();
            throw new BuildException(msg, getLocation());
        } catch (IOException e) {
            String msg = "Failed executing: " + commandLine.toString()
                + ". Exception: " + e.getMessage();
            throw new BuildException(msg, getLocation());
        } catch (ParseException e) {
            String msg = "Failed executing: " + commandLine.toString()
                + ". Exception: " + e.getMessage();
            throw new BuildException(msg, getLocation());
        } finally {
            if (tmp != null) {
                tmp.delete();
            }
            if (tmp2 != null) {
                tmp2.delete();
            }
        }
    }
    private void createFolders(File file) throws IOException, ParseException {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            MessageFormat mf = new MessageFormat(getFilenameFormat());
            String line = in.readLine();
            while (line != null) {
                log("Considering \"" + line + "\"", Project.MSG_VERBOSE);
                if (line.startsWith("\"\\")    
                    || line.startsWith("\"/")  
                   || (line.length() > POS_3 && line.startsWith("\"")
                        && Character.isLetter(line.charAt(POS_1))
                        && String.valueOf(line.charAt(POS_2)).equals(":")
                        && String.valueOf(line.charAt(POS_3)).equals("\\"))) {
                    Object[] objs = mf.parse(line);
                    String f = (String) objs[1];
                    int index = f.lastIndexOf(File.separator);
                    if (index > -1) {
                        File dir = new File(f.substring(0, index));
                        if (!dir.exists()) {
                            log("Creating " + dir.getAbsolutePath(),
                                Project.MSG_VERBOSE);
                            if (dir.mkdirs()) {
                                log("Created " + dir.getAbsolutePath(),
                                    Project.MSG_INFO);
                            } else {
                                log("Failed to create "
                                    + dir.getAbsolutePath(),
                                    Project.MSG_INFO);
                            }
                        } else {
                            log(dir.getAbsolutePath() + " exists. Skipping",
                                Project.MSG_VERBOSE);
                        }
                    } else {
                        log("File separator problem with " + line,
                            Project.MSG_WARN);
                    }
                } else {
                    log("Skipped \"" + line + "\"", Project.MSG_VERBOSE);
                }
                line = in.readLine();
            }
        } finally {
            FileUtils.close(in);
        }
    }
    private void massagePCLI(File in, File out)
        throws IOException {
        BufferedReader inReader = null;
        BufferedWriter outWriter = null;
        try {
            inReader = new BufferedReader(new FileReader(in));
            outWriter = new BufferedWriter(new FileWriter(out));
            String s = null;
            while ((s = inReader.readLine()) != null) {
                String sNormal = s.replace('\\', '/');
                outWriter.write(sNormal);
                outWriter.newLine();
            }
        } finally {
            FileUtils.close(inReader);
            FileUtils.close(outWriter);
        }
    }
    public String getRepository() {
        return repository;
    }
    public String getFilenameFormat() {
        return filenameFormat;
    }
    public void setFilenameFormat(String f) {
        filenameFormat = f;
    }
    public String getLineStart() {
        return lineStart;
    }
    public void setLineStart(String l) {
        lineStart = l;
    }
    public void setRepository(String repo) {
        repository = repo;
    }
    public String getPvcsproject() {
        return pvcsProject;
    }
    public void setPvcsproject(String prj) {
        pvcsProject = prj;
    }
    public Vector getPvcsprojects() {
        return pvcsProjects;
    }
    public String getWorkspace() {
        return workspace;
    }
    public void setWorkspace(String ws) {
        workspace = ws;
    }
    public String getPvcsbin() {
        return pvcsbin;
    }
    public void setPvcsbin(String bin) {
        pvcsbin = bin;
    }
    public String getForce() {
        return force;
    }
    public void setForce(String f) {
        if (f != null && f.equalsIgnoreCase("yes")) {
            force = "yes";
        } else {
            force = "no";
        }
    }
    public String getPromotiongroup() {
        return promotiongroup;
    }
    public void setPromotiongroup(String w) {
        promotiongroup = w;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String l) {
        label = l;
    }
    public String getRevision() {
        return revision;
    }
    public void setRevision(String r) {
        revision = r;
    }
    public boolean getIgnoreReturnCode() {
        return ignorerc;
    }
    public void setIgnoreReturnCode(boolean b) {
        ignorerc = b;
    }
    public void addPvcsproject(PvcsProject p) {
        pvcsProjects.addElement(p);
    }
    public boolean getUpdateOnly() {
        return updateOnly;
    }
    public void setUpdateOnly(boolean l) {
        updateOnly = l;
    }
    public String getConfig() {
        return config;
    }
    public void setConfig(File f) {
        config = f.toString();
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String u) {
        userId = u;
    }
    public Pvcs() {
        super();
        pvcsProject = null;
        pvcsProjects = new Vector();
        workspace = null;
        repository = null;
        pvcsbin = null;
        force = null;
        promotiongroup = null;
        label = null;
        ignorerc = false;
        updateOnly = false;
        lineStart = "\"P:";
        filenameFormat = "{0}-arc({1})";
    }
}
