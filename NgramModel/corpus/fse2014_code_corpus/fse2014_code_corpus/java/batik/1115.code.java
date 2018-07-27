package org.apache.batik.script;
public class InterpreterException extends RuntimeException {
    private int line = -1; 
    private int column = -1; 
    private Exception embedded = null; 
    public InterpreterException(String message, int lineno, int columnno) {
        super(message);
        line = lineno;
        column = columnno;
    }
    public InterpreterException(Exception exception,
                                String message, int lineno, int columnno) {
        this(message, lineno, columnno);
        embedded = exception;
    }
    public int getLineNumber() {
        return line;
    }
    public int getColumnNumber() {
        return column;
    }
    public Exception getException() {
        return embedded;
    }
    public String getMessage() {
        String msg = super.getMessage();
        if (msg != null) {
            return msg;
        } else if (embedded != null) {
            return embedded.getMessage();
        } else {
            return null;
        }
    }
}
