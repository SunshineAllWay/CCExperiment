package org.apache.batik.ext.awt.image.rendered;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import org.apache.batik.ext.awt.image.GraphicsUtil;
public class AffineRed extends AbstractRed {
    RenderingHints  hints;
    AffineTransform src2me;
    AffineTransform me2src;
    public AffineTransform getTransform() {
        return (AffineTransform)src2me.clone();
    }
    public CachableRed getSource() {
        return (CachableRed)getSources().get(0);
    }
    public AffineRed(CachableRed     src,
                     AffineTransform src2me,
                     RenderingHints  hints) {
        super(); 
        this.src2me = src2me;
        this.hints  = hints;
        try {
            me2src = src2me.createInverse();
        } catch (NoninvertibleTransformException nite) {
            me2src = null;
        }
        Rectangle srcBounds = src.getBounds();
        Rectangle myBounds;
        myBounds = src2me.createTransformedShape(srcBounds).getBounds();
        ColorModel cm = fixColorModel(src);
        SampleModel sm = fixSampleModel(src, cm, myBounds);
        Point2D pt = new Point2D.Float(src.getTileGridXOffset(),
                                       src.getTileGridYOffset());
        pt = src2me.transform(pt, null);
        init(src, myBounds, cm, sm,
             (int)pt.getX(), (int)pt.getY(), null);
    }
    public WritableRaster copyData(WritableRaster wr) {
        PadRed.ZeroRecter zr = PadRed.ZeroRecter.getZeroRecter(wr);
        zr.zeroRect(new Rectangle(wr.getMinX(), wr.getMinY(),
                                  wr.getWidth(), wr.getHeight()));
        genRect(wr);
        return wr;
    }
    public Raster getTile(int x, int y) {
        if (me2src == null)
            return null;
        int tx = tileGridXOff+x*tileWidth;
        int ty = tileGridYOff+y*tileHeight;
        Point pt = new Point(tx, ty);
        WritableRaster wr = Raster.createWritableRaster(sm, pt);
        genRect(wr);
        return wr;
    }
    public void genRect(WritableRaster wr) {
        if (me2src == null)
            return;
        Rectangle srcR
            = me2src.createTransformedShape(wr.getBounds()).getBounds();
        srcR.setBounds(srcR.x-1, srcR.y-1, srcR.width+2, srcR.height+2);
        CachableRed src = (CachableRed)getSources().get(0);
        if ( ! srcR.intersects(src.getBounds()) )
            return;
        Raster srcRas = src.getData(srcR.intersection(src.getBounds()));
        if (srcRas == null)
            return;
        AffineTransform aff = (AffineTransform)src2me.clone();
        aff.concatenate(AffineTransform.getTranslateInstance
                        (srcRas.getMinX(), srcRas.getMinY()));
        Point2D srcPt = new Point2D.Float(wr.getMinX(), wr.getMinY());
        srcPt         = me2src.transform(srcPt, null);
        Point2D destPt = new Point2D.Double(srcPt.getX()-srcRas.getMinX(),
                                            srcPt.getY()-srcRas.getMinY());
        destPt = aff.transform(destPt, null);
        aff.preConcatenate(AffineTransform.getTranslateInstance
                           (-destPt.getX(), -destPt.getY()));
        AffineTransformOp op = new AffineTransformOp(aff, hints);
        BufferedImage srcBI, myBI;
        ColorModel srcCM = src.getColorModel();
        ColorModel myCM = getColorModel();
        WritableRaster srcWR = (WritableRaster)srcRas;
        srcCM = GraphicsUtil.coerceData(srcWR, srcCM, true);
        srcBI = new BufferedImage(srcCM,
                                  srcWR.createWritableTranslatedChild(0,0),
                                  srcCM.isAlphaPremultiplied(), null);
        myBI = new BufferedImage(myCM,wr.createWritableTranslatedChild(0,0),
                                 myCM.isAlphaPremultiplied(), null);
        op.filter(srcBI, myBI);
    }
    protected static ColorModel fixColorModel(CachableRed src) {
        ColorModel  cm = src.getColorModel();
        if (cm.hasAlpha()) {
            if (!cm.isAlphaPremultiplied())
                cm = GraphicsUtil.coerceColorModel(cm, true);
            return cm;
        }
        ColorSpace cs = cm.getColorSpace();
        int b = src.getSampleModel().getNumBands()+1;
        if (b == 4) {
            int [] masks = new int[4];
            for (int i=0; i < b-1; i++)
                masks[i] = 0xFF0000 >> (8*i);
            masks[3] = 0xFF << (8*(b-1));
            return new DirectColorModel(cs, 8*b, masks[0], masks[1],
                                        masks[2], masks[3],
                                        true, DataBuffer.TYPE_INT);
        }
        int [] bits = new int[b];
        for (int i=0; i<b; i++)
            bits[i] = 8;
        return new ComponentColorModel(cs, bits, true, true,
                                       Transparency.TRANSLUCENT,
                                       DataBuffer.TYPE_INT);
    }
    protected SampleModel fixSampleModel(CachableRed src,
                                         ColorModel  cm,
                                         Rectangle   bounds) {
        SampleModel sm = src.getSampleModel();
        int defSz = AbstractTiledRed.getDefaultTileSize();
        int w = sm.getWidth();
        if (w < defSz) w = defSz;
        if (w > bounds.width)  w = bounds.width;
        int h = sm.getHeight();
        if (h < defSz) h = defSz;
        if (h > bounds.height) h = bounds.height;
        if ((w <= 0) || (h <= 0)) {
            w = 1;
            h = 1;
        }
        return cm.createCompatibleSampleModel(w, h);
    }
}
