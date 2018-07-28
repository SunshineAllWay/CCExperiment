package org.apache.batik.transcoder.image;
import java.io.IOException;
import org.w3c.dom.Document;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.util.XMLResourceDescriptor;
public class ParametrizedDOMTest extends AbstractImageTranscoderTest {
    protected String inputURI;
    protected String refImageURI;
    public ParametrizedDOMTest(String inputURI, String refImageURI) {
        this.inputURI = inputURI;
        this.refImageURI = refImageURI;
    }
    protected TranscoderInput createTranscoderInput() {
        try {
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
            Document doc = f.createDocument(resolveURL(inputURI).toString());
            return new TranscoderInput(doc);
        } catch (IOException ex) {
            throw new IllegalArgumentException(inputURI);
        }
    }
    protected byte [] getReferenceImageData() {
        return createBufferedImageData(resolveURL(refImageURI));
    }
}
