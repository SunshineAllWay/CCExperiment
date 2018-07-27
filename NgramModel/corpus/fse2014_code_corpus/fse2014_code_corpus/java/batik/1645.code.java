package org.apache.batik.transcoder.image;
import java.util.HashMap;
import java.util.Map;
import org.apache.batik.transcoder.TranscoderInput;
public class PixelToMMTest extends AbstractImageTranscoderTest {
    protected String inputURI;
    protected String refImageURI;
    protected Float px2mm;
    public PixelToMMTest(String inputURI, 
                         String refImageURI, 
                         Float px2mm) {
        this.inputURI = inputURI;
        this.refImageURI = refImageURI;
        this.px2mm = px2mm;
    }
    protected TranscoderInput createTranscoderInput() {
        return new TranscoderInput(resolveURL(inputURI).toString());
    }
    protected Map createTranscodingHints() {
        Map hints = new HashMap(3);
        hints.put(ImageTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, px2mm);
        return hints;
    }
    protected byte [] getReferenceImageData() {
        return createBufferedImageData(resolveURL(refImageURI));
    }
}
