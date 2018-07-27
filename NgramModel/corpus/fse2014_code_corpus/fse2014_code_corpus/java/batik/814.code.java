package org.apache.batik.ext.awt.image.renderable;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import org.apache.batik.ext.awt.image.GraphicsUtil;
public class AffineRable8Bit 
    extends    AbstractRable
    implements AffineRable, PaintRable {
    AffineTransform affine;
    AffineTransform invAffine;
    public AffineRable8Bit(Filter src, AffineTransform affine) {
        init(src);
        setAffine(affine);
    }
    public Rectangle2D getBounds2D() {
        Filter src = getSource();
        Rectangle2D r = src.getBounds2D();
        return affine.createTransformedShape(r).getBounds2D();
    }
    public Filter getSource() {
        return (Filter)srcs.get(0);
    }
    public void setSource(Filter src) {
        init(src);
    }
    public void setAffine(AffineTransform affine) {
        touch();
        this.affine = affine;
        try {
            invAffine = affine.createInverse();
        } catch (NoninvertibleTransformException e) {
            invAffine = null;
        }
    }
    public AffineTransform getAffine() {
        return (AffineTransform)affine.clone();
    }
    public boolean paintRable(Graphics2D g2d) {
        AffineTransform at = g2d.getTransform();
        g2d.transform(getAffine());
        GraphicsUtil.drawImage(g2d, getSource());
        g2d.setTransform(at);
        return true;
    }
    public RenderedImage createRendering(RenderContext rc) {
        if (invAffine == null) return null;
        RenderingHints rh = rc.getRenderingHints();
        if (rh == null) rh = new RenderingHints(null);
        Shape aoi = rc.getAreaOfInterest();
        if (aoi != null)
            aoi = invAffine.createTransformedShape(aoi);
        AffineTransform at = rc.getTransform();
        at.concatenate(affine);
        return getSource().createRendering(new RenderContext(at, aoi, rh));
    }
    public Shape getDependencyRegion(int srcIndex, Rectangle2D outputRgn) {
        if (srcIndex != 0)
            throw new IndexOutOfBoundsException("Affine only has one input");
        if (invAffine == null)
            return null;
        return invAffine.createTransformedShape(outputRgn);
    }
    public Shape getDirtyRegion(int srcIndex, Rectangle2D inputRgn) {
        if (srcIndex != 0)
            throw new IndexOutOfBoundsException("Affine only has one input");
        return affine.createTransformedShape(inputRgn);
    }
}
