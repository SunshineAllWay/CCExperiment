package org.apache.batik.ext.awt.image.rendered;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.image.BandCombineOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import org.apache.batik.ext.awt.image.GraphicsUtil;
public class Any2LsRGBRed extends AbstractRed {
    boolean srcIssRGB = false;
    public Any2LsRGBRed(CachableRed src) {
        super(src,src.getBounds(),
              fixColorModel(src),
              fixSampleModel(src),
              src.getTileGridXOffset(),
              src.getTileGridYOffset(),
              null);
        ColorModel srcCM = src.getColorModel();
        if (srcCM == null) return;
        ColorSpace srcCS = srcCM.getColorSpace();
        if (srcCS == ColorSpace.getInstance(ColorSpace.CS_sRGB))
            srcIssRGB = true;
    }
    private static final double GAMMA = 2.4;
    private static final double LFACT = 1.0/12.92;
    public static final double sRGBToLsRGB(double value) {
        if(value <= 0.003928)
            return value*LFACT;
        return Math.pow((value+0.055)/1.055, GAMMA);
    }
    private static final int[] sRGBToLsRGBLut = new int[256];
    static {
        final double scale = 1.0/255;
        for(int i=0; i<256; i++){
            double value = sRGBToLsRGB(i*scale);
            sRGBToLsRGBLut[i] = (int)Math.round(value*255.0);
        }
    }
    public WritableRaster copyData(WritableRaster wr) {
        CachableRed src   = (CachableRed)getSources().get(0);
        ColorModel  srcCM = src.getColorModel();
        SampleModel srcSM = src.getSampleModel();
        if (srcIssRGB &&
            Any2sRGBRed.is_INT_PACK_COMP(wr.getSampleModel())) {
            src.copyData(wr);
            if (srcCM.hasAlpha())
                GraphicsUtil.coerceData(wr, srcCM, false);
            Any2sRGBRed.applyLut_INT(wr, sRGBToLsRGBLut);
            return wr;
        }
        if (srcCM == null) {
            float [][] matrix = null;
            switch (srcSM.getNumBands()) {
            case 1:
                matrix = new float[1][3];
                matrix[0][0] = 1; 
                matrix[0][1] = 1; 
                matrix[0][2] = 1; 
                break;
            case 2:
                matrix = new float[2][4];
                matrix[0][0] = 1; 
                matrix[0][1] = 1; 
                matrix[0][2] = 1; 
                matrix[1][3] = 1; 
                break;
            case 3:
                matrix = new float[3][3];
                matrix[0][0] = 1; 
                matrix[1][1] = 1; 
                matrix[2][2] = 1; 
                break;
            default:
                matrix = new float[srcSM.getNumBands()][4];
                matrix[0][0] = 1; 
                matrix[1][1] = 1; 
                matrix[2][2] = 1; 
                matrix[3][3] = 1; 
                break;
            }
            Raster srcRas = src.getData(wr.getBounds());
            BandCombineOp op = new BandCombineOp(matrix, null);
            op.filter(srcRas, wr);
        } else {
            ColorModel dstCM = getColorModel();
            BufferedImage dstBI;
            if (!dstCM.hasAlpha()) {
                dstBI = new BufferedImage
                    (dstCM, wr.createWritableTranslatedChild(0,0),
                     dstCM.isAlphaPremultiplied(), null);
            } else {
                SinglePixelPackedSampleModel dstSM;
                dstSM = (SinglePixelPackedSampleModel)wr.getSampleModel();
                int [] masks = dstSM.getBitMasks();
                SampleModel dstSMNoA = new SinglePixelPackedSampleModel
                    (dstSM.getDataType(), dstSM.getWidth(), dstSM.getHeight(),
                     dstSM.getScanlineStride(),
                     new int[] {masks[0], masks[1], masks[2]});
                ColorModel dstCMNoA = GraphicsUtil.Linear_sRGB;
                WritableRaster dstWr;
                dstWr = Raster.createWritableRaster(dstSMNoA,
                                                    wr.getDataBuffer(),
                                                    new Point(0,0));
                dstWr = dstWr.createWritableChild
                    (wr.getMinX()-wr.getSampleModelTranslateX(),
                     wr.getMinY()-wr.getSampleModelTranslateY(),
                     wr.getWidth(), wr.getHeight(),
                     0, 0, null);
                dstBI = new BufferedImage(dstCMNoA, dstWr, false, null);
            }
            ColorModel srcBICM = srcCM;
            WritableRaster srcWr;
            if ( srcCM.hasAlpha() && srcCM.isAlphaPremultiplied() ) {
                Rectangle wrR = wr.getBounds();
                SampleModel sm = srcCM.createCompatibleSampleModel
                    (wrR.width, wrR.height);
                srcWr = Raster.createWritableRaster
                    (sm, new Point(wrR.x, wrR.y));
                src.copyData(srcWr);
                srcBICM = GraphicsUtil.coerceData(srcWr, srcCM, false);
            } else {
                Raster srcRas = src.getData(wr.getBounds());
                srcWr = GraphicsUtil.makeRasterWritable(srcRas);
            }
            BufferedImage srcBI;
            srcBI = new BufferedImage(srcBICM,
                                      srcWr.createWritableTranslatedChild(0,0),
                                      false,
                                      null);
            ColorConvertOp op = new ColorConvertOp(null);
            op.filter(srcBI, dstBI);
            if (dstCM.hasAlpha())
                copyBand(srcWr, srcSM.getNumBands()-1,
                         wr,    getSampleModel().getNumBands()-1);
        }
        return wr;
    }
    protected static ColorModel fixColorModel(CachableRed src) {
        ColorModel  cm = src.getColorModel();
        if (cm != null) {
            if (cm.hasAlpha())
                return GraphicsUtil.Linear_sRGB_Unpre;
            return GraphicsUtil.Linear_sRGB;
        }
        else {
            SampleModel sm = src.getSampleModel();
            switch (sm.getNumBands()) {
            case 1:
                return GraphicsUtil.Linear_sRGB;
            case 2:
                return GraphicsUtil.Linear_sRGB_Unpre;
            case 3:
                return GraphicsUtil.Linear_sRGB;
            }
            return GraphicsUtil.Linear_sRGB_Unpre;
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
