package org.apache.tools.ant.taskdefs.optional.j2ee;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;
public class WebLogicHotDeploymentTool extends AbstractHotDeploymentTool
                                       implements HotDeploymentTool {
    private static final int STRING_BUFFER_SIZE = 1024;
    private static final String WEBLOGIC_DEPLOY_CLASS_NAME = "weblogic.deploy";
    private static final String[] VALID_ACTIONS
        = {ACTION_DELETE, ACTION_DEPLOY, ACTION_LIST, ACTION_UNDEPLOY, ACTION_UPDATE};
    private boolean debug;
    private String application;
    private String component;
    public void deploy() {
        Java java = new Java(getTask());
        java.setFork(true);
        java.setFailonerror(true);
        java.setClasspath(getClasspath());
        java.setClassname(WEBLOGIC_DEPLOY_CLASS_NAME);
        java.createArg().setLine(getArguments());
        java.execute();
    }
    public void validateAttributes() throws BuildException {
        super.validateAttributes();
        String action = getTask().getAction();
        if ((getPassword() == null)) {
            throw new BuildException("The password attribute must be set.");
        }
        if ((action.equals(ACTION_DEPLOY) || action.equals(ACTION_UPDATE))
            && application == null) {
            throw new BuildException("The application attribute must be set "
                + "if action = " + action);
        }
        if ((action.equals(ACTION_DEPLOY) || action.equals(ACTION_UPDATE))
            && getTask().getSource() == null) {
            throw new BuildException("The source attribute must be set if "
                + "action = " + action);
        }
        if ((action.equals(ACTION_DELETE) || action.equals(ACTION_UNDEPLOY))
            && application == null) {
            throw new BuildException("The application attribute must be set if "
                + "action = " + action);
        }
    }
    public String getArguments() throws BuildException {
        String action = getTask().getAction();
        String args = null;
        if (action.equals(ACTION_DEPLOY) || action.equals(ACTION_UPDATE)) {
            args = buildDeployArgs();
        } else if (action.equals(ACTION_DELETE) || action.equals(ACTION_UNDEPLOY)) {
            args = buildUndeployArgs();
        } else if (action.equals(ACTION_LIST)) {
            args = buildListArgs();
        }
        return args;
    }
    protected boolean isActionValid() {
        boolean valid = false;
        String action = getTask().getAction();
        for (int i = 0; i < VALID_ACTIONS.length; i++) {
            if (action.equals(VALID_ACTIONS[i])) {
                valid = true;
                break;
            }
        }
        return valid;
    }
    protected StringBuffer buildArgsPrefix() {
        ServerDeploy task = getTask();
        return new StringBuffer(STRING_BUFFER_SIZE)
                .append((getServer() != null)
                    ? "-url " + getServer()
                    : "")
                .append(" ")
                .append(debug ? "-debug " : "")
                .append((getUserName() != null)
                    ? "-username " + getUserName()
                    : "")
                .append(" ")
                .append(task.getAction()).append(" ")
                .append(getPassword()).append(" ");
    }
    protected String buildDeployArgs() {
        String args = buildArgsPrefix()
                .append(application).append(" ")
                .append(getTask().getSource())
                .toString();
        if (component != null) {
            args = "-component " + component + " " + args;
        }
        return args;
    }
    protected String buildUndeployArgs() {
        return buildArgsPrefix()
                .append(application).append(" ")
                .toString();
    }
    protected String buildListArgs() {
        return buildArgsPrefix()
                .toString();
    }
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    public void setApplication(String application) {
        this.application = application;
    }
    public void setComponent(String component) {
        this.component = component;
    }
}
