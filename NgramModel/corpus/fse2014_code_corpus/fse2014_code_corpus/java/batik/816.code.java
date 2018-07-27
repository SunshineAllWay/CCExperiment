package org.apache.batik.ext.awt.image.renderable;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.rendered.BufferedImageCachableRed;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.MultiplyAlphaRed;
import org.apache.batik.ext.awt.image.rendered.PadRed;
import org.apache.batik.ext.awt.image.rendered.RenderedImageCachableRed;
public class ClipRable8Bit
    extends    AbstractRable
    implements ClipRable {
    protected boolean useAA;
    protected Shape clipPath;
    public ClipRable8Bit(Filter src, Shape clipPath) {
        super(src, null);
        setClipPath(clipPath);
        setUseAntialiasedClip(false);
    }
    public ClipRable8Bit(Filter src, Shape clipPath, boolean useAA) {
        super(src, null);
        setClipPath(clipPath);
        setUseAntialiasedClip(useAA);
    }
    public void setSource(Filter src) {
        init(src, null);
    }
    public Filter getSource() {
        return (Filter)getSources().get(0);
    }
    public void setUseAntialiasedClip(boolean useAA) {
        touch();
        this.useAA = useAA;
    }
    public boolean getUseAntialiasedClip() {
        return useAA;
    }
    public void setClipPath(Shape clipPath) {
        touch();
        this.clipPath = clipPath;
    }
    public Shape getClipPath() {
        return clipPath;
    }
    public Rectangle2D getBounds2D(){
        return getSource().getBounds2D();
    }
    public RenderedImage createRendering(RenderContext rc) {
        AffineTransform usr2dev = rc.getTransform();
        RenderingHints rh = rc.getRenderingHints();
        if (rh == null)  rh = new RenderingHints(null);
        Shape aoi = rc.getAreaOfInterest();
        if (aoi == null) aoi = getBounds2D();
        Rectangle2D rect     = getBounds2D();
        Rectangle2D clipRect = clipPath.getBounds2D();
        Rectangle2D aoiRect  = aoi.getBounds2D();
        if ( ! rect.intersects(clipRect) )
            return null;
        Rectangle2D.intersect(rect, clipRect, rect);
        if ( ! rect.intersects(aoiRect) )
            return null;
        Rectangle2D.intersect(rect, aoi.getBounds2D(), rect);
        Rectangle devR = usr2dev.createTransformedShape(rect).getBounds();
        if ((devR.width == 0) || (devR.height == 0))
            return null;
        BufferedImage bi = new BufferedImage(devR.width, devR.height,
                                             BufferedImage.TYPE_BYTE_GRAY);
        Shape devShape = usr2dev.createTransformedShape(getClipPath());
        Rectangle devAOIR;
        devAOIR = usr2dev.createTransformedShape(aoi).getBounds();
        Graphics2D g2d = GraphicsUtil.createGraphics(bi, rh);
        if (false) {
            java.util.Set s = rh.keySet();
            java.util.Iterator i = s.iterator();
            while (i.hasNext()) {
                Object o = i.next();
                System.out.println("XXX: " + o + " -> " + rh.get(o));
            }
        }
        g2d.translate(-devR.x, -devR.y);
        g2d.setPaint(Color.white);
        g2d.fill(devShape);
        g2d.dispose();
        RenderedImage ri;
        ri = getSource().createRendering(new RenderContext(usr2dev, rect, rh));
        CachableRed cr, clipCr;
        cr = RenderedImageCachableRed.wrap(ri);
        clipCr = new BufferedImageCachableRed(bi, devR.x, devR.y);
        CachableRed ret = new MultiplyAlphaRed(cr, clipCr);
        ret = new PadRed(ret, devAOIR, PadMode.ZERO_PAD, rh);
        return ret;
    }
}
