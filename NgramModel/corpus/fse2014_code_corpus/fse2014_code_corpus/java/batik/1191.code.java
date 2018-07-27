package org.apache.batik.svggen;
public class SVGGraphics2DRuntimeException extends RuntimeException {
    private Exception embedded;
    public SVGGraphics2DRuntimeException(String s) {
        this(s, null);
    }
    public SVGGraphics2DRuntimeException(Exception ex) {
        this(null, ex);
    }
    public SVGGraphics2DRuntimeException(String s, Exception ex) {
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
    public Exception getException() {
        return embedded;
    }
}
