package org.apache.tools.ant.taskdefs.condition;
import org.apache.tools.ant.taskdefs.Execute;
public class IsFailure implements Condition {
    private int code;
    public void setCode(int c) {
        code = c;
    }
    public int getCode() {
        return code;
    }
    public boolean eval() {
        return Execute.isFailure(code);
    }
}
