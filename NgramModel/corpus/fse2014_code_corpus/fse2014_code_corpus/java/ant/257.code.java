package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.taskdefs.email.EmailTask;
public class SendEmail extends EmailTask {
    public void setMailport(Integer value) {
        setMailport(value.intValue());
    }
}
