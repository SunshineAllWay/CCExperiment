package org.apache.tools.ant.taskdefs;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.SubBuildListener;
import org.apache.tools.ant.util.StringUtils;
public class RecorderEntry implements BuildLogger, SubBuildListener {
    private String filename = null;
    private boolean record = true;
    private int loglevel = Project.MSG_INFO;
    private PrintStream out = null;
    private long targetStartTime = 0L;
    private boolean emacsMode = false;
    private Project project;
    protected RecorderEntry(String name) {
        targetStartTime = System.currentTimeMillis();
        filename = name;
    }
    public String getFilename() {
        return filename;
    }
    public void setRecordState(Boolean state) {
        if (state != null) {
            flush();
            record = state.booleanValue();
        }
    }
    public void buildStarted(BuildEvent event) {
        log("> BUILD STARTED", Project.MSG_DEBUG);
    }
    public void buildFinished(BuildEvent event) {
        log("< BUILD FINISHED", Project.MSG_DEBUG);
        if (record && out != null) {
            Throwable error = event.getException();
            if (error == null) {
                out.println(StringUtils.LINE_SEP + "BUILD SUCCESSFUL");
            } else {
                out.println(StringUtils.LINE_SEP + "BUILD FAILED"
                            + StringUtils.LINE_SEP);
                error.printStackTrace(out);
            }
        }
        cleanup();
    }
    public void subBuildFinished(BuildEvent event) {
        if (event.getProject() == project) {
            cleanup();
        }
    }
    public void subBuildStarted(BuildEvent event) {
    }
    public void targetStarted(BuildEvent event) {
        log(">> TARGET STARTED -- " + event.getTarget(), Project.MSG_DEBUG);
        log(StringUtils.LINE_SEP + event.getTarget().getName() + ":",
            Project.MSG_INFO);
        targetStartTime = System.currentTimeMillis();
    }
    public void targetFinished(BuildEvent event) {
        log("<< TARGET FINISHED -- " + event.getTarget(), Project.MSG_DEBUG);
        String time = formatTime(System.currentTimeMillis() - targetStartTime);
        log(event.getTarget() + ":  duration " + time, Project.MSG_VERBOSE);
        flush();
    }
    public void taskStarted(BuildEvent event) {
        log(">>> TASK STARTED -- " + event.getTask(), Project.MSG_DEBUG);
    }
    public void taskFinished(BuildEvent event) {
        log("<<< TASK FINISHED -- " + event.getTask(), Project.MSG_DEBUG);
        flush();
    }
    public void messageLogged(BuildEvent event) {
        log("--- MESSAGE LOGGED", Project.MSG_DEBUG);
        StringBuffer buf = new StringBuffer();
        if (event.getTask() != null) {
            String name = event.getTask().getTaskName();
            if (!emacsMode) {
                String label = "[" + name + "] ";
                int size = DefaultLogger.LEFT_COLUMN_SIZE - label.length();
                for (int i = 0; i < size; i++) {
                    buf.append(" ");
                }
                buf.append(label);
            }
        }
        buf.append(event.getMessage());
        log(buf.toString(), event.getPriority());
    }
    private void log(String mesg, int level) {
        if (record && (level <= loglevel) && out != null) {
            out.println(mesg);
        }
    }
    private void flush() {
        if (record && out != null) {
            out.flush();
        }
    }
    public void setMessageOutputLevel(int level) {
        if (level >= Project.MSG_ERR && level <= Project.MSG_DEBUG) {
            loglevel = level;
        }
    }
    public void setOutputPrintStream(PrintStream output) {
        closeFile();
        out = output;
    }
    public void setEmacsMode(boolean emacsMode) {
        this.emacsMode = emacsMode;
    }
    public void setErrorPrintStream(PrintStream err) {
        setOutputPrintStream(err);
    }
    private static String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        if (minutes > 0) {
            return Long.toString(minutes) + " minute"
                 + (minutes == 1 ? " " : "s ")
                 + Long.toString(seconds % 60) + " second"
                 + (seconds % 60 == 1 ? "" : "s");
        } else {
            return Long.toString(seconds) + " second"
                 + (seconds % 60 == 1 ? "" : "s");
        }
    }
    public void setProject(Project project) {
        this.project = project;
        if (project != null) {
            project.addBuildListener(this);
        }
    }
    public Project getProject() {
        return project;
    }
    public void cleanup() {
        closeFile();
        if (project != null) {
            project.removeBuildListener(this);
        }
        project = null;
    }
    void openFile(boolean append) throws BuildException {
        openFileImpl(append);
    }
    void closeFile() {
        if (out != null) {
            out.close();
            out = null;
        }
    }
    void reopenFile() throws BuildException {
        openFileImpl(true);
    }
    private void openFileImpl(boolean append) throws BuildException {
        if (out == null) {
            try {
                out = new PrintStream(new FileOutputStream(filename, append));
            } catch (IOException ioe) {
                throw new BuildException("Problems opening file using a "
                                         + "recorder entry", ioe);
            }
        }
    }
}
