package org.apache.batik.gvt.filter;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.renderable.AbstractRable;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.FilterAsAlphaRable;
import org.apache.batik.ext.awt.image.renderable.PadRable;
import org.apache.batik.ext.awt.image.renderable.PadRable8Bit;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.MultiplyAlphaRed;
import org.apache.batik.ext.awt.image.rendered.RenderedImageCachableRed;
import org.apache.batik.gvt.GraphicsNode;
public class MaskRable8Bit
    extends    AbstractRable
    implements Mask {
    protected GraphicsNode mask;
    protected Rectangle2D filterRegion;
    public MaskRable8Bit(Filter src, GraphicsNode mask, 
                             Rectangle2D filterRegion) {
        super(src, null);
        setMaskNode(mask);
        setFilterRegion(filterRegion);
    }
    public void setSource(Filter src) {
        init(src, null);
    }
    public Filter getSource() {
        return (Filter)getSources().get(0);
    }
    public Rectangle2D getFilterRegion(){
        return (Rectangle2D)filterRegion.clone();
    }
    public void setFilterRegion(Rectangle2D filterRegion){
        if(filterRegion == null){
            throw new IllegalArgumentException();
        }
        this.filterRegion = filterRegion;
    }
    public void setMaskNode(GraphicsNode mask) {
        touch();
        this.mask = mask;
    }
    public GraphicsNode getMaskNode() {
        return mask;
    }
    public Rectangle2D getBounds2D(){
        return (Rectangle2D)filterRegion.clone();
    }
    public RenderedImage createRendering(RenderContext rc) {
        Filter   maskSrc = getMaskNode().getGraphicsNodeRable(true);
        PadRable maskPad = new PadRable8Bit(maskSrc, getBounds2D(),
                                                PadMode.ZERO_PAD);
        maskSrc = new FilterAsAlphaRable(maskPad);
        RenderedImage ri = maskSrc.createRendering(rc);
        if (ri == null)
            return null;
        CachableRed maskCr = RenderedImageCachableRed.wrap(ri);
        PadRable maskedPad = new PadRable8Bit(getSource(),
                                                  getBounds2D(),
                                                  PadMode.ZERO_PAD);
        ri = maskedPad.createRendering(rc);
        if (ri == null)
            return null;
        CachableRed cr;
        cr = GraphicsUtil.wrap(ri);
        cr = GraphicsUtil.convertToLsRGB(cr);
        CachableRed ret = new MultiplyAlphaRed(cr, maskCr);
        return ret;
    }
}
