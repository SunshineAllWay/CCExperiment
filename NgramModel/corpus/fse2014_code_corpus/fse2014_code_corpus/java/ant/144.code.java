package org.apache.tools.ant.listener;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.NoBannerLogger;
public class SimpleBigProjectLogger extends NoBannerLogger {
    protected String extractTargetName(BuildEvent event) {
        String targetName = super.extractTargetName(event);
        String projectName = extractProjectName(event);
        if (projectName != null && targetName != null) {
            return projectName + '.' + targetName;
        } else {
            return targetName;
        }
    }
}
