package org.apache.batik.transcoder.image;
import org.apache.batik.transcoder.TranscoderInput;
public class URITest extends AbstractImageTranscoderTest {
    protected String inputURI;
    protected String refImageURI;
    public URITest(String inputURI, String refImageURI) {
        this.inputURI = inputURI;
        this.refImageURI = refImageURI;
    }
    protected TranscoderInput createTranscoderInput() {
        return new TranscoderInput(resolveURL(inputURI).toString());
    }
    protected byte [] getReferenceImageData() {
        return createBufferedImageData(resolveURL(refImageURI));
    }
}
