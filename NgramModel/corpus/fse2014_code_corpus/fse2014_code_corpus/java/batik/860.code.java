package org.apache.batik.ext.awt.image.rendered;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandCombineOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import org.apache.batik.ext.awt.ColorSpaceHintKey;
import org.apache.batik.ext.awt.image.GraphicsUtil;
public class Any2LumRed extends AbstractRed {
    public Any2LumRed(CachableRed src) {
        super(src,src.getBounds(), 
              fixColorModel(src),
              fixSampleModel(src),
              src.getTileGridXOffset(),
              src.getTileGridYOffset(),
              null);
        props.put(ColorSpaceHintKey.PROPERTY_COLORSPACE,
                  ColorSpaceHintKey.VALUE_COLORSPACE_GREY);
    }
    public WritableRaster copyData(WritableRaster wr) {
        CachableRed src = (CachableRed)getSources().get(0);
        SampleModel sm     = src.getSampleModel();
        ColorModel  srcCM  = src.getColorModel();
        Raster      srcRas = src.getData(wr.getBounds());
        if (srcCM == null) {
            float [][] matrix = null;
            if (sm.getNumBands() == 2) {
                matrix = new float[2][2];
                matrix[0][0] = 1;
                matrix[1][1] = 1;
            } else {
                matrix = new float[sm.getNumBands()][1];
                matrix[0][0] = 1;
            }
            BandCombineOp op = new BandCombineOp(matrix, null);
            op.filter(srcRas, wr);
        } else {
            WritableRaster srcWr  = (WritableRaster)srcRas;
            if (srcCM.hasAlpha())
                GraphicsUtil.coerceData(srcWr, srcCM, false);
            BufferedImage srcBI, dstBI;
            srcBI = new BufferedImage(srcCM, 
                                      srcWr.createWritableTranslatedChild(0,0),
                                      false, 
                                      null);
            ColorModel dstCM = getColorModel();
            if (!dstCM.hasAlpha()) {
                dstBI = new BufferedImage
                    (dstCM, wr.createWritableTranslatedChild(0,0),
                     dstCM.isAlphaPremultiplied(), null);
            } else {
                PixelInterleavedSampleModel dstSM;
                dstSM = (PixelInterleavedSampleModel)wr.getSampleModel();
                SampleModel smna = new PixelInterleavedSampleModel
                    (dstSM.getDataType(),    
                     dstSM.getWidth(),       dstSM.getHeight(),
                     dstSM.getPixelStride(), dstSM.getScanlineStride(),
                     new int [] { 0 });
                WritableRaster dstWr;
                dstWr = Raster.createWritableRaster(smna,
                                                    wr.getDataBuffer(),
                                                    new Point(0,0));
                dstWr = dstWr.createWritableChild
                    (wr.getMinX()-wr.getSampleModelTranslateX(),
                     wr.getMinY()-wr.getSampleModelTranslateY(),
                     wr.getWidth(), wr.getHeight(),
                     0, 0, null);
                ColorModel cmna = new ComponentColorModel
                    (ColorSpace.getInstance(ColorSpace.CS_GRAY),
                     new int [] {8}, false, false,
                     Transparency.OPAQUE, 
                     DataBuffer.TYPE_BYTE);
                dstBI = new BufferedImage(cmna, dstWr, false, null);
            }
            ColorConvertOp op = new ColorConvertOp(null);
            op.filter(srcBI, dstBI);
            if (dstCM.hasAlpha()) {
                copyBand(srcWr, sm.getNumBands()-1,
                         wr,    getSampleModel().getNumBands()-1);
                if (dstCM.isAlphaPremultiplied())
                    GraphicsUtil.multiplyAlpha(wr);
            }
        }
        return wr;
    }
    protected static ColorModel fixColorModel(CachableRed src) {
        ColorModel  cm = src.getColorModel();
        if (cm != null) {
            if (cm.hasAlpha())
                return new ComponentColorModel
                    (ColorSpace.getInstance(ColorSpace.CS_GRAY),
                     new int [] {8,8}, true,
                     cm.isAlphaPremultiplied(),
                     Transparency.TRANSLUCENT, 
                     DataBuffer.TYPE_BYTE);
            return new ComponentColorModel
                (ColorSpace.getInstance(ColorSpace.CS_GRAY),
                 new int [] {8}, false, false,
                 Transparency.OPAQUE, 
                 DataBuffer.TYPE_BYTE);
        } 
        else {
            SampleModel sm = src.getSampleModel();
            if (sm.getNumBands() == 2)
                return new ComponentColorModel
                    (ColorSpace.getInstance(ColorSpace.CS_GRAY),
                     new int [] {8,8}, true,
                     true, Transparency.TRANSLUCENT, 
                     DataBuffer.TYPE_BYTE);
            return new ComponentColorModel
                (ColorSpace.getInstance(ColorSpace.CS_GRAY),
                 new int [] {8}, false, false,
                 Transparency.OPAQUE, 
                 DataBuffer.TYPE_BYTE);
        }
    }
    protected static SampleModel fixSampleModel(CachableRed src) {
        SampleModel sm = src.getSampleModel();
        int width  = sm.getWidth();
        int height = sm.getHeight();
        ColorModel  cm = src.getColorModel();
        if (cm != null) {
            if (cm.hasAlpha()) 
                return new PixelInterleavedSampleModel
                    (DataBuffer.TYPE_BYTE, width, height, 2, 2*width,
                     new int [] { 0, 1 });
            return new PixelInterleavedSampleModel
                (DataBuffer.TYPE_BYTE, width, height, 1, width,
                 new int [] { 0 });
        }
        else {
            if (sm.getNumBands() == 2)
                return new PixelInterleavedSampleModel
                    (DataBuffer.TYPE_BYTE, width, height, 2, 2*width,
                     new int [] { 0, 1 });
            return new PixelInterleavedSampleModel
                (DataBuffer.TYPE_BYTE, width, height, 1, width,
                 new int [] { 0 });
        }
    }
}
