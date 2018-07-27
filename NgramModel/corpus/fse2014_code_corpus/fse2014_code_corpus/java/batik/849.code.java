package org.apache.batik.ext.awt.image.renderable;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import org.apache.batik.ext.awt.image.rendered.AffineRed;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.TranslateRed;
public class RedRable
    extends    AbstractRable {
    CachableRed src;
    public RedRable(CachableRed src) {
        super((Filter)null);
        this.src = src;
    }
    public CachableRed getSource() {
        return src;
    }
    public Object getProperty(String name) {
        return src.getProperty(name);
    }
    public String [] getPropertyNames() {
        return src.getPropertyNames();
    }
    public Rectangle2D getBounds2D() {
        return getSource().getBounds();
    }
    public RenderedImage createDefaultRendering() {
        return getSource();
    }
    public RenderedImage createRendering(RenderContext rc) {
        RenderingHints rh = rc.getRenderingHints();
        if (rh == null) rh = new RenderingHints(null);
        Shape aoi = rc.getAreaOfInterest();
        Rectangle aoiR;
        if (aoi != null)
            aoiR = aoi.getBounds();
        else
            aoiR = getBounds2D().getBounds();
        AffineTransform at = rc.getTransform();
        CachableRed cr = getSource();
        if ( ! aoiR.intersects(cr.getBounds()) )
            return null;
        if (at.isIdentity()) {
            return cr;
        }
        if ((at.getScaleX() == 1.0) && (at.getScaleY() == 1.0) &&
            (at.getShearX() == 0.0) && (at.getShearY() == 0.0)) {
            int xloc = (int)(cr.getMinX()+at.getTranslateX());
            int yloc = (int)(cr.getMinY()+at.getTranslateY());
            double dx = xloc - (cr.getMinX()+at.getTranslateX());
            double dy = yloc - (cr.getMinY()+at.getTranslateY());
            if (((dx > -0.0001) && (dx < 0.0001)) &&
                ((dy > -0.0001) && (dy < 0.0001))) {
                return new TranslateRed(cr, xloc, yloc);
            }
        }
        return new AffineRed(cr, at, rh);
    }
}
