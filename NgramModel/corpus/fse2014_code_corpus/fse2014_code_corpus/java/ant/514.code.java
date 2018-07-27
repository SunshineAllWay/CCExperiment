package org.apache.tools.ant.taskdefs.optional.perforce;
import java.io.IOException;
import org.apache.oro.text.perl.Perl5Util;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.types.Commandline;
public abstract class P4Base extends org.apache.tools.ant.Task {
    protected Perl5Util util = null;
    protected String shell;
    protected String P4Port = "";
    protected String P4Client = "";
    protected String P4User = "";
    protected String P4View = "";
    protected boolean failOnError = true;
    protected String P4Opts = "";
    protected String P4CmdOpts = "";
    private boolean inError = false;
    private String errorMessage = "";
    public boolean getInError() {
        return inError;
    }
    public void setInError(boolean inError) {
        this.inError = inError;
    }
    public String getErrorMessage() {
        return errorMessage;
    }
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    public void setPort(String p4Port) {
        this.P4Port = "-p" + p4Port;
    }
    public void setClient(String p4Client) {
        this.P4Client = "-c" + p4Client;
    }
    public void setUser(String p4User) {
        this.P4User = "-u" + p4User;
    }
    public void setGlobalopts(String p4Opts) {
        this.P4Opts = p4Opts;
    }
    public void setView(String p4View) {
        this.P4View = p4View;
    }
    public void setCmdopts(String p4CmdOpts) {
        this.P4CmdOpts = p4CmdOpts;
    }
    public void setFailonerror(boolean fail) {
        failOnError = fail;
    }
    public void init() {
        util = new Perl5Util();
        String tmpprop;
        if ((tmpprop = getProject().getProperty("p4.port")) != null) {
            setPort(tmpprop);
        }
        if ((tmpprop = getProject().getProperty("p4.client")) != null) {
            setClient(tmpprop);
        }
        if ((tmpprop = getProject().getProperty("p4.user")) != null) {
            setUser(tmpprop);
        }
    }
    protected void execP4Command(String command) throws BuildException {
        execP4Command(command, null);
    }
    protected void execP4Command(String command, P4Handler handler) throws BuildException {
        try {
            inError = false;
            errorMessage = "";
            Commandline commandline = new Commandline();
            commandline.setExecutable("p4");
            if (P4Port != null && P4Port.length() != 0) {
                commandline.createArgument().setValue(P4Port);
            }
            if (P4User != null && P4User.length() != 0) {
                commandline.createArgument().setValue(P4User);
            }
            if (P4Client != null && P4Client.length() != 0) {
                commandline.createArgument().setValue(P4Client);
            }
            if (P4Opts != null && P4Opts.length() != 0) {
                commandline.createArgument().setLine(P4Opts);
            }
            commandline.createArgument().setLine(command);
            log(commandline.describeCommand(), Project.MSG_VERBOSE);
            if (handler == null) {
                handler = new SimpleP4OutputHandler(this);
            }
            Execute exe = new Execute(handler, null);
            exe.setAntRun(getProject());
            exe.setCommandline(commandline.getCommandline());
            try {
                exe.execute();
                if (inError && failOnError) {
                    throw new BuildException(errorMessage);
                }
            } catch (IOException e) {
                throw new BuildException(e);
            } finally {
                try {
                    handler.stop();
                } catch (Exception e) {
                    log("Error stopping execution framework: " + e.toString(),
                        Project.MSG_ERR);
                }
            }
        } catch (Exception e) {
            String failMsg = "Problem exec'ing P4 command: " + e.getMessage();
            if (failOnError) {
                if (e instanceof BuildException) {
                    throw (BuildException) e;
                } else {
                    throw new BuildException(failMsg, e);
                }
            } else {
                log(failMsg, Project.MSG_ERR);
            }
        }
    }
}
