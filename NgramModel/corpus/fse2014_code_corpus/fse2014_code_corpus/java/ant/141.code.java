package org.apache.tools.ant.listener;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.NullEnumeration;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
public class Log4jListener implements BuildListener {
    private final boolean initialized;
    public static final String LOG_ANT = "org.apache.tools.ant";
    public Log4jListener() {
        Logger log = Logger.getLogger(LOG_ANT);
        Logger rootLog = Logger.getRootLogger();
        initialized = !(rootLog.getAllAppenders() instanceof NullEnumeration);
        if (!initialized) {
            log.error("No log4j.properties in build area");
        }
    }
    public void buildStarted(BuildEvent event) {
        if (initialized) {
            Logger log = Logger.getLogger(Project.class.getName());
            log.info("Build started.");
        }
    }
    public void buildFinished(BuildEvent event) {
        if (initialized) {
            Logger log = Logger.getLogger(Project.class.getName());
            if (event.getException() == null) {
                log.info("Build finished.");
            } else {
                log.error("Build finished with error.", event.getException());
            }
        }
    }
    public void targetStarted(BuildEvent event) {
        if (initialized) {
            Logger log = Logger.getLogger(Target.class.getName());
            log.info("Target \"" + event.getTarget().getName() + "\" started.");
        }
    }
    public void targetFinished(BuildEvent event) {
        if (initialized) {
            String targetName = event.getTarget().getName();
            Logger cat = Logger.getLogger(Target.class.getName());
            if (event.getException() == null) {
                cat.info("Target \"" + targetName + "\" finished.");
            } else {
                cat.error("Target \"" + targetName
                    + "\" finished with error.", event.getException());
            }
        }
    }
    public void taskStarted(BuildEvent event) {
        if (initialized) {
            Task task = event.getTask();
            Logger log = Logger.getLogger(task.getClass().getName());
            log.info("Task \"" + task.getTaskName() + "\" started.");
        }
    }
    public void taskFinished(BuildEvent event) {
        if (initialized) {
            Task task = event.getTask();
            Logger log = Logger.getLogger(task.getClass().getName());
            if (event.getException() == null) {
                log.info("Task \"" + task.getTaskName() + "\" finished.");
            } else {
                log.error("Task \"" + task.getTaskName()
                    + "\" finished with error.", event.getException());
            }
        }
    }
    public void messageLogged(BuildEvent event) {
        if (initialized) {
            Object categoryObject = event.getTask();
            if (categoryObject == null) {
                categoryObject = event.getTarget();
                if (categoryObject == null) {
                    categoryObject = event.getProject();
                }
            }
            Logger log
                = Logger.getLogger(categoryObject.getClass().getName());
            switch (event.getPriority()) {
                case Project.MSG_ERR:
                    log.error(event.getMessage());
                    break;
                case Project.MSG_WARN:
                    log.warn(event.getMessage());
                    break;
                case Project.MSG_INFO:
                    log.info(event.getMessage());
                    break;
                case Project.MSG_VERBOSE:
                    log.debug(event.getMessage());
                    break;
                case Project.MSG_DEBUG:
                    log.debug(event.getMessage());
                    break;
                default:
                    log.error(event.getMessage());
                    break;
            }
        }
    }
}
