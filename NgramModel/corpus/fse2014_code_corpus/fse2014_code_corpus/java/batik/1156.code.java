package org.apache.batik.svggen;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.batik.ext.awt.image.spi.ImageWriter;
import org.apache.batik.ext.awt.image.spi.ImageWriterRegistry;
import org.apache.batik.util.Base64EncoderStream;
import org.w3c.dom.Element;
public class ImageHandlerBase64Encoder extends DefaultImageHandler {
    public ImageHandlerBase64Encoder() {
        super();
    }
    public void handleHREF(Image image, Element imageElement,
                              SVGGeneratorContext generatorContext)
        throws SVGGraphics2DIOException {
        if (image == null)
            throw new SVGGraphics2DRuntimeException(ERR_IMAGE_NULL);
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        if (width==0 || height==0) {
            handleEmptyImage(imageElement);
        } else {
            if (image instanceof RenderedImage) {
                handleHREF((RenderedImage)image, imageElement,
                           generatorContext);
            } else {
                BufferedImage buf =
                    new BufferedImage(width, height,
                                      BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = buf.createGraphics();
                g.drawImage(image, 0, 0, null);
                g.dispose();
                handleHREF((RenderedImage)buf, imageElement,
                           generatorContext);
            }
        }
    }
    public void handleHREF(RenderableImage image, Element imageElement,
                              SVGGeneratorContext generatorContext)
        throws SVGGraphics2DIOException {
        if (image == null){
            throw new SVGGraphics2DRuntimeException(ERR_IMAGE_NULL);
        }
        RenderedImage r = image.createDefaultRendering();
        if (r == null) {
            handleEmptyImage(imageElement);
        } else {
            handleHREF(r, imageElement, generatorContext);
        }
    }
    protected void handleEmptyImage(Element imageElement) {
        imageElement.setAttributeNS(XLINK_NAMESPACE_URI,
                                    XLINK_HREF_QNAME, DATA_PROTOCOL_PNG_PREFIX);
        imageElement.setAttributeNS(null, SVG_WIDTH_ATTRIBUTE, "0");
        imageElement.setAttributeNS(null, SVG_HEIGHT_ATTRIBUTE, "0");
    }
    public void handleHREF(RenderedImage image, Element imageElement,
                              SVGGeneratorContext generatorContext)
        throws SVGGraphics2DIOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Base64EncoderStream b64Encoder = new Base64EncoderStream(os);
        try {
            encodeImage(image, b64Encoder);
            b64Encoder.close();
        } catch (IOException e) {
            throw new SVGGraphics2DIOException(ERR_UNEXPECTED, e);
        }
        imageElement.setAttributeNS(XLINK_NAMESPACE_URI,
                                    XLINK_HREF_QNAME,
                                    DATA_PROTOCOL_PNG_PREFIX +
                                    os.toString());
    }
    public void encodeImage(RenderedImage buf, OutputStream os)
        throws SVGGraphics2DIOException {
        try{
            ImageWriter writer = ImageWriterRegistry.getInstance()
                .getWriterFor("image/png");
            writer.writeImage(buf, os);
        } catch(IOException e) {
            throw new SVGGraphics2DIOException(ERR_UNEXPECTED);
        }
    }
    public BufferedImage buildBufferedImage(Dimension size) {
        return new BufferedImage(size.width, size.height,
                                 BufferedImage.TYPE_INT_ARGB);
    }
}
