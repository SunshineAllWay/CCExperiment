package org.apache.batik.transcoder.image;
import java.util.HashMap;
import java.util.Map;
import org.apache.batik.transcoder.TranscoderInput;
public class DefaultFontFamilyTest extends AbstractImageTranscoderTest {
    protected String inputURI;
    protected String refImageURI;
    protected String defaultFontFamily;
    public DefaultFontFamilyTest(String inputURI, 
                                 String refImageURI, 
                                 String defaultFontFamily) {
        this.inputURI = inputURI;
        this.refImageURI = refImageURI;
        this.defaultFontFamily = defaultFontFamily;
    }
    protected TranscoderInput createTranscoderInput() {
        return new TranscoderInput(resolveURL(inputURI).toString());
    }
    protected Map createTranscodingHints() {
        Map hints = new HashMap(3);
        hints.put(ImageTranscoder.KEY_DEFAULT_FONT_FAMILY, defaultFontFamily);
        return hints;
    }
    protected byte [] getReferenceImageData() {
        return createBufferedImageData(resolveURL(refImageURI));
    }
}
