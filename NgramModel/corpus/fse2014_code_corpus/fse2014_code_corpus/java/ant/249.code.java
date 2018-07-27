package org.apache.tools.ant.taskdefs;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.SubBuildListener;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.LogLevel;
public class Recorder extends Task implements SubBuildListener {
    private String filename = null;
    private Boolean append = null;
    private Boolean start = null;
    private int loglevel = -1;
    private boolean emacsMode = false;
    private static Hashtable recorderEntries = new Hashtable();
    public void init() {
        getProject().addBuildListener(this);
    }
    public void setName(String fname) {
        filename = fname;
    }
    public void setAction(ActionChoices action) {
        if (action.getValue().equalsIgnoreCase("start")) {
            start = Boolean.TRUE;
        } else {
            start = Boolean.FALSE;
        }
    }
    public void setAppend(boolean append) {
        this.append = (append ? Boolean.TRUE : Boolean.FALSE);
    }
    public void setEmacsMode(boolean emacsMode) {
        this.emacsMode = emacsMode;
    }
    public void setLoglevel(VerbosityLevelChoices level) {
        loglevel = level.getLevel();
    }
    public void execute() throws BuildException {
        if (filename == null) {
            throw new BuildException("No filename specified");
        }
        getProject().log("setting a recorder for name " + filename,
            Project.MSG_DEBUG);
        RecorderEntry recorder = getRecorder(filename, getProject());
        recorder.setMessageOutputLevel(loglevel);
        recorder.setEmacsMode(emacsMode);
        if (start != null) {
            if (start.booleanValue()) {
                recorder.reopenFile();
                recorder.setRecordState(start);
            } else {
                recorder.setRecordState(start);
                recorder.closeFile();
            }
        }
    }
    public static class ActionChoices extends EnumeratedAttribute {
        private static final String[] VALUES = {"start", "stop"};
        public String[] getValues() {
            return VALUES;
        }
    }
    public static class VerbosityLevelChoices extends LogLevel {
    }
    protected RecorderEntry getRecorder(String name, Project proj)
         throws BuildException {
        Object o = recorderEntries.get(name);
        RecorderEntry entry;
        if (o == null) {
            entry = new RecorderEntry(name);
            if (append == null) {
                entry.openFile(false);
            } else {
                entry.openFile(append.booleanValue());
            }
            entry.setProject(proj);
            recorderEntries.put(name, entry);
        } else {
            entry = (RecorderEntry) o;
        }
        return entry;
    }
    public void buildStarted(BuildEvent event) {
    }
    public void subBuildStarted(BuildEvent event) {
    }
    public void targetStarted(BuildEvent event) {
    }
    public void targetFinished(BuildEvent event) {
    }
    public void taskStarted(BuildEvent event) {
    }
    public void taskFinished(BuildEvent event) {
    }
    public void messageLogged(BuildEvent event) {
    }
    public void buildFinished(BuildEvent event) {
        cleanup();
    }
    public void subBuildFinished(BuildEvent event) {
        if (event.getProject() == getProject()) {
            cleanup();
        }
    }
    private void cleanup() {
        Hashtable entries = (Hashtable) recorderEntries.clone();
        Iterator itEntries = entries.entrySet().iterator();
        while (itEntries.hasNext()) {
            Map.Entry entry = (Map.Entry) itEntries.next();
            RecorderEntry re = (RecorderEntry) entry.getValue();
            if (re.getProject() == getProject()) {
                recorderEntries.remove(entry.getKey());
            }
        }
        getProject().removeBuildListener(this);
    }
}
