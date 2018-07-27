package org.apache.batik.transcoder.image;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.batik.ext.awt.image.spi.ImageWriter;
import org.apache.batik.ext.awt.image.spi.ImageWriterParams;
import org.apache.batik.ext.awt.image.spi.ImageWriterRegistry;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.resources.Messages;
public class JPEGTranscoder extends ImageTranscoder {
    public JPEGTranscoder() {
        hints.put(ImageTranscoder.KEY_BACKGROUND_COLOR, Color.white);
    }
    public BufferedImage createImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }
    public void writeImage(BufferedImage img, TranscoderOutput output)
            throws TranscoderException {
        OutputStream ostream = output.getOutputStream();
        ostream = new OutputStreamWrapper(ostream);
        if (ostream == null) {
            throw new TranscoderException(
                Messages.formatMessage("jpeg.badoutput", null));
        }
        try {
            float quality;
            if (hints.containsKey(KEY_QUALITY)) {
                quality = ((Float)hints.get(KEY_QUALITY)).floatValue();
            } else {
                TranscoderException te;
                te = new TranscoderException
                    (Messages.formatMessage("jpeg.unspecifiedQuality", null));
                handler.error(te);
                quality = 0.75f;
            }
            ImageWriter writer = ImageWriterRegistry.getInstance()
                .getWriterFor("image/jpeg");
            ImageWriterParams params = new ImageWriterParams();
            params.setJPEGQuality(quality, true);
            float PixSzMM = userAgent.getPixelUnitToMillimeter();
            int PixSzInch = (int)(25.4 / PixSzMM + 0.5);
            params.setResolution(PixSzInch);
            writer.writeImage(img, ostream, params);
            ostream.flush();
        } catch (IOException ex) {
            throw new TranscoderException(ex);
        }
    }
    public static final TranscodingHints.Key KEY_QUALITY
        = new QualityKey();
    private static class QualityKey extends TranscodingHints.Key {
        public boolean isCompatibleValue(Object v) {
            if (v instanceof Float) {
                float q = ((Float)v).floatValue();
                return (q > 0 && q <= 1.0f);
            } else {
                return false;
            }
        }
    }
    private static class OutputStreamWrapper extends OutputStream {
        OutputStream os;
        OutputStreamWrapper(OutputStream os) {
            this.os = os;
        }
        public void close() throws IOException {
            if (os == null) return;
            try {
                os.close();
            } catch (IOException ioe) {
                os = null;
            }
        }
        public void flush() throws IOException {
            if (os == null) return;
            try {
                os.flush();
            } catch (IOException ioe) {
                os = null;
            }
        }
        public void write(byte[] b) throws IOException {
            if (os == null) return;
            try {
                os.write(b);
            } catch (IOException ioe) {
                os = null;
            }
        }
        public void write(byte[] b, int off, int len) throws IOException {
            if (os == null) return;
            try {
                os.write(b, off, len);
            } catch (IOException ioe) {
                os = null;
            }
        }
        public void write(int b)  throws IOException {
            if (os == null) return;
            try {
                os.write(b);
            } catch (IOException ioe) {
                os = null;
            }
        }
    }
}
