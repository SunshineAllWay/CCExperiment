package org.apache.batik.transcoder;
import java.util.Map;
public interface Transcoder {
    void transcode(TranscoderInput input, TranscoderOutput output)
            throws TranscoderException;
    TranscodingHints getTranscodingHints();
    void addTranscodingHint(TranscodingHints.Key key, Object value);
    void removeTranscodingHint(TranscodingHints.Key key);
    void setTranscodingHints(Map hints);
    void setTranscodingHints(TranscodingHints hints);
    void setErrorHandler(ErrorHandler handler);
    ErrorHandler getErrorHandler();
}
