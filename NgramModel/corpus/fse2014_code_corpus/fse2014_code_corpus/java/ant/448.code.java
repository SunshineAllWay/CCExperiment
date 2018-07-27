package org.apache.tools.ant.taskdefs.optional.j2ee;
import org.apache.tools.ant.BuildException;
public interface HotDeploymentTool {
    String ACTION_DELETE = "delete";
    String ACTION_DEPLOY = "deploy";
    String ACTION_LIST = "list";
    String ACTION_UNDEPLOY = "undeploy";
    String ACTION_UPDATE = "update";
    void validateAttributes() throws BuildException;
    void deploy() throws BuildException;
    void setTask(ServerDeploy task);
}
