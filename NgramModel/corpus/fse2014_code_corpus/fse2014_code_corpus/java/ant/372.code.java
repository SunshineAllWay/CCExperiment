package org.apache.tools.ant.taskdefs.optional.ccm;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteStreamHandler;
import org.apache.tools.ant.types.Commandline;
public class CCMCreateTask extends Continuus implements ExecuteStreamHandler {
    private String comment = null;
    private String platform = null;
    private String resolver = null;
    private String release = null;
    private String subSystem = null;
    private String task = null;
    public CCMCreateTask() {
        super();
        setCcmAction(COMMAND_CREATE_TASK);
    }
    public void execute() throws BuildException {
        Commandline commandLine = new Commandline();
        int result = 0;
        commandLine.setExecutable(getCcmCommand());
        commandLine.createArgument().setValue(getCcmAction());
        checkOptions(commandLine);
        result = run(commandLine, this);
        if (Execute.isFailure(result)) {
            String msg = "Failed executing: " + commandLine.toString();
            throw new BuildException(msg, getLocation());
        }
        Commandline commandLine2 = new Commandline();
        commandLine2.setExecutable(getCcmCommand());
        commandLine2.createArgument().setValue(COMMAND_DEFAULT_TASK);
        commandLine2.createArgument().setValue(getTask());
        log(commandLine.describeCommand(), Project.MSG_DEBUG);
        result = run(commandLine2);
        if (result != 0) {
            String msg = "Failed executing: " + commandLine2.toString();
            throw new BuildException(msg, getLocation());
        }
    }
    private void checkOptions(Commandline cmd) {
        if (getComment() != null) {
            cmd.createArgument().setValue(FLAG_COMMENT);
            cmd.createArgument().setValue("\"" + getComment() + "\"");
        }
        if (getPlatform() != null) {
            cmd.createArgument().setValue(FLAG_PLATFORM);
            cmd.createArgument().setValue(getPlatform());
        } 
        if (getResolver() != null) {
            cmd.createArgument().setValue(FLAG_RESOLVER);
            cmd.createArgument().setValue(getResolver());
        } 
        if (getSubSystem() != null) {
            cmd.createArgument().setValue(FLAG_SUBSYSTEM);
            cmd.createArgument().setValue("\"" + getSubSystem() + "\"");
        } 
        if (getRelease() != null) {
            cmd.createArgument().setValue(FLAG_RELEASE);
            cmd.createArgument().setValue(getRelease());
        } 
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String v) {
        this.comment = v;
    }
    public String getPlatform() {
        return platform;
    }
    public void setPlatform(String v) {
        this.platform = v;
    }
    public String getResolver() {
        return resolver;
    }
    public void setResolver(String v) {
        this.resolver = v;
    }
    public String getRelease() {
        return release;
    }
    public void setRelease(String v) {
        this.release = v;
    }
    public String getSubSystem() {
        return subSystem;
    }
    public void setSubSystem(String v) {
        this.subSystem = v;
    }
    public String getTask() {
        return task;
    }
    public void setTask(String v) {
        this.task = v;
    }
    public static final String FLAG_COMMENT = "/synopsis";
    public static final String FLAG_PLATFORM = "/plat";
    public static final String FLAG_RESOLVER = "/resolver";
    public static final String FLAG_RELEASE = "/release";
    public static final String FLAG_SUBSYSTEM = "/subsystem";
    public static final String FLAG_TASK = "/task";
    public void start() throws IOException {
    }
    public void stop() {
    }
    public void setProcessInputStream(OutputStream param1) throws IOException {
    }
    public void setProcessErrorStream(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String s = reader.readLine();
        if (s != null) {
            log("err " + s, Project.MSG_DEBUG);
        } 
    }
    public void setProcessOutputStream(InputStream is) throws IOException {
        String buffer = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            buffer = reader.readLine();
            if (buffer != null) {
                log("buffer:" + buffer, Project.MSG_DEBUG);
                String taskstring = buffer.substring(buffer.indexOf(' ')).trim();
                taskstring = taskstring.substring(0, taskstring.lastIndexOf(' ')).trim();
                setTask(taskstring);
                log("task is " + getTask(), Project.MSG_DEBUG);
            } 
        } catch (NullPointerException npe) {
            log("error procession stream , null pointer exception", Project.MSG_ERR);
            npe.printStackTrace();
            throw new BuildException(npe.getClass().getName());
        } catch (Exception e) {
            log("error procession stream " + e.getMessage(), Project.MSG_ERR);
            throw new BuildException(e.getMessage());
        } 
    }
}
