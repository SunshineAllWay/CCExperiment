package org.apache.batik.transcoder;
public class TranscoderException extends Exception {
    protected Exception ex;
    public TranscoderException(String s) {
        this(s, null);
    }
    public TranscoderException(Exception ex) {
        this(null, ex);
    }
    public TranscoderException(String s, Exception ex) {
        super(s);
        this.ex = ex;
    }
    public String getMessage() {
        String msg = super.getMessage();
        if (ex != null) {
            msg += "\nEnclosed Exception:\n";
            msg += ex.getMessage();
        }
        return msg;
    }
    public Exception getException() {
        return ex;
    }
}
