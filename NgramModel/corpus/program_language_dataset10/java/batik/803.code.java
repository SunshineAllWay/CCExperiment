package org.apache.batik.ext.awt.image.codec.util;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
public interface ImageEncoder {
    ImageEncodeParam getParam();
    void setParam(ImageEncodeParam param);
    OutputStream getOutputStream();
    void encode(Raster ras, ColorModel cm) throws IOException;
    void encode(RenderedImage im) throws IOException;
}
