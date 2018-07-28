package org.apache.batik.transcoder.image;
import java.util.HashMap;
import java.util.Map;
import org.apache.batik.transcoder.TranscoderInput;
public class MediaTest extends AbstractImageTranscoderTest {
    protected String inputURI;
    protected String refImageURI;
    protected String media;
    public MediaTest(String inputURI, String refImageURI, String media) {
        this.inputURI = inputURI;
        this.refImageURI = refImageURI;
        this.media = media;
    }
    protected TranscoderInput createTranscoderInput() {
        return new TranscoderInput(resolveURL(inputURI).toString());
    }
    protected Map createTranscodingHints() {
        Map hints = new HashMap(3);
        hints.put(ImageTranscoder.KEY_MEDIA, media);
        return hints;
    }
    protected byte [] getReferenceImageData() {
        return createBufferedImageData(resolveURL(refImageURI));
    }
}
