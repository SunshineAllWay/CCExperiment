package org.apache.tools.ant.listener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;
import java.io.PrintStream;
public class CommonsLoggingListener implements BuildListener, BuildLogger {
    private boolean initialized = false;
    private LogFactory logFactory;
    public static final String TARGET_LOG = "org.apache.tools.ant.Target";
    public static final String PROJECT_LOG = "org.apache.tools.ant.Project";
    public CommonsLoggingListener() {
    }
    private Log getLog(String cat, String suffix) {
        if (suffix != null) {
            suffix = suffix.replace('.', '-');
            suffix = suffix.replace(' ', '-');
            cat = cat + "." + suffix;
        }
        PrintStream tmpOut = System.out;
        PrintStream tmpErr = System.err;
        System.setOut(out);
        System.setErr(err);
        if (!initialized) {
            try {
                logFactory = LogFactory.getFactory();
            } catch (LogConfigurationException e) {
                e.printStackTrace(System.err);
                return null;
            }
        }
        initialized = true;
        Log log = logFactory.getInstance(cat);
        System.setOut(tmpOut);
        System.setErr(tmpErr);
        return log;
    }
    public void buildStarted(BuildEvent event) {
        String categoryString = PROJECT_LOG;
        Log log = getLog(categoryString, null);
        if (initialized) {
            realLog(log, "Build started.", Project.MSG_INFO, null);
        }
    }
    public void buildFinished(BuildEvent event) {
        if (initialized) {
            String categoryString = PROJECT_LOG;
            Log log = getLog(categoryString, event.getProject().getName());
            if (event.getException() == null) {
                realLog(log, "Build finished.", Project.MSG_INFO, null);
            } else {
                realLog(log, "Build finished with error.", Project.MSG_ERR,
                        event.getException());
            }
        }
    }
    public void targetStarted(BuildEvent event) {
        if (initialized) {
            Log log = getLog(TARGET_LOG,
                    event.getTarget().getName());
            realLog(log, "Start: " + event.getTarget().getName(),
                    Project.MSG_VERBOSE, null);
        }
    }
    public void targetFinished(BuildEvent event) {
        if (initialized) {
            String targetName = event.getTarget().getName();
            Log log = getLog(TARGET_LOG,
                    event.getTarget().getName());
            if (event.getException() == null) {
                realLog(log, "Target end: " + targetName, Project.MSG_DEBUG, null);
            } else {
                realLog(log, "Target \"" + targetName
                        + "\" finished with error.", Project.MSG_ERR,
                        event.getException());
            }
        }
    }
    public void taskStarted(BuildEvent event) {
        if (initialized) {
            Task task = event.getTask();
            Object real = task;
            if (task instanceof UnknownElement) {
                Object realObj = ((UnknownElement) task).getTask();
                if (realObj != null) {
                    real = realObj;
                }
            }
            Log log = getLog(real.getClass().getName(), null);
            if (log.isTraceEnabled()) {
                realLog(log, "Task \"" + task.getTaskName() + "\" started ",
                        Project.MSG_VERBOSE, null);
            }
        }
    }
    public void taskFinished(BuildEvent event) {
        if (initialized) {
            Task task = event.getTask();
            Object real = task;
            if (task instanceof UnknownElement) {
                Object realObj = ((UnknownElement) task).getTask();
                if (realObj != null) {
                    real = realObj;
                }
            }
            Log log = getLog(real.getClass().getName(), null);
            if (event.getException() == null) {
                if (log.isTraceEnabled()) {
                    realLog(log, "Task \"" + task.getTaskName() + "\" finished.",
                            Project.MSG_VERBOSE, null);
                }
            } else {
                realLog(log, "Task \"" + task.getTaskName()
                        + "\" finished with error.", Project.MSG_ERR,
                        event.getException());
            }
        }
    }
    public void messageLogged(BuildEvent event) {
        if (initialized) {
            Object categoryObject = event.getTask();
            String categoryString = null;
            String categoryDetail = null;
            if (categoryObject == null) {
                categoryObject = event.getTarget();
                if (categoryObject == null) {
                    categoryObject = event.getProject();
                    categoryString = PROJECT_LOG;
                    categoryDetail = event.getProject().getName();
                } else {
                    categoryString = TARGET_LOG;
                    categoryDetail = event.getTarget().getName();
                }
            } else {
                if (event.getTarget() != null) {
                    categoryString = categoryObject.getClass().getName();
                    categoryDetail = event.getTarget().getName();
                } else {
                    categoryString = categoryObject.getClass().getName();
                }
            }
            Log log = getLog(categoryString, categoryDetail);
            int priority = event.getPriority();
            String message = event.getMessage();
            realLog(log, message, priority , null);
        }
    }
    private void realLog(Log log, String message, int priority, Throwable t) {
        PrintStream tmpOut = System.out;
        PrintStream tmpErr = System.err;
        System.setOut(out);
        System.setErr(err);
        switch (priority) {
            case Project.MSG_ERR:
                if (t == null) {
                    log.error(message);
                } else {
                    log.error(message, t);
                }
                break;
            case Project.MSG_WARN:
                if (t == null) {
                    log.warn(message);
                } else {
                    log.warn(message, t);
                }
                break;
            case Project.MSG_INFO:
                if (t == null) {
                    log.info(message);
                } else {
                    log.info(message, t);
                }
                break;
            case Project.MSG_VERBOSE:
                log.debug(message);
                break;
            case Project.MSG_DEBUG:
                log.debug(message);
                break;
            default:
                log.error(message);
                break;
        }
        System.setOut(tmpOut);
        System.setErr(tmpErr);
    }
    PrintStream out = System.out;
    PrintStream err = System.err;
    public void setMessageOutputLevel(int level) {
    }
    public void setOutputPrintStream(PrintStream output) {
        this.out = output;
    }
    public void setEmacsMode(boolean emacsMode) {
    }
    public void setErrorPrintStream(PrintStream err) {
        this.err = err;
    }
}
