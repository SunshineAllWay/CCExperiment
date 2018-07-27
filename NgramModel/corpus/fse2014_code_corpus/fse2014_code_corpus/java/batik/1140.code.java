package org.apache.batik.svggen;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.batik.ext.awt.image.spi.ImageWriter;
import org.apache.batik.ext.awt.image.spi.ImageWriterRegistry;
import org.apache.batik.util.Base64EncoderStream;
import org.w3c.dom.Element;
public class CachedImageHandlerBase64Encoder extends DefaultCachedImageHandler {
    public CachedImageHandlerBase64Encoder() {
        super();
        setImageCacher(new ImageCacher.Embedded());
    }
    public Element createElement(SVGGeneratorContext generatorContext) {
        Element imageElement =
            generatorContext.getDOMFactory().createElementNS(
                                                             SVG_NAMESPACE_URI, SVG_USE_TAG);
        return imageElement;
    }
    public String getRefPrefix(){
        return "";
    }
    protected AffineTransform handleTransform(Element imageElement,
                                              double x, double y,
                                              double srcWidth,
                                              double srcHeight,
                                              double dstWidth,
                                              double dstHeight,
                                              SVGGeneratorContext generatorContext) {
        AffineTransform af  = new AffineTransform();
        double hRatio = dstWidth / srcWidth;
        double vRatio = dstHeight / srcHeight;
        af.translate(x,y);
        if(hRatio != 1 || vRatio != 1) {
            af.scale(hRatio, vRatio);
        } 
        if (!af.isIdentity()){
            return af;
        } else {
            return null;
        }
    }
    public void encodeImage(BufferedImage buf, OutputStream os)
            throws IOException {
        Base64EncoderStream b64Encoder = new Base64EncoderStream(os);
        ImageWriter writer = ImageWriterRegistry.getInstance()
            .getWriterFor("image/png");
        writer.writeImage(buf, b64Encoder);
        b64Encoder.close();
    }
    public int getBufferedImageType(){
        return BufferedImage.TYPE_INT_ARGB;
    }
}
