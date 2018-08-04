package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.util.StringUtils;
public class DefaultExcludes extends Task {
    private String add = "";
    private String remove = "";
    private boolean defaultrequested = false;
    private boolean echo = false;
    private int logLevel = Project.MSG_WARN;
    public void execute() throws BuildException {
        if (!defaultrequested && add.equals("") && remove.equals("") && !echo) {
            throw new BuildException("<defaultexcludes> task must set "
                + "at least one attribute (echo=\"false\""
                + " doesn't count since that is the default");
        }
        if (defaultrequested) {
            DirectoryScanner.resetDefaultExcludes();
        }
        if (!add.equals("")) {
            DirectoryScanner.addDefaultExclude(add);
        }
        if (!remove.equals("")) {
            DirectoryScanner.removeDefaultExclude(remove);
        }
        if (echo) {
            StringBuffer message
                = new StringBuffer("Current Default Excludes:");
            message.append(StringUtils.LINE_SEP);
            String[] excludes = DirectoryScanner.getDefaultExcludes();
            for (int i = 0; i < excludes.length; i++) {
                message.append("  ");
                message.append(excludes[i]);
                message.append(StringUtils.LINE_SEP);
            }
            log(message.toString(), logLevel);
        }
    }
    public void setDefault(boolean def) {
        defaultrequested = def;
    }
    public void setAdd(String add) {
        this.add = add;
    }
    public void setRemove(String remove) {
        this.remove = remove;
    }
    public void setEcho(boolean echo) {
        this.echo = echo;
    }
}