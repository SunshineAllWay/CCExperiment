package org.apache.batik.ext.awt.image.renderable;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Kernel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.RenderContext;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.rendered.AffineRed;
import org.apache.batik.ext.awt.image.rendered.BufferedImageCachableRed;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.PadRed;
public class ConvolveMatrixRable8Bit
    extends    AbstractColorInterpolationRable
    implements ConvolveMatrixRable {
    Kernel kernel;
    Point  target;
    float bias;
    boolean kernelHasNegValues;
    PadMode edgeMode;
    float [] kernelUnitLength = new float[2];
    boolean preserveAlpha = false;
    public ConvolveMatrixRable8Bit(Filter source) {
        super(source);
    }
    public Filter getSource() {
        return (Filter)getSources().get(0);
    }
    public void setSource(Filter src) {
        init(src);
    }
    public Kernel getKernel() {
        return kernel;
    }
    public void setKernel(Kernel k) {
        touch();
        this.kernel = k;
        kernelHasNegValues = false;
        float [] kv = k.getKernelData(null);
        for (int i=0; i<kv.length; i++)
            if (kv[i] < 0) {
                kernelHasNegValues = true;
                break;
            }
    }
    public Point getTarget() {
        return (Point)target.clone();
    }
    public void setTarget(Point pt) {
        touch();
        this.target = (Point)pt.clone();
    }
    public double getBias() {
        return bias;
    }
    public void setBias(double bias) {
        touch();
        this.bias = (float)bias;
    }
    public PadMode getEdgeMode() {
        return edgeMode;
    }
    public void setEdgeMode(PadMode edgeMode) {
        touch();
        this.edgeMode = edgeMode;
    }
    public double [] getKernelUnitLength() {
        if (kernelUnitLength == null)
            return null;
        double [] ret = new double[2];
        ret[0] = kernelUnitLength[0];
        ret[1] = kernelUnitLength[1];
        return ret;
    }
    public void setKernelUnitLength(double [] kernelUnitLength) {
        touch();
        if (kernelUnitLength == null) {
            this.kernelUnitLength = null;
            return;
        }
        if (this.kernelUnitLength == null)
            this.kernelUnitLength = new float[2];
        this.kernelUnitLength[0] = (float)kernelUnitLength[0];
        this.kernelUnitLength[1] = (float)kernelUnitLength[1];
    }
    public boolean getPreserveAlpha() {
        return preserveAlpha;
    }
    public void setPreserveAlpha(boolean preserveAlpha) {
        touch();
        this.preserveAlpha = preserveAlpha;
    }
    public void fixAlpha(BufferedImage bi) {
        if ((!bi.getColorModel().hasAlpha()) ||
            (!bi.isAlphaPremultiplied()))
            return;
        if (GraphicsUtil.is_INT_PACK_Data(bi.getSampleModel(), true))
            fixAlpha_INT_PACK(bi.getRaster());
        else
            fixAlpha_FALLBACK(bi.getRaster());
    }
    public void fixAlpha_INT_PACK(WritableRaster wr) {
        SinglePixelPackedSampleModel sppsm;
        sppsm = (SinglePixelPackedSampleModel)wr.getSampleModel();
        final int width = wr.getWidth();
        final int scanStride = sppsm.getScanlineStride();
        DataBufferInt db = (DataBufferInt)wr.getDataBuffer();
        final int base
            = (db.getOffset() +
               sppsm.getOffset(wr.getMinX()-wr.getSampleModelTranslateX(),
                               wr.getMinY()-wr.getSampleModelTranslateY()));
        final int[] pixels = db.getBankData()[0];
        for (int y=0; y<wr.getHeight(); y++) {
            int sp = base + y*scanStride;
            final int end = sp + width;
            while (sp < end) {
                int pixel = pixels[sp];
                int a = pixel>>>24;          
                int v = (pixel>>16)&0xFF;
                if (a < v) a = v;
                v = (pixel>> 8)&0xFF;
                if (a < v) a = v;
                v = (pixel    )&0xFF;
                if (a < v) a = v;
                pixels[sp] = (pixel&0x00FFFFFF) | (a << 24);
                sp++;
            }
        }
    }
    public void fixAlpha_FALLBACK(WritableRaster wr) {
        int x0=wr.getMinX();
        int w =wr.getWidth();
        int y0=wr.getMinY();
        int y1=y0 + wr.getHeight()-1;
        int bands = wr.getNumBands();
        int a, x, y, b, i;
        int [] pixel = null;
        for (y=y0; y<=y1; y++) {
            pixel = wr.getPixels(x0, y, w, 1, pixel);
            i=0;
            for (x=0; x<w; x++) {
                a=pixel[i];
                for (b=1; b<bands; b++)
                    if (pixel[i+b] > a) a = pixel[i+b];
                pixel[i+bands-1] = a;
                i+=bands;
            }
            wr.setPixels(x0, y, w, 1, pixel);
        }
    }
    public RenderedImage createRendering(RenderContext rc) {
        RenderingHints rh = rc.getRenderingHints();
        if (rh == null) rh = new RenderingHints(null);
        AffineTransform at = rc.getTransform();
        double sx = at.getScaleX();
        double sy = at.getScaleY();
        double shx = at.getShearX();
        double shy = at.getShearY();
        double tx = at.getTranslateX();
        double ty = at.getTranslateY();
        double scaleX = Math.sqrt(sx*sx + shy*shy);
        double scaleY = Math.sqrt(sy*sy + shx*shx);
        if (kernelUnitLength != null) {
            if (kernelUnitLength[0] > 0.0)
                scaleX = 1/kernelUnitLength[0];
            if (kernelUnitLength[1] > 0.0)
                scaleY = 1/kernelUnitLength[1];
        }
        Shape aoi = rc.getAreaOfInterest();
        if(aoi == null)
            aoi = getBounds2D();
        Rectangle2D r = aoi.getBounds2D();
        int kw = kernel.getWidth();
        int kh = kernel.getHeight();
        int kx = target.x;
        int ky = target.y;
        {
            double rx0 = r.getX() -(kx/scaleX);
            double ry0 = r.getY() -(ky/scaleY);
            double rx1 = rx0 + r.getWidth()  + (kw-1)/scaleX;
            double ry1 = ry0 + r.getHeight() + (kh-1)/scaleY;
            r = new Rectangle2D.Double(Math.floor(rx0),
                                       Math.floor(ry0),
                                       Math.ceil (rx1-Math.floor(rx0)),
                                       Math.ceil (ry1-Math.floor(ry0)));
        }
        AffineTransform srcAt
            = AffineTransform.getScaleInstance(scaleX, scaleY);
        AffineTransform resAt = new AffineTransform(sx/scaleX, shy/scaleX,
                                                    shx/scaleY, sy/scaleY,
                                                    tx, ty);
        RenderedImage ri;
        ri = getSource().createRendering(new RenderContext(srcAt, r, rh));
        if (ri == null)
            return null;
        CachableRed cr = convertSourceCS(ri);
        Shape devShape = srcAt.createTransformedShape(aoi);
        Rectangle2D devRect = devShape.getBounds2D();
        r = devRect;
        r = new Rectangle2D.Double(Math.floor(r.getX()-kx),
                                   Math.floor(r.getY()-ky),
                                   Math.ceil (r.getX()+r.getWidth())-
                                   Math.floor(r.getX())+(kw-1),
                                   Math.ceil (r.getY()+r.getHeight())-
                                   Math.floor(r.getY())+(kh-1));
        if (!r.getBounds().equals(cr.getBounds())) {
            if (edgeMode == PadMode.WRAP)
                throw new IllegalArgumentException
                    ("edgeMode=\"wrap\" is not supported by ConvolveMatrix.");
            cr = new PadRed(cr, r.getBounds(), edgeMode, rh);
        }
        if (bias != 0.0)
            throw new IllegalArgumentException
                ("Only bias equal to zero is supported in ConvolveMatrix.");
        BufferedImageOp op = new ConvolveOp(kernel,
                                            ConvolveOp.EDGE_NO_OP,
                                            rh);
        ColorModel cm = cr.getColorModel();
        Raster rr = cr.getData();
        WritableRaster wr = GraphicsUtil.makeRasterWritable(rr, 0, 0);
        int phaseShiftX = target.x - kernel.getXOrigin();
        int phaseShiftY = target.y - kernel.getYOrigin();
        int destX = (int)(r.getX() + phaseShiftX);
        int destY = (int)(r.getY() + phaseShiftY);
        BufferedImage destBI;
        if (!preserveAlpha) {
            cm = GraphicsUtil.coerceData(wr, cm, true);
            BufferedImage srcBI;
            srcBI = new BufferedImage(cm, wr, cm.isAlphaPremultiplied(), null);
            destBI = op.filter(srcBI, null);
            if (kernelHasNegValues) {
                fixAlpha(destBI);
            }
        } else {
            BufferedImage srcBI;
            srcBI = new BufferedImage(cm, wr, cm.isAlphaPremultiplied(), null);
            cm = new DirectColorModel(ColorSpace.getInstance
                                      (ColorSpace.CS_LINEAR_RGB), 24,
                                      0x00FF0000, 0x0000FF00,
                                      0x000000FF, 0x0, false,
                                      DataBuffer.TYPE_INT);
            BufferedImage tmpSrcBI = new BufferedImage
                (cm, cm.createCompatibleWritableRaster(wr.getWidth(),
                                                       wr.getHeight()),
                 cm.isAlphaPremultiplied(), null);
            GraphicsUtil.copyData(srcBI, tmpSrcBI);
            ColorModel dstCM = GraphicsUtil.Linear_sRGB_Unpre;
            destBI = new BufferedImage
                (dstCM, dstCM.createCompatibleWritableRaster(wr.getWidth(),
                                                             wr.getHeight()),
                 dstCM.isAlphaPremultiplied(), null);
            WritableRaster dstWR =
                Raster.createWritableRaster
                (cm.createCompatibleSampleModel(wr.getWidth(), wr.getHeight()),
                 destBI.getRaster().getDataBuffer(),
                 new Point(0,0));
            BufferedImage tmpDstBI = new BufferedImage
                (cm, dstWR, cm.isAlphaPremultiplied(), null);
            tmpDstBI = op.filter(tmpSrcBI, tmpDstBI);
            Rectangle srcRect = wr.getBounds();
            Rectangle dstRect = new Rectangle(srcRect.x-phaseShiftX,
                                              srcRect.y-phaseShiftY,
                                              srcRect.width, srcRect.height);
            GraphicsUtil.copyBand(wr, srcRect, wr.getNumBands()-1,
                                  destBI.getRaster(), dstRect,
                                  destBI.getRaster().getNumBands()-1);
        }
        cr = new BufferedImageCachableRed(destBI, destX, destY);
        cr = new PadRed(cr, devRect.getBounds(), PadMode.ZERO_PAD, rh);
        if (!resAt.isIdentity())
            cr = new AffineRed(cr, resAt, null);
        return cr;
    }
}
