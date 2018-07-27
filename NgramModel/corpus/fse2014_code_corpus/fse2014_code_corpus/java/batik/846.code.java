package org.apache.batik.ext.awt.image.renderable;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.SVGComposite;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.PadRed;
public class PadRable8Bit extends AbstractRable
    implements PadRable, PaintRable {
    PadMode           padMode;
    Rectangle2D       padRect;
    public PadRable8Bit(Filter src,
                        Rectangle2D padRect,
                        PadMode     padMode) {
        super.init(src, null);
        this.padRect = padRect;
        this.padMode = padMode;
    }
    public Filter getSource() {
        return (Filter)srcs.get(0);
    }
    public void setSource(Filter src) {
        super.init(src, null);
    }
    public Rectangle2D getBounds2D() {
        return (Rectangle2D)padRect.clone();
    }
    public void setPadRect(Rectangle2D rect) {
        touch();
        this.padRect = rect;
    }
    public Rectangle2D getPadRect() {
        return (Rectangle2D)padRect.clone();
    }
    public void setPadMode(PadMode padMode) {
        touch();
        this.padMode = padMode;
    }
    public PadMode getPadMode() {
        return padMode;
    }
    public boolean paintRable(Graphics2D g2d) {
        Composite c = g2d.getComposite();
        if (!SVGComposite.OVER.equals(c))
            return false;
        if (getPadMode() != PadMode.ZERO_PAD)
            return false;
        Rectangle2D padBounds = getPadRect();
        Shape clip = g2d.getClip();
        g2d.clip(padBounds);
        GraphicsUtil.drawImage(g2d, getSource());
        g2d.setClip(clip);
        return true;
    }
    public RenderedImage createRendering(RenderContext rc) {
        RenderingHints rh = rc.getRenderingHints();
        if (rh == null) rh = new RenderingHints(null);
        Filter src = getSource();
        Shape aoi = rc.getAreaOfInterest();
        if(aoi == null){
            aoi = getBounds2D();
        }
        AffineTransform usr2dev = rc.getTransform();
        Rectangle2D srect = src.getBounds2D();
        Rectangle2D rect  = getBounds2D();
        Rectangle2D arect = aoi.getBounds2D();
        if ( ! arect.intersects(rect) )
            return null;
        Rectangle2D.intersect(arect, rect, arect);
        RenderedImage ri = null;
        if ( arect.intersects(srect) ) {
            srect = (Rectangle2D)srect.clone();
            Rectangle2D.intersect(srect, arect, srect);
            RenderContext srcRC = new RenderContext(usr2dev, srect, rh);
            ri = src.createRendering(srcRC);
        }
        if (ri == null)
            ri = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        CachableRed cr = GraphicsUtil.wrap(ri);
        arect = usr2dev.createTransformedShape(arect).getBounds2D();
        cr = new PadRed(cr, arect.getBounds(), padMode, rh);
        return cr;
    }
    public Shape getDependencyRegion(int srcIndex, Rectangle2D outputRgn) {
        if (srcIndex != 0)
            throw new IndexOutOfBoundsException("Affine only has one input");
        Rectangle2D srect = getSource().getBounds2D();
        if ( ! srect.intersects(outputRgn) )
            return new Rectangle2D.Float();
        Rectangle2D.intersect(srect, outputRgn, srect);
        Rectangle2D bounds = getBounds2D();
        if ( ! srect.intersects(bounds) )
            return new Rectangle2D.Float();
        Rectangle2D.intersect(srect, bounds, srect);
        return srect;
    }
    public Shape getDirtyRegion(int srcIndex, Rectangle2D inputRgn) {
        if (srcIndex != 0)
            throw new IndexOutOfBoundsException("Affine only has one input");
        inputRgn = (Rectangle2D)inputRgn.clone();
        Rectangle2D bounds = getBounds2D();
        if ( ! inputRgn.intersects(bounds) )
            return new Rectangle2D.Float();
        Rectangle2D.intersect(inputRgn, bounds, inputRgn);
        return inputRgn;
    }
}
