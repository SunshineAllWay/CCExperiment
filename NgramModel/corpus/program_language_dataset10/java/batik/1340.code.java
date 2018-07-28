package org.apache.batik.transcoder.image;
import java.awt.image.BufferedImage;
import java.awt.image.SinglePixelPackedSampleModel;
import java.io.OutputStream;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.resources.Messages;
import org.apache.batik.transcoder.keys.FloatKey;
import org.apache.batik.transcoder.keys.IntegerKey;
public class PNGTranscoder extends ImageTranscoder {
    public PNGTranscoder() {
        hints.put(KEY_FORCE_TRANSPARENT_WHITE, Boolean.FALSE);
    }
    public UserAgent getUserAgent() {
        return this.userAgent;
    }
    public BufferedImage createImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }
    private WriteAdapter getWriteAdapter(String className) {
        WriteAdapter adapter;
        try {
            Class clazz = Class.forName(className);
            adapter = (WriteAdapter)clazz.newInstance();
            return adapter;
        } catch (ClassNotFoundException e) {
            return null;
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }
    public void writeImage(BufferedImage img, TranscoderOutput output)
            throws TranscoderException {
        OutputStream ostream = output.getOutputStream();
        if (ostream == null) {
            throw new TranscoderException(
                Messages.formatMessage("png.badoutput", null));
        }
        boolean forceTransparentWhite = false;
        if (hints.containsKey(PNGTranscoder.KEY_FORCE_TRANSPARENT_WHITE)) {
            forceTransparentWhite =
                ((Boolean)hints.get
                 (PNGTranscoder.KEY_FORCE_TRANSPARENT_WHITE)).booleanValue();
        }
        if (forceTransparentWhite) {
            SinglePixelPackedSampleModel sppsm;
            sppsm = (SinglePixelPackedSampleModel)img.getSampleModel();
            forceTransparentWhite(img, sppsm);
        }
        WriteAdapter adapter = getWriteAdapter(
                "org.apache.batik.ext.awt.image.codec.png.PNGTranscoderInternalCodecWriteAdapter");
        if (adapter == null) {
            adapter = getWriteAdapter(
                "org.apache.batik.transcoder.image.PNGTranscoderImageIOWriteAdapter");
        }
        if (adapter == null) {
            throw new TranscoderException(
                    "Could not write PNG file because no WriteAdapter is availble");
        }
        adapter.writeImage(this, img, output);
    }
    public interface WriteAdapter {
        void writeImage(PNGTranscoder transcoder, BufferedImage img, 
                TranscoderOutput output) throws TranscoderException;
    }
    public static final TranscodingHints.Key KEY_GAMMA
        = new FloatKey();
    public static final float[] DEFAULT_CHROMA = {
        0.31270F, 0.329F, 0.64F, 0.33F, 0.3F, 0.6F, 0.15F, 0.06F
    };
    public static final TranscodingHints.Key KEY_INDEXED
        = new IntegerKey();
}
