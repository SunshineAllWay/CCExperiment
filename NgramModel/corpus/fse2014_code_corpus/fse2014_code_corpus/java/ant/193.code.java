package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.ResourceUtils;
import org.apache.tools.ant.util.StringUtils;
import org.apache.tools.ant.types.LogLevel;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.LogOutputResource;
import org.apache.tools.ant.types.resources.StringResource;
public class Echo extends Task {
    protected String message = "";
    protected File file = null;
    protected boolean append = false;
    private String encoding = "";
    private boolean force = false;
    protected int logLevel = Project.MSG_WARN;
    private Resource output;
    public void execute() throws BuildException {
        final String msg = "".equals(message) ? StringUtils.LINE_SEP : message;
        try {
            ResourceUtils
                    .copyResource(new StringResource(msg), output == null
                                  ? new LogOutputResource(this, logLevel)
                                  : output,
                                  null, null, false, false, append, null,
                                  "".equals(encoding) ? null : encoding,
                                  getProject(), force);
        } catch (IOException ioe) {
            throw new BuildException(ioe, getLocation());
        }
    }
    public void setMessage(String msg) {
        this.message = msg == null ? "" : msg;
    }
    public void setFile(File file) {
        setOutput(new FileResource(getProject(), file));
    }
    public void setOutput(Resource output) {
        if (this.output != null) {
            throw new BuildException("Cannot set > 1 output target");
        }
        this.output = output;
        FileProvider fp = (FileProvider) output.as(FileProvider.class);
        this.file = fp != null ? fp.getFile() : null;
    }
    public void setAppend(boolean append) {
        this.append = append;
    }
    public void addText(String msg) {
        message += getProject().replaceProperties(msg);
    }
    public void setLevel(EchoLevel echoLevel) {
        logLevel = echoLevel.getLevel();
    }
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    public void setForce(boolean f) {
        force = f;
    }
    public static class EchoLevel extends LogLevel {
    }
}
