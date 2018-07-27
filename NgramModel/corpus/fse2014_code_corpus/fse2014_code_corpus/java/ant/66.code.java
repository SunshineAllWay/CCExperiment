package org.apache.tools.ant;
public class ExitException extends SecurityException {
    private static final long serialVersionUID = 2772487854280543363L;
    private int status;
    public ExitException(int status) {
        super("ExitException: status " + status);
        this.status = status;
    }
    public ExitException(String msg, int status) {
        super(msg);
        this.status = status;
    }
    public int getStatus() {
        return status;
    }
}
