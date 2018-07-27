package org.apache.batik.ext.awt.image.renderable;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.rendered.AffineRed;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.GaussianBlurRed8Bit;
import org.apache.batik.ext.awt.image.rendered.PadRed;
public class GaussianBlurRable8Bit
    extends    AbstractColorInterpolationRable
    implements GaussianBlurRable {
    private double stdDeviationX;
    private double stdDeviationY;
    public GaussianBlurRable8Bit(Filter src,
                                 double stdevX, double stdevY) {
        super(src, null);
        setStdDeviationX(stdevX);
        setStdDeviationY(stdevY);
    }
    public void setStdDeviationX(double stdDeviationX){
        if(stdDeviationX < 0){
            throw new IllegalArgumentException();
        }
        touch();
        this.stdDeviationX = stdDeviationX;
    }
    public void setStdDeviationY(double stdDeviationY){
        if(stdDeviationY < 0){
            throw new IllegalArgumentException();
        }
        touch();
        this.stdDeviationY = stdDeviationY;
    }
    public double getStdDeviationX(){
        return stdDeviationX;
    }
    public double getStdDeviationY(){
        return stdDeviationY;
    }
    public void setSource(Filter src){
        init(src, null);
    }
    static final double DSQRT2PI = (Math.sqrt(2*Math.PI)*3.0/4.0);
    public Rectangle2D getBounds2D(){
        Rectangle2D src = getSource().getBounds2D();
        float dX = (float)(stdDeviationX*DSQRT2PI);
        float dY = (float)(stdDeviationY*DSQRT2PI);
        float radX = 3*dX/2;
        float radY = 3*dY/2;
        return new Rectangle2D.Float
            ((float)(src.getMinX()  -radX),
             (float)(src.getMinY()  -radY),
             (float)(src.getWidth() +2*radX),
             (float)(src.getHeight()+2*radY));
    }
    public Filter getSource(){
        return (Filter)getSources().get(0);
    }
    public static final double eps = 0.0001;
    public static boolean eps_eq(double f1, double f2) {
        return ((f1 >= f2-eps) && (f1 <= f2+eps));
    }
    public static boolean eps_abs_eq(double f1, double f2) {
        if (f1 <0) f1 = -f1;
        if (f2 <0) f2 = -f2;
        return eps_eq(f1, f2);
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
        double sdx = stdDeviationX*scaleX;
        double sdy = stdDeviationY*scaleY;
        AffineTransform srcAt;
        AffineTransform resAt;
        int outsetX, outsetY;
        if ((sdx < 10)           &&
            (sdy < 10)           &&
            eps_eq    (sdx, sdy) &&
            eps_abs_eq(sx/scaleX, sy/scaleY)) {
            srcAt = at;
            resAt = null;
            outsetX = 0;
            outsetY = 0;
        } else {
            if (sdx > 10) {
                scaleX = scaleX*10/sdx;
                sdx = 10;
            }
            if (sdy > 10) {
                scaleY = scaleY*10/sdy;
                sdy = 10;
            }
            srcAt = AffineTransform.getScaleInstance(scaleX, scaleY);
            resAt = new AffineTransform(sx/scaleX, shy/scaleX,
                                        shx/scaleY,  sy/scaleY,
                                        tx, ty);
            outsetX = 1;
            outsetY = 1;
        }
        Shape aoi = rc.getAreaOfInterest();
        if(aoi == null)
            aoi = getBounds2D();
        Shape devShape = srcAt.createTransformedShape(aoi);
        Rectangle devRect = devShape.getBounds();
        outsetX += GaussianBlurRed8Bit.surroundPixels(sdx, rh);
        outsetY += GaussianBlurRed8Bit.surroundPixels(sdy, rh);
        devRect.x      -= outsetX;
        devRect.y      -= outsetY;
        devRect.width  += 2*outsetX;
        devRect.height += 2*outsetY;
        Rectangle2D r;
        try {
            AffineTransform invSrcAt = srcAt.createInverse();
            r = invSrcAt.createTransformedShape(devRect).getBounds2D();
        } catch (NoninvertibleTransformException nte) {
            r = aoi.getBounds2D();
            r = new Rectangle2D.Double(r.getX()-outsetX/scaleX,
                                       r.getY()-outsetY/scaleY,
                                       r.getWidth() +2*outsetX/scaleX,
                                       r.getHeight()+2*outsetY/scaleY);
        }
        RenderedImage ri;
        ri = getSource().createRendering(new RenderContext(srcAt, r, rh));
        if (ri == null)
            return null;
        CachableRed cr = convertSourceCS(ri);
        if (!devRect.equals(cr.getBounds())) {
            cr = new PadRed(cr, devRect, PadMode.ZERO_PAD, rh);
        }
        cr = new GaussianBlurRed8Bit(cr, sdx, sdy, rh);
        if ((resAt != null) && (!resAt.isIdentity()))
            cr = new AffineRed(cr, resAt, rh);
        return cr;
    }
    public Shape getDependencyRegion(int srcIndex, Rectangle2D outputRgn){
        if(srcIndex != 0)
            outputRgn = null;
        else {
            float dX = (float)(stdDeviationX*DSQRT2PI);
            float dY = (float)(stdDeviationY*DSQRT2PI);
            float radX = 3*dX/2;
            float radY = 3*dY/2;
            outputRgn = new Rectangle2D.Float
                            ((float)(outputRgn.getMinX()  -radX),
                             (float)(outputRgn.getMinY()  -radY),
                             (float)(outputRgn.getWidth() +2*radX),
                             (float)(outputRgn.getHeight()+2*radY));
            Rectangle2D bounds = getBounds2D();
            if ( ! outputRgn.intersects(bounds) )
                return new Rectangle2D.Float();
            outputRgn = outputRgn.createIntersection(bounds);
        }
        return outputRgn;
    }
    public Shape getDirtyRegion(int srcIndex, Rectangle2D inputRgn){
        Rectangle2D dirtyRegion = null;
        if(srcIndex == 0){
            float dX = (float)(stdDeviationX*DSQRT2PI);
            float dY = (float)(stdDeviationY*DSQRT2PI);
            float radX = 3*dX/2;
            float radY = 3*dY/2;
            inputRgn = new Rectangle2D.Float
                            ((float)(inputRgn.getMinX()  -radX),
                             (float)(inputRgn.getMinY()  -radY),
                             (float)(inputRgn.getWidth() +2*radX),
                             (float)(inputRgn.getHeight()+2*radY));
            Rectangle2D bounds = getBounds2D();
            if ( ! inputRgn.intersects(bounds) )
                return new Rectangle2D.Float();
            dirtyRegion = inputRgn.createIntersection(bounds);
        }
        return dirtyRegion;
    }
}
