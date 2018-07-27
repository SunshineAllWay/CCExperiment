package org.apache.batik.ext.awt.image.codec.tiff;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.rendered.FormatRed;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.TIFFTranscoder;
public class TIFFTranscoderInternalCodecWriteAdapter implements
        TIFFTranscoder.WriteAdapter {
    public void writeImage(TIFFTranscoder transcoder, BufferedImage img,
            TranscoderOutput output) throws TranscoderException {
        TranscodingHints hints = transcoder.getTranscodingHints();
        TIFFEncodeParam params = new TIFFEncodeParam();
        float PixSzMM = transcoder.getUserAgent().getPixelUnitToMillimeter();
        int numPix      = (int)(((1000 * 100) / PixSzMM) + 0.5);
        int denom       = 100 * 100;  
        long [] rational = {numPix, denom};
        TIFFField [] fields = {
            new TIFFField(TIFFImageDecoder.TIFF_RESOLUTION_UNIT,
                          TIFFField.TIFF_SHORT, 1,
                          new char [] { (char)3 }),
            new TIFFField(TIFFImageDecoder.TIFF_X_RESOLUTION,
                          TIFFField.TIFF_RATIONAL, 1,
                          new long [][] { rational }),
            new TIFFField(TIFFImageDecoder.TIFF_Y_RESOLUTION,
                          TIFFField.TIFF_RATIONAL, 1,
                          new long [][] { rational })
                };
        params.setExtraFields(fields);
        if (hints.containsKey(TIFFTranscoder.KEY_COMPRESSION_METHOD)) {
            String method = (String)hints.get(TIFFTranscoder.KEY_COMPRESSION_METHOD);
            if ("packbits".equals(method)) {
                params.setCompression(TIFFEncodeParam.COMPRESSION_PACKBITS);
            } else if ("deflate".equals(method)) {
                params.setCompression(TIFFEncodeParam.COMPRESSION_DEFLATE);
            } else {
            }
        }
        try {
            int w = img.getWidth();
            int h = img.getHeight();
            SinglePixelPackedSampleModel sppsm;
            sppsm = (SinglePixelPackedSampleModel)img.getSampleModel();
            OutputStream ostream = output.getOutputStream();
            TIFFImageEncoder tiffEncoder =
                new TIFFImageEncoder(ostream, params);
            int bands = sppsm.getNumBands();
            int [] off = new int[bands];
            for (int i = 0; i < bands; i++)
                off[i] = i;
            SampleModel sm = new PixelInterleavedSampleModel
                (DataBuffer.TYPE_BYTE, w, h, bands, w * bands, off);
            RenderedImage rimg = new FormatRed(GraphicsUtil.wrap(img), sm);
            tiffEncoder.encode(rimg);
            ostream.flush();
        } catch (IOException ex) {
            throw new TranscoderException(ex);
        }
    }
}
