package org.apache.batik.ext.awt.image.rendered;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import org.apache.batik.ext.awt.ColorSpaceHintKey;
public class FilterAsAlphaRed extends AbstractRed {
    public FilterAsAlphaRed(CachableRed src) {
        super(new Any2LumRed(src),src.getBounds(), 
              new ComponentColorModel
                  (ColorSpace.getInstance(ColorSpace.CS_GRAY),
                   new int [] {8}, false, false,
                   Transparency.OPAQUE, 
                   DataBuffer.TYPE_BYTE),
              new PixelInterleavedSampleModel
                  (DataBuffer.TYPE_BYTE, 
                   src.getSampleModel().getWidth(),
                   src.getSampleModel().getHeight(),
                   1, src.getSampleModel().getWidth(),
                   new int [] { 0 }),
              src.getTileGridXOffset(),
              src.getTileGridYOffset(),
              null);
        props.put(ColorSpaceHintKey.PROPERTY_COLORSPACE,
                  ColorSpaceHintKey.VALUE_COLORSPACE_ALPHA);
    }
    public WritableRaster copyData(WritableRaster wr) {
        CachableRed srcRed = (CachableRed)getSources().get(0);
        SampleModel sm = srcRed.getSampleModel();
        if (sm.getNumBands() == 1)
            return srcRed.copyData(wr);
        Raster srcRas = srcRed.getData(wr.getBounds());
        PixelInterleavedSampleModel srcSM;
        srcSM = (PixelInterleavedSampleModel)srcRas.getSampleModel();
        DataBufferByte srcDB = (DataBufferByte)srcRas.getDataBuffer();
        byte []        src   = srcDB.getData();
        PixelInterleavedSampleModel dstSM;
        dstSM = (PixelInterleavedSampleModel)wr.getSampleModel();
        DataBufferByte dstDB = (DataBufferByte)wr.getDataBuffer();
        byte []        dst   = dstDB.getData();
        int srcX0 = srcRas.getMinX()-srcRas.getSampleModelTranslateX();
        int srcY0 = srcRas.getMinY()-srcRas.getSampleModelTranslateY();
        int dstX0 = wr.getMinX()-wr.getSampleModelTranslateX();
        int dstX1 = dstX0+wr.getWidth()-1;
        int dstY0 = wr.getMinY()-wr.getSampleModelTranslateY();
        int    srcStep = srcSM.getPixelStride();
        int [] offsets = srcSM.getBandOffsets();
        int    srcLOff = offsets[0];
        int    srcAOff = offsets[1];
        if (srcRed.getColorModel().isAlphaPremultiplied()) {
            for (int y=0; y<srcRas.getHeight(); y++) {
                int srcI  = srcDB.getOffset() + srcSM.getOffset(srcX0,  srcY0);
                int dstI  = dstDB.getOffset() + dstSM.getOffset(dstX0,  dstY0);
                int dstE  = dstDB.getOffset() + dstSM.getOffset(dstX1+1,dstY0);
                srcI += srcLOff; 
                while (dstI < dstE) {
                    dst[dstI++] = src[srcI];
                        srcI += srcStep; 
                }
                srcY0++;
                dstY0++;
            }
        }
        else {
            srcAOff = srcAOff-srcLOff;
            for (int y=0; y<srcRas.getHeight(); y++) {
                int srcI  = srcDB.getOffset() + srcSM.getOffset(srcX0,  srcY0);
                int dstI  = dstDB.getOffset() + dstSM.getOffset(dstX0,  dstY0);
                int dstE  = dstDB.getOffset() + dstSM.getOffset(dstX1+1,dstY0);
                srcI += srcLOff;
                while (dstI < dstE) {
                    int sl = (src[srcI])&0xFF; 
                    int sa = (src[srcI+srcAOff])&0xFF;
                    dst[dstI++] = (byte)((sl*sa+0x80)>>8);
                    srcI+= srcStep; 
                }
                srcY0++;
                dstY0++;
            }
        }
        return wr;
    }
}    
