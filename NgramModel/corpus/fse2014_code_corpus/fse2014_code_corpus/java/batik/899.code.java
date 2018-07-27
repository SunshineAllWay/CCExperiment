package org.apache.batik.ext.awt.image.spi;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
public interface ImageWriter {
    void writeImage(RenderedImage image, OutputStream out)
            throws IOException;
    void writeImage(RenderedImage image, OutputStream out,
            ImageWriterParams params)
            throws IOException;
    String getMIMEType();
}
