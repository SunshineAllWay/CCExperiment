package org.apache.batik.ext.awt.image.renderable;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import org.apache.batik.ext.awt.ColorSpaceHintKey;
import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.FilterAsAlphaRed;
import org.apache.batik.ext.awt.image.rendered.RenderedImageCachableRed;
public class FilterAsAlphaRable
    extends    AbstractRable {
    public FilterAsAlphaRable(Filter src) {
        super(src, null);
    }
    public Filter getSource() {
        return (Filter)getSources().get(0);
    }
    public Rectangle2D getBounds2D(){
        return getSource().getBounds2D();
    }
    public RenderedImage createRendering(RenderContext rc) {
        AffineTransform at = rc.getTransform();
        RenderingHints rh = rc.getRenderingHints();
        if (rh == null) rh = new RenderingHints(null);
        Shape aoi = rc.getAreaOfInterest();
        if (aoi == null) {
            aoi = getBounds2D();
        }
        rh.put(RenderingHintsKeyExt.KEY_COLORSPACE, 
               ColorSpaceHintKey.VALUE_COLORSPACE_ALPHA_CONVERT);
        RenderedImage ri;
        ri = getSource().createRendering(new RenderContext(at, aoi, rh));
        if (ri == null)
            return null;
        CachableRed cr = RenderedImageCachableRed.wrap(ri);
        Object val = cr.getProperty(ColorSpaceHintKey.PROPERTY_COLORSPACE);
        if (val == ColorSpaceHintKey.VALUE_COLORSPACE_ALPHA_CONVERT)
            return cr;
        return new FilterAsAlphaRed(cr);
    }
}
