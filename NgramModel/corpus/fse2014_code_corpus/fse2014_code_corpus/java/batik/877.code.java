package org.apache.batik.ext.awt.image.rendered;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.RasterOp;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import org.apache.batik.ext.awt.image.GraphicsUtil;
public class MorphologyOp implements BufferedImageOp, RasterOp {
    private int radiusX;
    private int radiusY;
    private boolean doDilation;
    private final int rangeX;
    private final int rangeY;
    private final ColorSpace sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);
    private final ColorSpace lRGB = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
    public MorphologyOp (int radiusX, int radiusY, boolean doDilation){
        if (radiusX<=0 || radiusY<=0){
            throw new IllegalArgumentException( "The radius of X-axis or Y-axis should not be Zero or Negatives." );
        }
        else {
            this.radiusX = radiusX;
            this.radiusY = radiusY;
            this.doDilation = doDilation;
            rangeX = 2*radiusX + 1;
            rangeY = 2*radiusY + 1;
        }
    }
    public Rectangle2D getBounds2D(Raster src){
        checkCompatible(src.getSampleModel());
        return new Rectangle(src.getMinX(), src.getMinY(), src.getWidth(), src.getHeight());
    }
    public Rectangle2D getBounds2D(BufferedImage src){
        return new Rectangle(0, 0, src.getWidth(), src.getHeight());
    }
    public Point2D getPoint2D(Point2D srcPt, Point2D destPt){
        if(destPt==null)
            destPt = new Point2D.Float();
        destPt.setLocation(srcPt.getX(), srcPt.getY());
        return destPt;
    }
    private void checkCompatible(ColorModel colorModel,
                                 SampleModel sampleModel){
        ColorSpace cs = colorModel.getColorSpace();
        if((!cs .equals (sRGB)) && (!cs .equals( lRGB)))
            throw new IllegalArgumentException("Expected CS_sRGB or CS_LINEAR_RGB color model");
        if(!(colorModel instanceof DirectColorModel))
            throw new IllegalArgumentException("colorModel should be an instance of DirectColorModel");
        if(sampleModel.getDataType() != DataBuffer.TYPE_INT)
            throw new IllegalArgumentException("colorModel's transferType should be DataBuffer.TYPE_INT");
        DirectColorModel dcm = (DirectColorModel)colorModel;
        if(dcm.getRedMask() != 0x00ff0000)
            throw new IllegalArgumentException("red mask in source should be 0x00ff0000");
        if(dcm.getGreenMask() != 0x0000ff00)
            throw new IllegalArgumentException("green mask in source should be 0x0000ff00");
        if(dcm.getBlueMask() != 0x000000ff)
            throw new IllegalArgumentException("blue mask in source should be 0x000000ff");
        if(dcm.getAlphaMask() != 0xff000000)
            throw new IllegalArgumentException("alpha mask in source should be 0xff000000");
    }
    private boolean isCompatible(ColorModel colorModel,
                                 SampleModel sampleModel){
        ColorSpace cs = colorModel.getColorSpace();
        if((cs != ColorSpace.getInstance(ColorSpace.CS_sRGB))
           &&
           (cs != ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB)))
            return false;
        if(!(colorModel instanceof DirectColorModel))
            return false;
        if(sampleModel.getDataType() != DataBuffer.TYPE_INT)
            return false;
        DirectColorModel dcm = (DirectColorModel)colorModel;
        if(dcm.getRedMask() != 0x00ff0000)
            return false;
        if(dcm.getGreenMask() != 0x0000ff00)
            return false;
        if(dcm.getBlueMask() != 0x000000ff)
            return false;
        if(dcm.getAlphaMask() != 0xff000000)
            return false;
        return true;
    }
    private void checkCompatible(SampleModel model){
        if(!(model instanceof SinglePixelPackedSampleModel))
            throw new IllegalArgumentException
                ("MorphologyOp only works with Rasters " +
                 "using SinglePixelPackedSampleModels");
        int nBands = model.getNumBands();
        if(nBands!=4)
            throw new IllegalArgumentException
                ("MorphologyOp only words with Rasters having 4 bands");
        if(model.getDataType()!=DataBuffer.TYPE_INT)
            throw new IllegalArgumentException
                ("MorphologyOp only works with Rasters using DataBufferInt");
        int[] bitOffsets=((SinglePixelPackedSampleModel)model).getBitOffsets();
        for(int i=0; i<bitOffsets.length; i++){
            if(bitOffsets[i]%8 != 0)
                throw new IllegalArgumentException
                    ("MorphologyOp only works with Rasters using 8 bits " +
                     "per band : " + i + " : " + bitOffsets[i]);
        }
    }
    public RenderingHints getRenderingHints(){
        return null;
    }
    public WritableRaster createCompatibleDestRaster(Raster src){
        checkCompatible(src.getSampleModel());
        return src.createCompatibleWritableRaster();
    }
    public BufferedImage createCompatibleDestImage(BufferedImage src,
                                                   ColorModel destCM){
        BufferedImage dest = null;
        if(destCM==null)
            destCM = src.getColorModel();
        WritableRaster wr;
        wr = destCM.createCompatibleWritableRaster(src.getWidth(),
                                                   src.getHeight());
        checkCompatible(destCM, wr.getSampleModel());
        dest = new BufferedImage(destCM, wr,
                                 destCM.isAlphaPremultiplied(), null);
        return dest;
    }
    static final boolean isBetter (final int v1, final int v2, final boolean doDilation) {
        if (v1 > v2)
            return doDilation;
        if (v1 < v2)
            return !doDilation;
        return true;
    }
    private void specialProcessRow(Raster src, WritableRaster dest){
        final int w = src.getWidth();
        final int h = src.getHeight();
        DataBufferInt srcDB = (DataBufferInt)src.getDataBuffer();
        DataBufferInt dstDB = (DataBufferInt)dest.getDataBuffer();
        SinglePixelPackedSampleModel sppsm;
        sppsm = (SinglePixelPackedSampleModel)src.getSampleModel();
        final int srcOff = srcDB.getOffset() +
            sppsm.getOffset(src.getMinX() - src.getSampleModelTranslateX(),
                            src.getMinY() - src.getSampleModelTranslateY());
        sppsm = (SinglePixelPackedSampleModel)dest.getSampleModel();
        final int dstOff = dstDB.getOffset() +
            sppsm.getOffset(dest.getMinX() - dest.getSampleModelTranslateX(),
                            dest.getMinY() - dest.getSampleModelTranslateY());
        final int srcScanStride = ((SinglePixelPackedSampleModel)src.getSampleModel()).getScanlineStride();
        final int dstScanStride = ((SinglePixelPackedSampleModel)dest.getSampleModel()).getScanlineStride();
        final int[] srcPixels = srcDB.getBankData()[0];
        final int[] destPixels = dstDB.getBankData()[0];
        int sp, dp;
        int bufferHead;
        int maxIndexA;
        int maxIndexR;
        int maxIndexG;
        int maxIndexB;
        int pel, currentPixel, lastPixel;
        int a,r,g,b;
        int a1,r1,g1,b1;
        if (w<=radiusX){
            for (int i=0; i<h; i++){
                sp = srcOff + i*srcScanStride;
                dp = dstOff + i*dstScanStride;
                pel = srcPixels[sp++];
                a = pel>>>24;
                r = pel&0xff0000;
                g = pel&0xff00;
                b = pel&0xff;
                for (int k=1; k<w; k++){
                    currentPixel = srcPixels[sp++];
                    a1 = currentPixel>>>24;
                    r1 = currentPixel&0xff0000;
                    g1 = currentPixel&0xff00;
                    b1 = currentPixel&0xff;
                    if (isBetter(a1, a, doDilation)){
                        a = a1;
                    }
                    if (isBetter(r1, r, doDilation)){
                        r = r1;
                    }
                    if (isBetter(g1, g, doDilation)){
                        g = g1;
                    }
                    if (isBetter(b1, b, doDilation)){
                        b = b1;
                    }
                }
                for (int k=0; k<w; k++){
                    destPixels[dp++] = (a << 24) | r | g | b;
                }
            }
        }
        else {
            final int [] bufferA = new int [w];
            final int [] bufferR = new int [w];
            final int [] bufferG = new int [w];
            final int [] bufferB = new int [w];
            for (int i=0; i<h; i++){
                sp = srcOff + i*srcScanStride;
                dp = dstOff + i*dstScanStride;
                bufferHead = 0;
                maxIndexA = 0;
                maxIndexR = 0;
                maxIndexG = 0;
                maxIndexB = 0;
                pel = srcPixels[sp++];
                a = pel>>>24;
                r = pel&0xff0000;
                g = pel&0xff00;
                b = pel&0xff;
                bufferA[0] = a;
                bufferR[0] = r;
                bufferG[0] = g;
                bufferB[0] = b;
                for (int k=1; k<=radiusX; k++){
                    currentPixel = srcPixels[sp++];
                    a1 = currentPixel>>>24;
                    r1 = currentPixel&0xff0000;
                    g1 = currentPixel&0xff00;
                    b1 = currentPixel&0xff;
                    bufferA[k] = a1;
                    bufferR[k] = r1;
                    bufferG[k] = g1;
                    bufferB[k] = b1;
                    if (isBetter(a1, a, doDilation)){
                        a = a1;
                        maxIndexA = k;
                    }
                    if (isBetter(r1, r, doDilation)){
                        r = r1;
                        maxIndexR = k;
                    }
                    if (isBetter(g1, g, doDilation)){
                        g = g1;
                        maxIndexG = k;
                    }
                    if (isBetter(b1, b, doDilation)){
                        b = b1;
                        maxIndexB = k;
                    }
                }
                destPixels[dp++] = (a << 24) | r | g | b;
                for (int j=1; j<=w-radiusX-1; j++){
                    lastPixel = srcPixels[sp++];
                    a = bufferA[maxIndexA];
                    a1 = lastPixel>>>24;
                    bufferA[j+radiusX] = a1;
                    if (isBetter(a1, a, doDilation)){
                        a = a1;
                        maxIndexA = j+radiusX;
                    }
                    r = bufferR[maxIndexR];
                    r1 = lastPixel&0xff0000;
                    bufferR[j+radiusX] = r1;
                    if (isBetter(r1, r, doDilation)){
                        r = r1;
                        maxIndexR = j+radiusX;
                    }
                    g = bufferG[maxIndexG];
                    g1 = lastPixel&0xff00;
                    bufferG[j+radiusX] = g1;
                    if (isBetter(g1, g, doDilation)){
                        g = g1;
                        maxIndexG = j+radiusX;
                    }
                    b = bufferB[maxIndexB];
                    b1 = lastPixel&0xff;
                    bufferB[j+radiusX] = b1;
                    if (isBetter(b1, b, doDilation)){
                        b = b1;
                        maxIndexB = j+radiusX;
                    }
                    destPixels[dp++] = (a << 24) | r | g | b;
                }
                for (int j = w-radiusX; j<= radiusX; j++){
                    destPixels[dp] = destPixels[dp-1];
                    dp++;
                }
                for (int j = radiusX+1; j<w; j++){
                    if (maxIndexA == bufferHead){
                        a = bufferA[bufferHead+1];
                        maxIndexA = bufferHead+1;
                        for (int m= bufferHead+2; m< w; m++){
                            a1 = bufferA[m];
                            if (isBetter(a1, a, doDilation)){
                                a = a1;
                                maxIndexA = m;
                            }
                        }
                    }
                    else {
                        a = bufferA[maxIndexA];
                    }
                    if (maxIndexR == bufferHead){
                        r = bufferR[bufferHead+1];
                        maxIndexR = bufferHead+1;
                        for (int m= bufferHead+2; m< w; m++){
                            r1 = bufferR[m];
                            if (isBetter(r1, r, doDilation)){
                                r = r1;
                                maxIndexR = m;
                            }
                        }
                    }
                    else {
                        r = bufferR[maxIndexR];
                    }
                    if (maxIndexG == bufferHead){
                        g = bufferG[bufferHead+1];
                        maxIndexG = bufferHead+1;
                        for (int m= bufferHead+2; m< w; m++){
                            g1 = bufferG[m];
                            if (isBetter(g1, g, doDilation)){
                                g = g1;
                                maxIndexG = m;
                            }
                        }
                    }
                    else {
                        g = bufferG[maxIndexG];
                    }
                    if (maxIndexB == bufferHead){
                        b = bufferB[bufferHead+1];
                        maxIndexB = bufferHead+1;
                        for (int m= bufferHead+2; m< w; m++){
                            b1 = bufferB[m];
                            if (isBetter(b1, b, doDilation)){
                                b = b1;
                                maxIndexB = m;
                            }
                        }
                    }
                    else {
                        b = bufferB[maxIndexB];
                    }
                    bufferHead++;
                    destPixels[dp++] = (a << 24) | r | g | b;
                }
            }
        }
    }
    private void specialProcessColumn(Raster src, WritableRaster dest){
        final int w = src.getWidth();
        final int h = src.getHeight();
        DataBufferInt dstDB = (DataBufferInt)dest.getDataBuffer();
        final int dstOff = dstDB.getOffset();
        final int dstScanStride = ((SinglePixelPackedSampleModel)dest.getSampleModel()).getScanlineStride();
        final int[] destPixels = dstDB.getBankData()[0];
        int dp, cp;
        int bufferHead;
        int maxIndexA;
        int maxIndexR;
        int maxIndexG;
        int maxIndexB;
        int pel, currentPixel, lastPixel;
        int a,r,g,b;
        int a1,r1,g1,b1;
        if (h<=radiusY){
            for (int j=0; j<w; j++){
                dp = dstOff + j;
                cp = dstOff + j;
                pel = destPixels[cp];
                cp += dstScanStride;
                a = pel>>>24;
                r = pel&0xff0000;
                g = pel&0xff00;
                b = pel&0xff;
                for (int k=1; k<h; k++){
                    currentPixel = destPixels[cp];
                    cp += dstScanStride;
                    a1 = currentPixel>>>24;
                    r1 = currentPixel&0xff0000;
                    g1 = currentPixel&0xff00;
                    b1 = currentPixel&0xff;
                    if (isBetter(a1, a, doDilation)){
                        a = a1;
                    }
                    if (isBetter(r1, r, doDilation)){
                        r = r1;
                    }
                    if (isBetter(g1, g, doDilation)){
                        g = g1;
                    }
                    if (isBetter(b1, b, doDilation)){
                        b = b1;
                    }
                }
                for (int k=0; k<h; k++){
                    destPixels[dp] = (a << 24) | r | g | b;
                    dp += dstScanStride;
                }
            }
        }
        else {
            final int [] bufferA = new int [h];
            final int [] bufferR = new int [h];
            final int [] bufferG = new int [h];
            final int [] bufferB = new int [h];
            for (int j=0; j<w; j++){
                dp = dstOff + j;
                cp = dstOff + j;
                bufferHead = 0;
                maxIndexA = 0;
                maxIndexR = 0;
                maxIndexG = 0;
                maxIndexB = 0;
                pel = destPixels[cp];
                cp += dstScanStride;
                a = pel>>>24;
                r = pel&0xff0000;
                g = pel&0xff00;
                b = pel&0xff;
                bufferA[0] = a;
                bufferR[0] = r;
                bufferG[0] = g;
                bufferB[0] = b;
                for (int k=1; k<=radiusY; k++){
                    currentPixel = destPixels[cp];
                    cp += dstScanStride;
                    a1 = currentPixel>>>24;
                    r1 = currentPixel&0xff0000;
                    g1 = currentPixel&0xff00;
                    b1 = currentPixel&0xff;
                    bufferA[k] = a1;
                    bufferR[k] = r1;
                    bufferG[k] = g1;
                    bufferB[k] = b1;
                    if (isBetter(a1, a, doDilation)){
                        a = a1;
                        maxIndexA = k;
                    }
                    if (isBetter(r1, r, doDilation)){
                        r = r1;
                        maxIndexR = k;
                    }
                    if (isBetter(g1, g, doDilation)){
                        g = g1;
                        maxIndexG = k;
                    }
                    if (isBetter(b1, b, doDilation)){
                        b = b1;
                        maxIndexB = k;
                    }
                }
                destPixels[dp] = (a << 24) | r | g | b;
                dp += dstScanStride;
                for (int i=1; i<=h-radiusY-1; i++){
                    lastPixel = destPixels[cp];
                    cp += dstScanStride;
                    a = bufferA[maxIndexA];
                    a1 = lastPixel>>>24;
                    bufferA[i+radiusY] = a1;
                    if (isBetter(a1, a, doDilation)){
                        a = a1;
                        maxIndexA = i+radiusY;
                    }
                    r = bufferR[maxIndexR];
                    r1 = lastPixel&0xff0000;
                    bufferR[i+radiusY] = r1;
                    if (isBetter(r1, r, doDilation)){
                        r = r1;
                        maxIndexR = i+radiusY;
                    }
                    g = bufferG[maxIndexG];
                    g1 = lastPixel&0xff00;
                    bufferG[i+radiusY] = g1;
                    if (isBetter(g1, g, doDilation)){
                        g = g1;
                        maxIndexG = i+radiusY;
                    }
                    b = bufferB[maxIndexB];
                    b1 = lastPixel&0xff;
                    bufferB[i+radiusY] = b1;
                    if (isBetter(b1, b, doDilation)){
                        b = b1;
                        maxIndexB = i+radiusY;
                    }
                    destPixels[dp] = (a << 24) | r | g | b;
                    dp += dstScanStride;
                }
                for (int i = h-radiusY; i<= radiusY; i++){
                    destPixels[dp] = destPixels[dp-dstScanStride];
                    dp += dstScanStride;
                }
                for (int i = radiusY+1; i<h; i++){
                    if (maxIndexA == bufferHead){
                        a = bufferA[bufferHead+1];
                        maxIndexA = bufferHead+1;
                        for (int m= bufferHead+2; m< h; m++){
                            a1 = bufferA[m];
                            if (isBetter(a1, a, doDilation)){
                                a = a1;
                                maxIndexA = m;
                            }
                        }
                    }
                    else {
                        a = bufferA[maxIndexA];
                    }
                    if (maxIndexR == bufferHead){
                        r = bufferR[bufferHead+1];
                        maxIndexR = bufferHead+1;
                        for (int m= bufferHead+2; m< h; m++){
                            r1 = bufferR[m];
                            if (isBetter(r1, r, doDilation)){
                                r = r1;
                                maxIndexR = m;
                            }
                        }
                    }
                    else {
                        r = bufferR[maxIndexR];
                    }
                    if (maxIndexG == bufferHead){
                        g = bufferG[bufferHead+1];
                        maxIndexG = bufferHead+1;
                        for (int m= bufferHead+2; m< h; m++){
                            g1 = bufferG[m];
                            if (isBetter(g1, g, doDilation)){
                                g = g1;
                                maxIndexG = m;
                            }
                        }
                    }
                    else {
                        g = bufferG[maxIndexG];
                    }
                    if (maxIndexB == bufferHead){
                        b = bufferB[bufferHead+1];
                        maxIndexB = bufferHead+1;
                        for (int m= bufferHead+2; m< h; m++){
                            b1 = bufferB[m];
                            if (isBetter(b1, b, doDilation)){
                                b = b1;
                                maxIndexB = m;
                            }
                        }
                    }
                    else {
                        b = bufferB[maxIndexB];
                    }
                    bufferHead++;
                    destPixels[dp] = (a << 24) | r | g | b;
                    dp += dstScanStride;
                }
            }
        } 
    }
    public WritableRaster filter(Raster src, WritableRaster dest){
        if(dest!=null) checkCompatible(dest.getSampleModel());
        else {
            if(src==null)
                throw new IllegalArgumentException("src should not be null when dest is null");
            else dest = createCompatibleDestRaster(src);
        }
        final int w = src.getWidth();
        final int h = src.getHeight();
        DataBufferInt srcDB = (DataBufferInt)src.getDataBuffer();
        DataBufferInt dstDB = (DataBufferInt)dest.getDataBuffer();
        final int srcOff = srcDB.getOffset();
        final int dstOff = dstDB.getOffset();
        final int srcScanStride = ((SinglePixelPackedSampleModel)src.getSampleModel()).getScanlineStride();
        final int dstScanStride = ((SinglePixelPackedSampleModel)dest.getSampleModel()).getScanlineStride();
        final int[] srcPixels = srcDB.getBankData()[0];
        final int[] destPixels = dstDB.getBankData()[0];
        int sp, dp, cp;
        int bufferHead;
        int maxIndexA;
        int maxIndexR;
        int maxIndexG;
        int maxIndexB;
        int pel, currentPixel, lastPixel;
        int a,r,g,b;
        int a1,r1,g1,b1;
        if (w<=2*radiusX){
            specialProcessRow(src, dest);
        }
        else {
            final int [] bufferA = new int [rangeX];
            final int [] bufferR = new int [rangeX];
            final int [] bufferG = new int [rangeX];
            final int [] bufferB = new int [rangeX];
            for (int i=0; i<h; i++){
                sp = srcOff + i*srcScanStride;
                dp = dstOff + i*dstScanStride;
                bufferHead = 0;
                maxIndexA = 0;
                maxIndexR = 0;
                maxIndexG = 0;
                maxIndexB = 0;
                pel = srcPixels[sp++];
                a = pel>>>24;
                r = pel&0xff0000;
                g = pel&0xff00;
                b = pel&0xff;
                bufferA[0] = a;
                bufferR[0] = r;
                bufferG[0] = g;
                bufferB[0] = b;
                for (int k=1; k<=radiusX; k++){
                    currentPixel = srcPixels[sp++];
                    a1 = currentPixel>>>24;
                    r1 = currentPixel&0xff0000;
                    g1 = currentPixel&0xff00;
                    b1 = currentPixel&0xff;
                    bufferA[k] = a1;
                    bufferR[k] = r1;
                    bufferG[k] = g1;
                    bufferB[k] = b1;
                    if (isBetter(a1, a, doDilation)){
                        a = a1;
                        maxIndexA = k;
                    }
                    if (isBetter(r1, r, doDilation)){
                        r = r1;
                        maxIndexR = k;
                    }
                    if (isBetter(g1, g, doDilation)){
                        g = g1;
                        maxIndexG = k;
                    }
                    if (isBetter(b1, b, doDilation)){
                        b = b1;
                        maxIndexB = k;
                    }
                }
                destPixels[dp++] = (a << 24) | r | g | b;
                for (int j=1; j<=radiusX; j++){
                    lastPixel = srcPixels[sp++];
                    a = bufferA[maxIndexA];
                    a1 = lastPixel>>>24;
                    bufferA[j+radiusX] = a1;
                    if (isBetter(a1, a, doDilation)){
                        a = a1;
                        maxIndexA = j+radiusX;
                    }
                    r = bufferR[maxIndexR];
                    r1 = lastPixel&0xff0000;
                    bufferR[j+radiusX] = r1;
                    if (isBetter(r1, r, doDilation)){
                        r = r1;
                        maxIndexR = j+radiusX;
                    }
                    g = bufferG[maxIndexG];
                    g1 = lastPixel&0xff00;
                    bufferG[j+radiusX] = g1;
                    if (isBetter(g1, g, doDilation)){
                        g = g1;
                        maxIndexG = j+radiusX;
                    }
                    b = bufferB[maxIndexB];
                    b1 = lastPixel&0xff;
                    bufferB[j+radiusX] = b1;
                    if (isBetter(b1, b, doDilation)){
                        b = b1;
                        maxIndexB = j+radiusX;
                    }
                    destPixels[dp++] = (a << 24) | r | g | b;
                }
                for (int j=radiusX+1; j<=w-1-radiusX; j++){
                    lastPixel = srcPixels[sp++];
                    a1 = lastPixel>>>24;
                    r1 = lastPixel&0xff0000;
                    g1 = lastPixel&0xff00;
                    b1 = lastPixel&0xff;
                    bufferA[bufferHead] = a1;
                    bufferR[bufferHead] = r1;
                    bufferG[bufferHead] = g1;
                    bufferB[bufferHead] = b1;
                    if (maxIndexA == bufferHead){
                        a = bufferA[0];
                        maxIndexA = 0;
                        for (int m= 1; m< rangeX; m++){
                            a1 = bufferA[m];
                            if (isBetter(a1, a, doDilation)){
                                a = a1;
                                maxIndexA = m;
                            }
                        }
                    }
                    else {
                        a = bufferA[maxIndexA];
                        if (isBetter(a1, a, doDilation)){
                            a = a1;
                            maxIndexA = bufferHead;
                        }
                    }
                    if (maxIndexR == bufferHead){
                        r = bufferR[0];
                        maxIndexR = 0;
                        for (int m= 1; m< rangeX; m++){
                            r1 = bufferR[m];
                            if (isBetter(r1, r, doDilation)){
                                r = r1;
                                maxIndexR = m;
                            }
                        }
                    }
                    else {
                        r = bufferR[maxIndexR];
                        if (isBetter(r1, r, doDilation)){
                            r = r1;
                            maxIndexR = bufferHead;
                        }
                    }
                    if (maxIndexG == bufferHead){
                        g = bufferG[0];
                        maxIndexG = 0;
                        for (int m= 1; m< rangeX; m++){
                            g1 = bufferG[m];
                            if (isBetter(g1, g, doDilation)){
                                g = g1;
                                maxIndexG = m;
                            }
                        }
                    }
                    else {
                        g = bufferG[maxIndexG];
                        if (isBetter(g1, g, doDilation)){
                            g = g1;
                            maxIndexG = bufferHead;
                        }
                    }
                    if (maxIndexB == bufferHead){
                        b = bufferB[0];
                        maxIndexB = 0;
                        for (int m= 1; m< rangeX; m++){
                            b1 = bufferB[m];
                            if (isBetter(b1, b, doDilation)){
                                b = b1;
                                maxIndexB = m;
                            }
                        }
                    }
                    else {
                        b = bufferB[maxIndexB];
                        if (isBetter(b1, b, doDilation)){
                            b = b1;
                            maxIndexB = bufferHead;
                        }
                    }
                    destPixels[dp++] = (a << 24) | r | g | b;
                    bufferHead = (bufferHead+1)%rangeX;
                }
                int head;
                final int tail = (bufferHead == 0)?rangeX-1:bufferHead -1;
                int count = rangeX-1;
                for (int j=w-radiusX; j<w; j++){
                    head = (bufferHead+1)%rangeX;
                    if (maxIndexA == bufferHead){
                        a = bufferA[tail];
                        int hd = head;
                        for(int m=1; m<count; m++) {
                            a1 = bufferA[hd];
                            if (isBetter(a1, a, doDilation)){
                                a = a1;
                                maxIndexA = hd;
                            }
                            hd = (hd+1)%rangeX;
                        }
                    }
                    if (maxIndexR == bufferHead){
                        r = bufferR[tail];
                        int hd = head;
                        for(int m=1; m<count; m++) {
                            r1 = bufferR[hd];
                            if (isBetter(r1, r, doDilation)){
                                r = r1;
                                maxIndexR = hd;
                            }
                            hd = (hd+1)%rangeX;
                        }
                    }
                    if (maxIndexG == bufferHead){
                        g = bufferG[tail];
                        int hd = head;
                        for(int m=1; m<count; m++) {
                            g1 = bufferG[hd];
                            if (isBetter(g1, g, doDilation)){
                                g = g1;
                                maxIndexG = hd;
                            }
                            hd = (hd+1)%rangeX;
                        }
                    }
                    if (maxIndexB == bufferHead){
                        b = bufferB[tail];
                        int hd = head;
                        for(int m=1; m<count; m++) {
                            b1 = bufferB[hd];
                            if (isBetter(b1, b, doDilation)){
                                b = b1;
                                maxIndexB = hd;
                            }
                            hd = (hd+1)%rangeX;
                        }
                    }
                    destPixels[dp++] = (a << 24) | r | g | b;
                    bufferHead = (bufferHead+1)%rangeX;
                    count--;
                }
            }
        }
        if (h<=2*radiusY){
            specialProcessColumn(src, dest);
        }
        else {
            final int [] bufferA = new int [rangeY];
            final int [] bufferR = new int [rangeY];
            final int [] bufferG = new int [rangeY];
            final int [] bufferB = new int [rangeY];
            for (int j=0; j<w; j++){
                dp = dstOff + j;
                cp = dstOff + j;
                bufferHead = 0;
                maxIndexA = 0;
                maxIndexR = 0;
                maxIndexG = 0;
                maxIndexB = 0;
                pel = destPixels[cp];
                cp += dstScanStride;
                a = pel>>>24;
                r = pel&0xff0000;
                g = pel&0xff00;
                b = pel&0xff;
                bufferA[0] = a;
                bufferR[0] = r;
                bufferG[0] = g;
                bufferB[0] = b;
                for (int k=1; k<=radiusY; k++){
                    currentPixel = destPixels[cp];
                    cp += dstScanStride;
                    a1 = currentPixel>>>24;
                    r1 = currentPixel&0xff0000;
                    g1 = currentPixel&0xff00;
                    b1 = currentPixel&0xff;
                    bufferA[k] = a1;
                    bufferR[k] = r1;
                    bufferG[k] = g1;
                    bufferB[k] = b1;
                    if (isBetter(a1, a, doDilation)){
                        a = a1;
                        maxIndexA = k;
                    }
                    if (isBetter(r1, r, doDilation)){
                        r = r1;
                        maxIndexR = k;
                    }
                    if (isBetter(g1, g, doDilation)){
                        g = g1;
                        maxIndexG = k;
                    }
                    if (isBetter(b1, b, doDilation)){
                        b = b1;
                        maxIndexB = k;
                    }
                }
                destPixels[dp] = (a << 24) | r | g | b;
                dp += dstScanStride;
                for (int i=1; i<=radiusY; i++){
                    int maxI = i+radiusY;
                    lastPixel = destPixels[cp];
                    cp += dstScanStride;
                    a = bufferA[maxIndexA];
                    a1 = lastPixel>>>24;
                    bufferA[maxI] = a1;
                    if (isBetter(a1, a, doDilation)){
                        a = a1;
                        maxIndexA = maxI;
                    }
                    r = bufferR[maxIndexR];
                    r1 = lastPixel&0xff0000;
                    bufferR[maxI] = r1;
                    if (isBetter(r1, r, doDilation)){
                        r = r1;
                        maxIndexR = maxI;
                    }
                    g = bufferG[maxIndexG];
                    g1 = lastPixel&0xff00;
                    bufferG[maxI] = g1;
                    if (isBetter(g1, g, doDilation)){
                        g = g1;
                        maxIndexG = maxI;
                    }
                    b = bufferB[maxIndexB];
                    b1 = lastPixel&0xff;
                    bufferB[maxI] = b1;
                    if (isBetter(b1, b, doDilation)){
                        b = b1;
                        maxIndexB = maxI;
                    }
                    destPixels[dp] = (a << 24) | r | g | b;
                    dp += dstScanStride;
                }
                for (int i=radiusY+1; i<=h-1-radiusY; i++){
                    lastPixel = destPixels[cp];
                    cp += dstScanStride;
                    a1 = lastPixel>>>24;
                    r1 = lastPixel&0xff0000;
                    g1 = lastPixel&0xff00;
                    b1 = lastPixel&0xff;
                    bufferA[bufferHead] = a1;
                    bufferR[bufferHead] = r1;
                    bufferG[bufferHead] = g1;
                    bufferB[bufferHead] = b1;
                    if (maxIndexA == bufferHead){
                        a = bufferA[0];
                        maxIndexA = 0;
                        for (int m= 1; m<= 2*radiusY; m++){
                            a1 = bufferA[m];
                            if (isBetter(a1, a, doDilation)){
                                a = a1;
                                maxIndexA = m;
                            }
                        }
                    }
                    else {
                        a = bufferA[maxIndexA];
                        if (isBetter(a1, a, doDilation)){
                            a = a1;
                            maxIndexA = bufferHead;
                        }
                    }
                    if (maxIndexR == bufferHead){
                        r = bufferR[0];
                        maxIndexR = 0;
                        for (int m= 1; m<= 2*radiusY; m++){
                            r1 = bufferR[m];
                            if (isBetter(r1, r, doDilation)){
                                r = r1;
                                maxIndexR = m;
                            }
                        }
                    }
                    else {
                        r = bufferR[maxIndexR];
                        if (isBetter(r1, r, doDilation)){
                            r = r1;
                            maxIndexR = bufferHead;
                        }
                    }
                    if (maxIndexG == bufferHead){
                        g = bufferG[0];
                        maxIndexG = 0;
                        for (int m= 1; m<= 2*radiusY; m++){
                            g1 = bufferG[m];
                            if (isBetter(g1, g, doDilation)){
                                g = g1;
                                maxIndexG = m;
                            }
                        }
                    }
                    else {
                        g = bufferG[maxIndexG];
                        if (isBetter(g1, g, doDilation)){
                            g = g1;
                            maxIndexG = bufferHead;
                        }
                    }
                    if (maxIndexB == bufferHead){
                        b = bufferB[0];
                        maxIndexB = 0;
                        for (int m= 1; m<= 2*radiusY; m++){
                            b1 = bufferB[m];
                            if (isBetter(b1, b, doDilation)){
                                b = b1;
                                maxIndexB = m;
                            }
                        }
                    }
                    else {
                        b = bufferB[maxIndexB];
                        if (isBetter(b1, b, doDilation)){
                            b = b1;
                            maxIndexB = bufferHead;
                        }
                    }
                    destPixels[dp] = (a << 24) | r | g | b;
                    dp += dstScanStride;
                    bufferHead = (bufferHead+1)%rangeY;
                }
                int head;
                final int tail = (bufferHead == 0)?2*radiusY:bufferHead -1;
                int count = rangeY-1;
                for (int i= h-radiusY; i<h-1; i++){
                    head = (bufferHead +1)%rangeY;
                    if (maxIndexA == bufferHead){
                        a = bufferA[tail];
                        int hd = head;
                        for (int m=1; m<count; m++){
                            a1 = bufferA[hd];
                            if (isBetter(a1, a, doDilation)){
                                a = a1;
                                maxIndexA = hd;
                            }
                            hd = (hd+1)%rangeY;
                        }
                    }
                    if (maxIndexR == bufferHead){
                        r = bufferR[tail];
                        int hd = head;
                        for (int m=1; m<count; m++){
                            r1 = bufferR[hd];
                            if (isBetter(r1, r, doDilation)){
                                r = r1;
                                maxIndexR = hd;
                            }
                            hd = (hd+1)%rangeY;
                        }
                    }
                    if (maxIndexG == bufferHead){
                        g = bufferG[tail];
                        int hd = head;
                        for (int m=1; m<count; m++){
                            g1 = bufferG[hd];
                            if (isBetter(g1, g, doDilation)){
                                g = g1;
                                maxIndexG = hd;
                            }
                            hd = (hd+1)%rangeY;
                        }
                    }
                    if (maxIndexB == bufferHead){
                        b = bufferB[tail];
                        int hd = head;
                        for (int m=1; m<count; m++){
                            b1 = bufferB[hd];
                            if (isBetter(b1, b, doDilation)){
                                b = b1;
                                maxIndexB = hd;
                            }
                            hd = (hd+1)%rangeY;
                        }
                    }
                    destPixels[dp] = (a << 24) | r | g | b;
                    dp += dstScanStride;
                    bufferHead = (bufferHead+1)%rangeY;
                    count--;
                }
            }
        }
        return dest;
    }
    public BufferedImage filter(BufferedImage src, BufferedImage dest){
        if (src == null)
            throw new NullPointerException("Source image should not be null");
        BufferedImage origSrc   = src;
        BufferedImage finalDest = dest;
        if (!isCompatible(src.getColorModel(), src.getSampleModel())) {
            src = new BufferedImage(src.getWidth(), src.getHeight(),
                                    BufferedImage.TYPE_INT_ARGB_PRE);
            GraphicsUtil.copyData(origSrc, src);
        }
        else if (!src.isAlphaPremultiplied()) {
            ColorModel    srcCM, srcCMPre;
            srcCM    = src.getColorModel();
            srcCMPre = GraphicsUtil.coerceColorModel(srcCM, true);
            src = new BufferedImage(srcCMPre, src.getRaster(),
                                    true, null);
            GraphicsUtil.copyData(origSrc, src);
        }
        if (dest == null) {
            dest = createCompatibleDestImage(src, null);
            finalDest = dest;
        } else if (!isCompatible(dest.getColorModel(),
                                 dest.getSampleModel())) {
            dest = createCompatibleDestImage(src, null);
        } else if (!dest.isAlphaPremultiplied()) {
            ColorModel    dstCM, dstCMPre;
            dstCM    = dest.getColorModel();
            dstCMPre = GraphicsUtil.coerceColorModel(dstCM, true);
            dest = new BufferedImage(dstCMPre, finalDest.getRaster(),
                                     true, null);
        }
        filter(src.getRaster(), dest.getRaster());
        if ((src.getRaster() == origSrc.getRaster()) &&
            (src.isAlphaPremultiplied() != origSrc.isAlphaPremultiplied())) {
            GraphicsUtil.copyData(src, origSrc);
        }
        if ((dest.getRaster() != finalDest.getRaster()) ||
            (dest.isAlphaPremultiplied() != finalDest.isAlphaPremultiplied())){
            GraphicsUtil.copyData(dest, finalDest);
        }
        return finalDest;
    }
}
