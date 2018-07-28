package org.apache.tools.ant;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Date;
import java.text.DateFormat;
import org.apache.tools.ant.util.DateUtils;
import org.apache.tools.ant.util.StringUtils;
import org.apache.tools.ant.util.FileUtils;
public class DefaultLogger implements BuildLogger {
    public static final int LEFT_COLUMN_SIZE = 12;
    protected PrintStream out;
    protected PrintStream err;
    protected int msgOutputLevel = Project.MSG_ERR;
    private long startTime = System.currentTimeMillis();
    protected static final String lSep = StringUtils.LINE_SEP;
    protected boolean emacsMode = false;
    public DefaultLogger() {
    }
    public void setMessageOutputLevel(int level) {
        this.msgOutputLevel = level;
    }
    public void setOutputPrintStream(PrintStream output) {
        this.out = new PrintStream(output, true);
    }
    public void setErrorPrintStream(PrintStream err) {
        this.err = new PrintStream(err, true);
    }
    public void setEmacsMode(boolean emacsMode) {
        this.emacsMode = emacsMode;
    }
    public void buildStarted(BuildEvent event) {
        startTime = System.currentTimeMillis();
    }
    static void throwableMessage(StringBuffer m, Throwable error, boolean verbose) {
        while (error instanceof BuildException) { 
            Throwable cause = error.getCause();
            if (cause == null) {
                break;
            }
            String msg1 = error.toString();
            String msg2 = cause.toString();
            if (msg1.endsWith(msg2)) {
                m.append(msg1.substring(0, msg1.length() - msg2.length()));
                error = cause;
            } else {
                break;
            }
        }
        if (verbose || !(error instanceof BuildException)) {
            m.append(StringUtils.getStackTrace(error));
        } else {
            m.append(error).append(lSep);
        }
    }
    public void buildFinished(BuildEvent event) {
        Throwable error = event.getException();
        StringBuffer message = new StringBuffer();
        if (error == null) {
            message.append(StringUtils.LINE_SEP);
            message.append(getBuildSuccessfulMessage());
        } else {
            message.append(StringUtils.LINE_SEP);
            message.append(getBuildFailedMessage());
            message.append(StringUtils.LINE_SEP);
            throwableMessage(message, error, Project.MSG_VERBOSE <= msgOutputLevel);
        }
        message.append(StringUtils.LINE_SEP);
        message.append("Total time: ");
        message.append(formatTime(System.currentTimeMillis() - startTime));
        String msg = message.toString();
        if (error == null) {
            printMessage(msg, out, Project.MSG_VERBOSE);
        } else {
            printMessage(msg, err, Project.MSG_ERR);
        }
        log(msg);
    }
    protected String getBuildFailedMessage() {
        return "BUILD FAILED";
    }
    protected String getBuildSuccessfulMessage() {
        return "BUILD SUCCESSFUL";
    }
    public void targetStarted(BuildEvent event) {
        if (Project.MSG_INFO <= msgOutputLevel
            && !event.getTarget().getName().equals("")) {
            String msg = StringUtils.LINE_SEP
                + event.getTarget().getName() + ":";
            printMessage(msg, out, event.getPriority());
            log(msg);
        }
    }
    public void targetFinished(BuildEvent event) {
    }
    public void taskStarted(BuildEvent event) {
    }
    public void taskFinished(BuildEvent event) {
    }
    public void messageLogged(BuildEvent event) {
        int priority = event.getPriority();
        if (priority <= msgOutputLevel) {
            StringBuffer message = new StringBuffer();
            if (event.getTask() != null && !emacsMode) {
                String name = event.getTask().getTaskName();
                String label = "[" + name + "] ";
                int size = LEFT_COLUMN_SIZE - label.length();
                StringBuffer tmp = new StringBuffer();
                for (int i = 0; i < size; i++) {
                    tmp.append(" ");
                }
                tmp.append(label);
                label = tmp.toString();
                BufferedReader r = null;
                try {
                    r = new BufferedReader(
                            new StringReader(event.getMessage()));
                    String line = r.readLine();
                    boolean first = true;
                    do {
                        if (first) {
                            if (line == null) {
                                message.append(label);
                                break;
                            }
                        } else {
                            message.append(StringUtils.LINE_SEP);
                        }
                        first = false;
                        message.append(label).append(line);
                        line = r.readLine();
                    } while (line != null);
                } catch (IOException e) {
                    message.append(label).append(event.getMessage());
                } finally {
                    if (r != null) {
                        FileUtils.close(r);
                    }
                }
            } else {
                message.append(event.getMessage());
            }
            Throwable ex = event.getException();
            if (Project.MSG_DEBUG <= msgOutputLevel && ex != null) {
                    message.append(StringUtils.getStackTrace(ex));
            }
            String msg = message.toString();
            if (priority != Project.MSG_ERR) {
                printMessage(msg, out, priority);
            } else {
                printMessage(msg, err, priority);
            }
            log(msg);
        }
    }
    protected static String formatTime(final long millis) {
        return DateUtils.formatElapsedTime(millis);
    }
    protected void printMessage(final String message,
                                final PrintStream stream,
                                final int priority) {
        stream.println(message);
    }
    protected void log(String message) {
    }
    protected String getTimestamp() {
        Date date = new Date(System.currentTimeMillis());
        DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        String finishTime = formatter.format(date);
        return finishTime;
    }
    protected String extractProjectName(BuildEvent event) {
        Project project = event.getProject();
        return (project != null) ? project.getName() : null;
    }
}
