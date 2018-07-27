package org.apache.batik.svggen;
import java.io.IOException;
public class SVGGraphics2DIOException extends IOException {
    private IOException embedded;
    public SVGGraphics2DIOException(String s) {
        this(s, null);
    }
    public SVGGraphics2DIOException(IOException ex) {
        this(null, ex);
    }
    public SVGGraphics2DIOException(String s, IOException ex) {
        super(s);
        embedded = ex;
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
    public IOException getException() {
        return embedded;
    }
}
