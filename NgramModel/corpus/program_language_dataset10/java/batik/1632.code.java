package org.apache.batik.transcoder.image;
import java.util.Map;
import java.util.HashMap;
import org.apache.batik.transcoder.TranscoderInput;
public class AlternateStylesheetTest extends AbstractImageTranscoderTest {
    protected String inputURI;
    protected String refImageURI;
    protected String alternateStylesheet;
    public AlternateStylesheetTest(String inputURI, 
                                   String refImageURI, 
                                   String alternateStylesheet) {
        this.inputURI = inputURI;
        this.refImageURI = refImageURI;
        this.alternateStylesheet = alternateStylesheet;
    }
    protected TranscoderInput createTranscoderInput() {
        return new TranscoderInput(resolveURL(inputURI).toString());
    }
    protected Map createTranscodingHints() {
        Map hints = new HashMap(3);
        hints.put(ImageTranscoder.KEY_ALTERNATE_STYLESHEET, 
                  alternateStylesheet);
        return hints;
    }
    protected byte [] getReferenceImageData() {
        return createBufferedImageData(resolveURL(refImageURI));
    }
}
