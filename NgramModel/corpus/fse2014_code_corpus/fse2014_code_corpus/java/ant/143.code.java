package org.apache.tools.ant.listener;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.util.StringUtils;
public class ProfileLogger extends DefaultLogger {
    private Map profileData = new HashMap(); 
    public void targetStarted(BuildEvent event) {
        Date now = new Date();
        String name = "Target " + event.getTarget().getName();
        logStart(event, now, name);
        profileData.put(event.getTarget(), now);
    }
    public void targetFinished(BuildEvent event) {
        Date start = (Date) profileData.remove(event.getTarget());
        String name = "Target " + event.getTarget().getName();
        logFinish(event, start, name);
    }
    public void taskStarted(BuildEvent event) {
        String name = event.getTask().getTaskName();
        Date now = new Date();
        logStart(event, now, name);
        profileData.put(event.getTask(), now);
    }
    public void taskFinished(BuildEvent event) {
        Date start = (Date) profileData.remove(event.getTask());
        String name = event.getTask().getTaskName();
        logFinish(event, start, name);
    }
    private void logFinish(BuildEvent event, Date start, String name) {
        Date now = new Date();
        String msg = null;
        if (start != null) {
            long diff = now.getTime() - start.getTime();
            msg = StringUtils.LINE_SEP + name + ": finished" + now + " ("
                    + diff + "ms)";
        } else {
            msg = StringUtils.LINE_SEP + name + ": finished" + now
                    + " (unknown duration, start not detected)";
        }
        printMessage(msg, out, event.getPriority());
        log(msg);
    }
    private void logStart(BuildEvent event, Date start, String name) {
        String msg = StringUtils.LINE_SEP + name + ": started " + start;
        printMessage(msg, out, event.getPriority());
        log(msg);
    }
}
