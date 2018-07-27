package org.apache.tools.ant.util;
import java.io.IOException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
public class RetryHandler {
    private int retriesAllowed = 0;
    private Task task;
    public RetryHandler(int retriesAllowed, Task task) {
        this.retriesAllowed = retriesAllowed;
        this.task = task;
    }
    public void execute(Retryable exe, String desc) throws IOException {
        int retries = 0;
        while (true) {
            try {
                exe.execute();
                break;
            } catch (IOException e) {
                retries++;
                if (retries > this.retriesAllowed && this.retriesAllowed > -1) {
                    task.log("try #" + retries + ": IO error ("
                            + desc + "), number of maximum retries reached ("
                            + this.retriesAllowed + "), giving up", Project.MSG_WARN);
                    throw e;
                } else {
                    task.log("try #" + retries + ": IO error (" + desc
                             + "), retrying", Project.MSG_WARN);
                }
            }
        }
    }
}
