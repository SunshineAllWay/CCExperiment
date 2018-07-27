package org.apache.batik.transcoder.image;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.HashMap;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.DOMImplementation;
public class BackgroundColorTest extends AbstractImageTranscoderTest {
    public BackgroundColorTest() {
    }
    protected TranscoderInput createTranscoderInput() {
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        Document doc = impl.createDocument(svgNS, "svg", null);
        Element root = doc.getDocumentElement();
        root.setAttributeNS(null, "width", "400");
        root.setAttributeNS(null, "height", "400");
        Element r = doc.createElementNS(svgNS, "rect");
        r.setAttributeNS(null, "x", "100");
        r.setAttributeNS(null, "y", "50");
        r.setAttributeNS(null, "width", "100");
        r.setAttributeNS(null, "height", "50");
        r.setAttributeNS(null, "style", "fill:red");
        root.appendChild(r);
        return new TranscoderInput(doc);
    }
    protected Map createTranscodingHints() {
        Map hints = new HashMap(7);
        hints.put(ImageTranscoder.KEY_BACKGROUND_COLOR, Color.blue);
        return hints;
    }
    protected byte [] getReferenceImageData() {
        try {
            BufferedImage img = new BufferedImage
                (400, 400, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics();
            g2d.setColor(Color.blue);
            g2d.fillRect(0, 0, 400, 400);
            g2d.setColor(Color.red);
            g2d.fillRect(100, 50, 100, 50);
            ByteArrayOutputStream ostream = new ByteArrayOutputStream();
            PNGTranscoder t = new PNGTranscoder();
            TranscoderOutput output = new TranscoderOutput(ostream);
            t.writeImage(img, output);
            return ostream.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("BackgroundColorTest error");
        }
    }
}
