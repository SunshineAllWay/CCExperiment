package org.apache.batik.transcoder;
import java.util.Map;
public class TranscoderSupport {
    static final ErrorHandler defaultErrorHandler = new DefaultErrorHandler();
    protected TranscodingHints hints = new TranscodingHints();
    protected ErrorHandler handler = defaultErrorHandler;
    public TranscoderSupport() { }
    public TranscodingHints getTranscodingHints() {
        return new TranscodingHints(hints);
    }
    public void addTranscodingHint(TranscodingHints.Key key, Object value) {
        hints.put(key, value);
    }
    public void removeTranscodingHint(TranscodingHints.Key key) {
        hints.remove(key);
    }
    public void setTranscodingHints(Map hints) {
        this.hints.putAll(hints);
    }
    public void setTranscodingHints(TranscodingHints hints) {
        this.hints = hints;
    }
    public void setErrorHandler(ErrorHandler handler) {
        this.handler = handler;
    }
    public ErrorHandler getErrorHandler() {
        return handler;
    }
}
