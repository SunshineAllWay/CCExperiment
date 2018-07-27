package org.apache.batik.ext.awt.image.codec.util;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
public abstract class ImageDecoderImpl implements ImageDecoder {
    protected SeekableStream input;
    protected ImageDecodeParam param;
    public ImageDecoderImpl(SeekableStream input,
                            ImageDecodeParam param) {
        this.input = input;
        this.param = param;
    }
    public ImageDecoderImpl(InputStream input,
                            ImageDecodeParam param) {
        this.input = new ForwardSeekableStream(input);
        this.param = param;
    }
    public ImageDecodeParam getParam() {
        return param;
    }
    public void setParam(ImageDecodeParam param) {
        this.param = param;
    }
    public SeekableStream getInputStream() {
        return input;
    }
    public int getNumPages() throws IOException {
        return 1;
    }
    public Raster decodeAsRaster() throws IOException {
        return decodeAsRaster(0);
    }
    public Raster decodeAsRaster(int page) throws IOException {
        RenderedImage im = decodeAsRenderedImage(page);
        return im.getData();
    }
    public RenderedImage decodeAsRenderedImage() throws IOException {
        return decodeAsRenderedImage(0);
    }
    public abstract RenderedImage decodeAsRenderedImage(int page)
        throws IOException;
}
