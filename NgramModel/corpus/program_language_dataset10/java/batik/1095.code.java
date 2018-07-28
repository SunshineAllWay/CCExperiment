package org.apache.batik.parser;
public class ParseException extends RuntimeException {
    protected Exception exception;
    protected int lineNumber;
    protected int columnNumber;
    public ParseException (String message, int line, int column) {
        super(message);
        exception = null;
        lineNumber = line;
        columnNumber = column;
    }
    public ParseException (Exception e) {
        exception = e;
        lineNumber = -1;
        columnNumber = -1;
    }
    public ParseException (String message, Exception e) {
        super(message);
        this.exception = e;
    }
    public String getMessage () {
        String message = super.getMessage();
        if (message == null && exception != null) {
            return exception.getMessage();
        } else {
            return message;
        }
    }
    public Exception getException () {
        return exception;
    }
    public int getLineNumber() {
        return lineNumber;
    }
    public int getColumnNumber() {
        return columnNumber;
    }
}
