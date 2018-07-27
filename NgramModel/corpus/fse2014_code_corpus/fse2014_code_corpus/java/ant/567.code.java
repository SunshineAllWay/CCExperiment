package org.apache.tools.ant.taskdefs.optional.vss;
import org.apache.tools.ant.types.EnumeratedAttribute;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.util.FileUtils;
public abstract class MSVSS extends Task implements MSVSSConstants {
    private String ssDir = null;
    private String vssLogin = null;
    private String vssPath = null;
    private String serverPath = null;
    private String version = null;
    private String date = null;
    private String label = null;
    private String autoResponse = null;
    private String localPath = null;
    private String comment = null;
    private String fromLabel = null;
    private String toLabel = null;
    private String outputFileName = null;
    private String user = null;
    private String fromDate = null;
    private String toDate = null;
    private String style = null;
    private boolean quiet = false;
    private boolean recursive = false;
    private boolean writable = false;
    private boolean failOnError = true;
    private boolean getLocalCopy = true;
    private int numDays = Integer.MIN_VALUE;
    private DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
    private CurrentModUpdated timestamp = null;
    private WritableFiles writableFiles = null;
    abstract Commandline buildCmdLine();
    public final void setSsdir(String dir) {
        this.ssDir = FileUtils.translatePath(dir);
    }
    public final void setLogin(final String vssLogin) {
        this.vssLogin = vssLogin;
    }
    public final void setVsspath(final String vssPath) {
        String projectPath;
        if (vssPath.startsWith("vss://")) { 
            projectPath = vssPath.substring(5);
        } else {
            projectPath = vssPath;
        }
        if (projectPath.startsWith(PROJECT_PREFIX)) {
            this.vssPath = projectPath;
        } else {
            this.vssPath = PROJECT_PREFIX + projectPath;
        }
    }
    public final void setServerpath(final String serverPath) {
        this.serverPath = serverPath;
    }
    public final void setFailOnError(final boolean failOnError) {
        this.failOnError = failOnError;
    }
    public void execute() throws BuildException {
        int result = 0;
        Commandline commandLine = buildCmdLine();
        result = run(commandLine);
        if (Execute.isFailure(result) && getFailOnError()) {
            String msg = "Failed executing: " + formatCommandLine(commandLine)
                     + " With a return code of " + result;
            throw new BuildException(msg, getLocation());
        }
    }
    protected void setInternalComment(final String comment) {
        this.comment = comment;
    }
    protected void setInternalAutoResponse(final String autoResponse) {
        this.autoResponse = autoResponse;
    }
    protected void setInternalDate(final String date) {
        this.date = date;
    }
    protected void setInternalDateFormat(final DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }
    protected void setInternalFailOnError(final boolean failOnError) {
        this.failOnError = failOnError;
    }
    protected void setInternalFromDate(final String fromDate) {
        this.fromDate = fromDate;
    }
    protected void setInternalFromLabel(final String fromLabel) {
        this.fromLabel = fromLabel;
    }
    protected void setInternalLabel(final String label) {
        this.label = label;
    }
    protected void setInternalLocalPath(final String localPath) {
        this.localPath = localPath;
    }
    protected void setInternalNumDays(final int numDays) {
        this.numDays = numDays;
    }
    protected void setInternalOutputFilename(final String outputFileName) {
        this.outputFileName = outputFileName;
    }
    protected void setInternalQuiet(final boolean quiet) {
        this.quiet = quiet;
    }
    protected void setInternalRecursive(final boolean recursive) {
        this.recursive = recursive;
    }
    protected void setInternalStyle(final String style) {
        this.style = style;
    }
    protected void setInternalToDate(final String toDate) {
        this.toDate = toDate;
    }
    protected void setInternalToLabel(final String toLabel) {
        this.toLabel = toLabel;
    }
    protected void setInternalUser(final String user) {
        this.user = user;
    }
    protected void setInternalVersion(final String version) {
        this.version = version;
    }
    protected void setInternalWritable(final boolean writable) {
        this.writable = writable;
    }
    protected void setInternalFileTimeStamp(final CurrentModUpdated timestamp) {
        this.timestamp = timestamp;
    }
    protected void setInternalWritableFiles(final WritableFiles writableFiles) {
        this.writableFiles = writableFiles;
    }
    protected void setInternalGetLocalCopy(final boolean getLocalCopy) {
        this.getLocalCopy = getLocalCopy;
    }
    protected String getSSCommand() {
        if (ssDir == null) {
            return SS_EXE;
        }
        return ssDir.endsWith(File.separator) ? ssDir + SS_EXE : ssDir
                 + File.separator + SS_EXE;
    }
    protected String getVsspath() {
        return vssPath;
    }
    protected String getQuiet() {
        return quiet ? FLAG_QUIET : "";
    }
    protected String getRecursive() {
        return recursive ? FLAG_RECURSION : "";
    }
    protected String getWritable() {
        return writable ? FLAG_WRITABLE : "";
    }
    protected String getLabel() {
        String shortLabel = "";
        if (label != null && label.length() > 0) {
                shortLabel = FLAG_LABEL + getShortLabel();
        }
        return shortLabel;
    }
    private String getShortLabel() {
        String shortLabel;
        if (label !=  null && label.length() > 31) {
            shortLabel = this.label.substring(0, 30);
            log("Label is longer than 31 characters, truncated to: " + shortLabel,
                Project.MSG_WARN);
        } else {
            shortLabel = label;
        }
        return shortLabel;
    }
    protected String getStyle() {
        return style != null ? style : "";
    }
    protected String getVersionDateLabel() {
        String versionDateLabel = "";
        if (version != null) {
            versionDateLabel = FLAG_VERSION + version;
        } else if (date != null) {
            versionDateLabel = FLAG_VERSION_DATE + date;
        } else {
            String shortLabel = getShortLabel();
            if (shortLabel != null && !shortLabel.equals("")) {
                versionDateLabel = FLAG_VERSION_LABEL + shortLabel;
            }
        }
        return versionDateLabel;
    }
    protected String getVersion() {
        return version != null ? FLAG_VERSION + version : "";
    }
    protected String getLocalpath() {
        String lclPath = ""; 
        if (localPath != null) {
            File dir = getProject().resolveFile(localPath);
            if (!dir.exists()) {
                boolean done = dir.mkdirs();
                if (!done) {
                    String msg = "Directory " + localPath + " creation was not "
                            + "successful for an unknown reason";
                    throw new BuildException(msg, getLocation());
                }
                getProject().log("Created dir: " + dir.getAbsolutePath());
            }
            lclPath = FLAG_OVERRIDE_WORKING_DIR + localPath;
        }
        return lclPath;
    }
    protected String getComment() {
        return comment != null ? FLAG_COMMENT + comment : FLAG_COMMENT + "-";
    }
    protected String getAutoresponse() {
        if (autoResponse == null) {
            return FLAG_AUTORESPONSE_DEF;
        }
        if (autoResponse.equalsIgnoreCase("Y")) {
            return FLAG_AUTORESPONSE_YES;
        } else if (autoResponse.equalsIgnoreCase("N")) {
            return FLAG_AUTORESPONSE_NO;
        } else {
            return FLAG_AUTORESPONSE_DEF;
        }
    }
    protected String getLogin() {
        return vssLogin != null ? FLAG_LOGIN + vssLogin : "";
    }
    protected String getOutput() {
        return outputFileName != null ? FLAG_OUTPUT + outputFileName : "";
    }
    protected String getUser() {
        return user != null ? FLAG_USER + user : "";
    }
    protected String getVersionLabel() {
        if (fromLabel == null && toLabel == null) {
            return "";
        }
        if (fromLabel != null && toLabel != null) {
            if (fromLabel.length() > 31) {
                fromLabel = fromLabel.substring(0, 30);
                log("FromLabel is longer than 31 characters, truncated to: "
                    + fromLabel, Project.MSG_WARN);
            }
            if (toLabel.length() > 31) {
                toLabel = toLabel.substring(0, 30);
                log("ToLabel is longer than 31 characters, truncated to: "
                    + toLabel, Project.MSG_WARN);
            }
            return FLAG_VERSION_LABEL + toLabel + VALUE_FROMLABEL + fromLabel;
        } else if (fromLabel != null) {
            if (fromLabel.length() > 31) {
                fromLabel = fromLabel.substring(0, 30);
                log("FromLabel is longer than 31 characters, truncated to: "
                    + fromLabel, Project.MSG_WARN);
            }
            return FLAG_VERSION + VALUE_FROMLABEL + fromLabel;
        } else {
            if (toLabel.length() > 31) {
                toLabel = toLabel.substring(0, 30);
                log("ToLabel is longer than 31 characters, truncated to: "
                    + toLabel, Project.MSG_WARN);
            }
            return FLAG_VERSION_LABEL + toLabel;
        }
    }
    protected String getVersionDate() throws BuildException {
        if (fromDate == null && toDate == null
            && numDays == Integer.MIN_VALUE) {
            return "";
        }
        if (fromDate != null && toDate != null) {
            return FLAG_VERSION_DATE + toDate + VALUE_FROMDATE + fromDate;
        } else if (toDate != null && numDays != Integer.MIN_VALUE) {
            try {
                return FLAG_VERSION_DATE + toDate + VALUE_FROMDATE
                        + calcDate(toDate, numDays);
            } catch (ParseException ex) {
                String msg = "Error parsing date: " + toDate;
                throw new BuildException(msg, getLocation());
            }
        } else if (fromDate != null && numDays != Integer.MIN_VALUE) {
            try {
                return FLAG_VERSION_DATE + calcDate(fromDate, numDays)
                        + VALUE_FROMDATE + fromDate;
            } catch (ParseException ex) {
                String msg = "Error parsing date: " + fromDate;
                throw new BuildException(msg, getLocation());
            }
        } else {
            return fromDate != null ? FLAG_VERSION + VALUE_FROMDATE
                    + fromDate : FLAG_VERSION_DATE + toDate;
        }
    }
    protected String getGetLocalCopy() {
        return (!getLocalCopy) ? FLAG_NO_GET : "";
    }
    private boolean getFailOnError() {
        return getWritableFiles().equals(WRITABLE_SKIP) ? false : failOnError;
    }
    public String getFileTimeStamp() {
        if (timestamp == null) {
            return "";
        } else if (timestamp.getValue().equals(TIME_MODIFIED)) {
            return FLAG_FILETIME_MODIFIED;
        } else if (timestamp.getValue().equals(TIME_UPDATED)) {
            return FLAG_FILETIME_UPDATED;
        } else {
            return FLAG_FILETIME_DEF;
        }
    }
    public String getWritableFiles() {
        if (writableFiles == null) {
            return "";
        } else if (writableFiles.getValue().equals(WRITABLE_REPLACE)) {
            return FLAG_REPLACE_WRITABLE;
        } else if (writableFiles.getValue().equals(WRITABLE_SKIP)) {
            failOnError = false;
            return FLAG_SKIP_WRITABLE;
        } else {
            return "";
        }
    }
    private int run(Commandline cmd) {
        try {
            Execute exe = new Execute(new LogStreamHandler(this,
                    Project.MSG_INFO,
                    Project.MSG_WARN));
            if (serverPath != null) {
                String[] env = exe.getEnvironment();
                if (env == null) {
                    env = new String[0];
                }
                String[] newEnv = new String[env.length + 1];
                System.arraycopy(env, 0, newEnv, 0, env.length);
                newEnv[env.length] = "SSDIR=" + serverPath;
                exe.setEnvironment(newEnv);
            }
            exe.setAntRun(getProject());
            exe.setWorkingDirectory(getProject().getBaseDir());
            exe.setCommandline(cmd.getCommandline());
            exe.setVMLauncher(false);
            return exe.execute();
        } catch (IOException e) {
            throw new BuildException(e, getLocation());
        }
    }
    private String calcDate(String startDate, int daysToAdd) throws ParseException {
        Calendar calendar = new GregorianCalendar();
        Date currentDate = dateFormat.parse(startDate);
        calendar.setTime(currentDate);
        calendar.add(Calendar.DATE, daysToAdd);
        return dateFormat.format(calendar.getTime());
    }
    private String formatCommandLine(Commandline cmd) {
        StringBuffer sBuff = new StringBuffer(cmd.toString());
        int indexUser = sBuff.substring(0).indexOf(FLAG_LOGIN);
        if (indexUser > 0) {
            int indexPass = sBuff.substring(0).indexOf(",", indexUser);
            int indexAfterPass = sBuff.substring(0).indexOf(" ", indexPass);
            for (int i = indexPass + 1; i < indexAfterPass; i++) {
                sBuff.setCharAt(i, '*');
            }
        }
        return sBuff.toString();
    }
    public static class CurrentModUpdated extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] {TIME_CURRENT, TIME_MODIFIED, TIME_UPDATED};
        }
    }
    public static class WritableFiles extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] {WRITABLE_REPLACE, WRITABLE_SKIP, WRITABLE_FAIL};
        }
    }
}
