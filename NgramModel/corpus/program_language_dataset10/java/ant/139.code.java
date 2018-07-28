package org.apache.tools.ant.listener;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.SubBuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.StringUtils;
import java.io.File;
public class BigProjectLogger extends SimpleBigProjectLogger
    implements SubBuildListener {
    private volatile boolean subBuildStartedRaised = false;
    private final Object subBuildLock = new Object();
    public static final String HEADER
        = "======================================================================";
    public static final String FOOTER = HEADER;
    protected String getBuildFailedMessage() {
        return super.getBuildFailedMessage() + TimestampedLogger.SPACER + getTimestamp();
    }
    protected String getBuildSuccessfulMessage() {
        return super.getBuildSuccessfulMessage() + TimestampedLogger.SPACER + getTimestamp();
    }
    public void targetStarted(BuildEvent event) {
        maybeRaiseSubBuildStarted(event);
        super.targetStarted(event);
    }
    public void taskStarted(BuildEvent event) {
        maybeRaiseSubBuildStarted(event);
        super.taskStarted(event);
    }
    public void buildFinished(BuildEvent event) {
        maybeRaiseSubBuildStarted(event);
        subBuildFinished(event);
        super.buildFinished(event);
    }
    public void messageLogged(BuildEvent event) {
        maybeRaiseSubBuildStarted(event);
        super.messageLogged(event);
    }
    public void subBuildStarted(BuildEvent event) {
        String name = extractNameOrDefault(event);
        Project project = event.getProject();
        File base = project == null ? null : project.getBaseDir();
        String path =
            (base == null)
            ? "With no base directory"
            : "In " + base.getAbsolutePath();
        printMessage(StringUtils.LINE_SEP + getHeader()
                + StringUtils.LINE_SEP + "Entering project " + name
                        + StringUtils.LINE_SEP + path
                        + StringUtils.LINE_SEP + getFooter(),
                out,
                event.getPriority());
    }
    protected String extractNameOrDefault(BuildEvent event) {
        String name = extractProjectName(event);
        if (name == null) {
            name = "";
        } else {
            name = '"' + name + '"';
        }
        return name;
    }
    public void subBuildFinished(BuildEvent event) {
        String name = extractNameOrDefault(event);
        String failed = event.getException() != null ? "failing " : "";
        printMessage(StringUtils.LINE_SEP + getHeader()
                + StringUtils.LINE_SEP + "Exiting " + failed + "project "
                + name
                + StringUtils.LINE_SEP + getFooter(),
                out,
                event.getPriority());
    }
    protected String getHeader() {
        return HEADER;
    }
    protected String getFooter() {
        return FOOTER;
    }
    private void maybeRaiseSubBuildStarted(BuildEvent event) {
        if (!subBuildStartedRaised) {
            synchronized (subBuildLock) {
                if (!subBuildStartedRaised) {
                    subBuildStartedRaised = true;
                    subBuildStarted(event);
                }
            }
        }
    }
}
