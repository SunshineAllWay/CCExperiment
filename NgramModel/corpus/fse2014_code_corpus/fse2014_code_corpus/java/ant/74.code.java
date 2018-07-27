package org.apache.tools.ant;
import org.apache.tools.ant.util.StringUtils;
public class NoBannerLogger extends DefaultLogger {
    protected String targetName;
    public NoBannerLogger() {
    }
    public synchronized void targetStarted(BuildEvent event) {
        targetName = extractTargetName(event);
    }
    protected String extractTargetName(BuildEvent event) {
        return event.getTarget().getName();
    }
    public synchronized void targetFinished(BuildEvent event) {
        targetName = null;
    }
    public void messageLogged(BuildEvent event) {
        if (event.getPriority() > msgOutputLevel
            || null == event.getMessage()
            || "".equals(event.getMessage().trim())) {
                return;
        }
        synchronized (this) {
            if (null != targetName) {
                out.println(StringUtils.LINE_SEP + targetName + ":");
                targetName = null;
            }
        }
        super.messageLogged(event);
    }
}
