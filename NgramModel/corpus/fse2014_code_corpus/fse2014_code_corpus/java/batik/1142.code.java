package org.apache.batik.svggen;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.batik.ext.awt.image.spi.ImageWriter;
import org.apache.batik.ext.awt.image.spi.ImageWriterRegistry;
public class CachedImageHandlerPNGEncoder extends DefaultCachedImageHandler {
    public static final String CACHED_PNG_PREFIX = "pngImage";
    public static final String CACHED_PNG_SUFFIX = ".png";
    protected String refPrefix = "";
    public CachedImageHandlerPNGEncoder(String imageDir, String urlRoot)
        throws SVGGraphics2DIOException {
        refPrefix = urlRoot + "/";
        setImageCacher(new ImageCacher.External(imageDir,
                                                CACHED_PNG_PREFIX,
                                                CACHED_PNG_SUFFIX));
    }
    public void encodeImage(BufferedImage buf, OutputStream os)
            throws IOException {
        ImageWriter writer = ImageWriterRegistry.getInstance()
            .getWriterFor("image/png");
        writer.writeImage(buf, os);
    }
    public int getBufferedImageType(){
        return BufferedImage.TYPE_INT_ARGB;
    }
    public String getRefPrefix(){
        return refPrefix;
    }
}
