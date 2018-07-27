package org.apache.tools.ant.taskdefs.optional.j2ee;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;
public abstract class AbstractHotDeploymentTool implements HotDeploymentTool {
    private ServerDeploy task;
    private Path classpath;
    private String userName;
    private String password;
    private String server;
    public Path createClasspath() {
        if (classpath == null) {
            classpath = new Path(task.getProject());
        }
        return classpath.createPath();
    }
    protected abstract boolean isActionValid();
    public void validateAttributes() throws BuildException {
        if (task.getAction() == null) {
            throw new BuildException("The \"action\" attribute must be set");
        }
        if (!isActionValid()) {
            throw new BuildException("Invalid action \"" + task.getAction() + "\" passed");
        }
        if (classpath == null) {
            throw new BuildException("The classpath attribute must be set");
        }
    }
    public abstract void deploy() throws BuildException;
    public void setTask(ServerDeploy task) {
        this.task = task;
    }
    protected ServerDeploy getTask() {
        return task;
    }
    public Path getClasspath() {
        return classpath;
    }
    public void setClasspath(Path classpath) {
        this.classpath = classpath;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getServer() {
        return server;
    }
    public void setServer(String server) {
        this.server = server;
    }
}
