package org.apache.tools.ant.taskdefs;
import java.util.Random;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
public class DemuxOutputTask extends Task {
    private String randomOutValue;
    private String randomErrValue;
    private boolean outputReceived = false;
    private boolean errorReceived = false;
    public void execute() {
        Random generator = new Random();
        randomOutValue = "Output Value is " + generator.nextInt();
        randomErrValue = "Error Value is " + generator.nextInt();
        System.out.println(randomOutValue);
        System.err.println(randomErrValue);
        if (!outputReceived) {
            throw new BuildException("Did not receive output");
        }
        if (!errorReceived) {
            throw new BuildException("Did not receive error");
        }
    }
    protected void handleOutput(String line) {
        line = line.trim();
        if (line.length() != 0 && !line.equals(randomOutValue)) {
            String message = "Received = [" + line + "], expected = ["
                + randomOutValue + "]";
            throw new BuildException(message);
        }
        outputReceived = true;
    }
    protected void handleErrorOutput(String line) {
        line = line.trim();
        if (line.length() != 0 && !line.equals(randomErrValue)) {
            String message = "Received = [" + line + "], expected = ["
                + randomErrValue + "]";
            throw new BuildException(message);
        }
        errorReceived = true;
    }
}
