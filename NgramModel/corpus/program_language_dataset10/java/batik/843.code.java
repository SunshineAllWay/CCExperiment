package org.apache.batik.ext.awt.image.renderable;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.RenderContext;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.rendered.AffineRed;
import org.apache.batik.ext.awt.image.rendered.BufferedImageCachableRed;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.MorphologyOp;
import org.apache.batik.ext.awt.image.rendered.PadRed;
import org.apache.batik.ext.awt.image.rendered.RenderedImageCachableRed;
public class MorphologyRable8Bit 
    extends AbstractRable
    implements MorphologyRable {
    private double radiusX, radiusY;
    private boolean doDilation;
    public MorphologyRable8Bit(Filter src,
                                   double radiusX,
                                   double radiusY,
                                   boolean doDilation){
        super(src, null);
        setRadiusX(radiusX);
        setRadiusY(radiusY);
        setDoDilation(doDilation);
    }
    public Filter getSource(){
        return (Filter)getSources().get(0);
    }
    public void setSource(Filter src){
        init(src, null);
    }
    public Rectangle2D getBounds2D(){
        return getSource().getBounds2D();
    }
    public void setRadiusX(double radiusX){
        if(radiusX <= 0){
            throw new IllegalArgumentException();
        }
        touch();
        this.radiusX = radiusX;
    }
    public void setRadiusY(double radiusY){
        if(radiusY <= 0){
            throw new IllegalArgumentException();
        }
        touch();
        this.radiusY = radiusY;
    }
    public void setDoDilation(boolean doDilation){
        touch();
        this.doDilation = doDilation;
    }
    public boolean getDoDilation(){
        return doDilation;
    }
    public double getRadiusX(){
        return radiusX;
    }
    public double getRadiusY(){
        return radiusY;
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
        AffineTransform srcAt;
        srcAt = AffineTransform.getScaleInstance(scaleX, scaleY);
        int radX = (int)Math.round(radiusX*scaleX);
        int radY = (int)Math.round(radiusY*scaleY);
        MorphologyOp op = null;
        if(radX > 0 && radY > 0){
            op = new MorphologyOp(radX, radY, doDilation);
        }
        AffineTransform resAt;
        resAt = new AffineTransform(sx/scaleX, shy/scaleX,
                                    shx/scaleY,  sy/scaleY,
                                    tx, ty);
        Shape aoi = rc.getAreaOfInterest();
        if(aoi == null) {
            aoi = getBounds2D();
        }
        Rectangle2D r = aoi.getBounds2D();
        r = new Rectangle2D.Double(r.getX()-radX/scaleX, 
                                   r.getY()-radY/scaleY,
                                   r.getWidth() +2*radX/scaleX, 
                                   r.getHeight()+2*radY/scaleY);
        RenderedImage ri;
        ri = getSource().createRendering(new RenderContext(srcAt, r, rh));
        if (ri == null) 
            return null;
        CachableRed cr;
        cr = new RenderedImageCachableRed(ri);
        Shape devShape = srcAt.createTransformedShape(aoi.getBounds2D());
        r = devShape.getBounds2D();
        r = new Rectangle2D.Double(r.getX()-radX, 
                                   r.getY()-radY,
                                   r.getWidth() +2*radX, 
                                   r.getHeight()+2*radY);
        cr = new PadRed(cr, r.getBounds(), PadMode.ZERO_PAD, rh);
        ColorModel cm = ri.getColorModel();
        Raster rr = cr.getData();
        Point  pt = new Point(0,0);
        WritableRaster wr = Raster.createWritableRaster(rr.getSampleModel(),
                                                        rr.getDataBuffer(),
                                                        pt);
        BufferedImage srcBI;
        srcBI = new BufferedImage(cm, wr, cm.isAlphaPremultiplied(), null);
        BufferedImage destBI;
        if(op != null){
            destBI = op.filter(srcBI, null);
        }
        else{
            destBI = srcBI;
        }
        final int rrMinX = cr.getMinX();
        final int rrMinY = cr.getMinY();
        cr = new BufferedImageCachableRed(destBI, rrMinX, rrMinY);
        if (!resAt.isIdentity())
            cr = new AffineRed(cr, resAt, rh);
        return cr;
    }
    public Shape getDependencyRegion(int srcIndex, Rectangle2D outputRgn){
        return super.getDependencyRegion(srcIndex, outputRgn);
    }
    public Shape getDirtyRegion(int srcIndex, Rectangle2D inputRgn){
        return super.getDirtyRegion(srcIndex, inputRgn);
    }
}
