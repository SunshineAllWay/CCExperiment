package org.apache.batik.transcoder.image;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.apache.batik.transcoder.TranscoderInput;
public class InputStreamTest extends AbstractImageTranscoderTest {
    protected String inputURI;
    protected String refImageURI;
    public InputStreamTest(String inputURI, String refImageURI) {
        this.inputURI = inputURI;
        this.refImageURI = refImageURI;
    }
    protected TranscoderInput createTranscoderInput() {
        try {
            URL url = resolveURL(inputURI);
            InputStream istream = url.openStream();
            TranscoderInput input = new TranscoderInput(istream);
            input.setURI(url.toString()); 
            return input;
        } catch (IOException ex) {
            throw new IllegalArgumentException(inputURI);
        }
    }
    protected byte [] getReferenceImageData() {
        return createBufferedImageData(resolveURL(refImageURI));
    }
}
