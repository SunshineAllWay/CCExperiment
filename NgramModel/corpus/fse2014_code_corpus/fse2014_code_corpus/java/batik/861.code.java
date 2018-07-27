package org.apache.batik.ext.awt.image.rendered;
import java.awt.color.ColorSpace;
import java.awt.image.BandCombineOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import org.apache.batik.ext.awt.image.GraphicsUtil;
public class Any2sRGBRed extends AbstractRed {
    boolean srcIsLsRGB = false;
    public Any2sRGBRed(CachableRed src) {
        super(src,src.getBounds(),
              fixColorModel(src),
              fixSampleModel(src),
              src.getTileGridXOffset(),
              src.getTileGridYOffset(),
              null);
        ColorModel srcCM = src.getColorModel();
        if (srcCM == null) return;
        ColorSpace srcCS = srcCM.getColorSpace();
        if (srcCS == ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB))
            srcIsLsRGB = true;
    }
    public static boolean is_INT_PACK_COMP(SampleModel sm) {
        if(!(sm instanceof SinglePixelPackedSampleModel)) return false;
        if(sm.getDataType() != DataBuffer.TYPE_INT)       return false;
        SinglePixelPackedSampleModel sppsm;
        sppsm = (SinglePixelPackedSampleModel)sm;
        int [] masks = sppsm.getBitMasks();
        if ((masks.length != 3) && (masks.length != 4)) return false;
        if(masks[0] != 0x00ff0000) return false;
        if(masks[1] != 0x0000ff00) return false;
        if(masks[2] != 0x000000ff) return false;
        if ((masks.length == 4) &&
            (masks[3] != 0xff000000)) return false;
        return true;
   }
    private static final double GAMMA = 2.4;
    private static final int[] linearToSRGBLut = new int[256];
    static {
        final double scale = 1.0/255;
        final double exp   = 1.0/GAMMA;
        for(int i=0; i<256; i++){
            double value = i*scale;
            if(value <= 0.0031308)
                value *= 12.92;
            else
                value = 1.055 * Math.pow(value, exp) - 0.055;
            linearToSRGBLut[i] = (int)Math.round(value*255.0);
        }
    }
    public static WritableRaster applyLut_INT(WritableRaster wr,
                                              final int []lut) {
        SinglePixelPackedSampleModel sm =
            (SinglePixelPackedSampleModel)wr.getSampleModel();
        DataBufferInt db = (DataBufferInt)wr.getDataBuffer();
        final int     srcBase
            = (db.getOffset() +
               sm.getOffset(wr.getMinX()-wr.getSampleModelTranslateX(),
                            wr.getMinY()-wr.getSampleModelTranslateY()));
        final int[] pixels   = db.getBankData()[0];
        final int width      = wr.getWidth();
        final int height     = wr.getHeight();
        final int scanStride = sm.getScanlineStride();
        int end, pix;
        for (int y=0; y<height; y++) {
            int sp  = srcBase + y*scanStride;
            end = sp + width;
            while (sp<end) {
                pix = pixels[sp];
                pixels[sp] =
                    ((     pix      &0xFF000000)|
                     (lut[(pix>>>16)&0xFF]<<16) |
                     (lut[(pix>>> 8)&0xFF]<< 8) |
                     (lut[(pix     )&0xFF]    ));
                sp++;
            }
        }
        return wr;
    }
    public WritableRaster copyData(WritableRaster wr) {
        CachableRed src   = (CachableRed)getSources().get(0);
        ColorModel  srcCM = src.getColorModel();
        SampleModel srcSM = src.getSampleModel();
        if (srcIsLsRGB &&
            is_INT_PACK_COMP(wr.getSampleModel())) {
            src.copyData(wr);
            if (srcCM.hasAlpha())
                GraphicsUtil.coerceData(wr, srcCM, false);
            applyLut_INT(wr, linearToSRGBLut);
            return wr;
        }
        if (srcCM == null) {
            float [][] matrix = null;
            switch (srcSM.getNumBands()) {
            case 1:
                matrix = new float[3][1];
                matrix[0][0] = 1; 
                matrix[1][0] = 1; 
                matrix[2][0] = 1; 
                break;
            case 2:
                matrix = new float[4][2];
                matrix[0][0] = 1; 
                matrix[1][0] = 1; 
                matrix[2][0] = 1; 
                matrix[3][1] = 1; 
                break;
            case 3:
                matrix = new float[3][3];
                matrix[0][0] = 1; 
                matrix[1][1] = 1; 
                matrix[2][2] = 1; 
                break;
            default:
                matrix = new float[4][srcSM.getNumBands()];
                matrix[0][0] = 1; 
                matrix[1][1] = 1; 
                matrix[2][2] = 1; 
                matrix[3][3] = 1; 
                break;
            }
            Raster srcRas = src.getData(wr.getBounds());
            BandCombineOp op = new BandCombineOp(matrix, null);
            op.filter(srcRas, wr);
            return wr;
        }
        if (srcCM.getColorSpace() ==
            ColorSpace.getInstance(ColorSpace.CS_GRAY)) {
            try {
                float [][] matrix = null;
                switch (srcSM.getNumBands()) {
                case 1:
                    matrix = new float[3][1];
                    matrix[0][0] = 1; 
                    matrix[1][0] = 1; 
                    matrix[2][0] = 1; 
                    break;
                case 2:
                default:
                    matrix = new float[4][2];
                    matrix[0][0] = 1; 
                    matrix[1][0] = 1; 
                    matrix[2][0] = 1; 
                    matrix[3][1] = 1; 
                    break;
                }
                Raster srcRas = src.getData(wr.getBounds());
                BandCombineOp op = new BandCombineOp(matrix, null);
                op.filter(srcRas, wr);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return wr;
        }
        ColorModel dstCM = getColorModel();
        if (srcCM.getColorSpace() == dstCM.getColorSpace()) {
            if (is_INT_PACK_COMP(srcSM))
                src.copyData(wr);
            else
                GraphicsUtil.copyData(src.getData(wr.getBounds()), wr);
            return wr;
        }
        Raster srcRas = src.getData(wr.getBounds());
        WritableRaster srcWr  = (WritableRaster)srcRas;
        ColorModel srcBICM = srcCM;
        if (srcCM.hasAlpha())
            srcBICM = GraphicsUtil.coerceData(srcWr, srcCM, false);
        BufferedImage srcBI, dstBI;
        srcBI = new BufferedImage(srcBICM,
                                  srcWr.createWritableTranslatedChild(0,0),
                                  false,
                                  null);
        ColorConvertOp op = new ColorConvertOp(dstCM.getColorSpace(),
                                               null);
        dstBI = op.filter(srcBI, null);
        WritableRaster wr00 = wr.createWritableTranslatedChild(0,0);
        for (int i=0; i<dstCM.getColorSpace().getNumComponents(); i++)
            copyBand(dstBI.getRaster(), i, wr00,    i);
        if (dstCM.hasAlpha())
            copyBand(srcWr, srcSM.getNumBands()-1,
                     wr,    getSampleModel().getNumBands()-1);
        return wr;
    }
    protected static ColorModel fixColorModel(CachableRed src) {
        ColorModel  cm = src.getColorModel();
        if (cm != null) {
            if (cm.hasAlpha())
                return GraphicsUtil.sRGB_Unpre;
            return GraphicsUtil.sRGB;
        }
        else {
            SampleModel sm = src.getSampleModel();
            switch (sm.getNumBands()) {
            case 1:
                return GraphicsUtil.sRGB;
            case 2:
                return GraphicsUtil.sRGB_Unpre;
            case 3:
                return GraphicsUtil.sRGB;
            }
            return GraphicsUtil.sRGB_Unpre;
        }
    }
    protected static SampleModel fixSampleModel(CachableRed src) {
        SampleModel sm = src.getSampleModel();
        ColorModel  cm = src.getColorModel();
        boolean alpha = false;
        if (cm != null)
            alpha = cm.hasAlpha();
        else {
            switch (sm.getNumBands()) {
            case 1: case 3:
                alpha = false;
                break;
            default:
                alpha = true;
                break;
            }
        }
        if (alpha)
            return new SinglePixelPackedSampleModel
                (DataBuffer.TYPE_INT,
                 sm.getWidth(),
                 sm.getHeight(),
                 new int [] {0xFF0000, 0xFF00, 0xFF, 0xFF000000});
        else
            return new SinglePixelPackedSampleModel
                (DataBuffer.TYPE_INT,
                 sm.getWidth(),
                 sm.getHeight(),
                 new int [] {0xFF0000, 0xFF00, 0xFF});
    }
}
