package org.apache.batik.ext.awt.image.renderable;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import org.apache.batik.ext.awt.image.CompositeRule;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.SVGComposite;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.CompositeRed;
import org.apache.batik.ext.awt.image.rendered.FloodRed;
public class CompositeRable8Bit
    extends    AbstractColorInterpolationRable
    implements CompositeRable, PaintRable {
    protected CompositeRule rule;
    public CompositeRable8Bit(List srcs,
                              CompositeRule rule,
                              boolean csIsLinear) {
        super(srcs);
        setColorSpaceLinear(csIsLinear);
        this.rule = rule;
    }
    public void setSources(List srcs) {
        init(srcs, null);
    }
    public void setCompositeRule(CompositeRule cr) {
        touch();
        this.rule =  cr;
    }
    public CompositeRule getCompositeRule() {
        return this.rule;
    }
    public boolean paintRable(Graphics2D g2d) {
        Composite c = g2d.getComposite();
        if (!SVGComposite.OVER.equals(c))
            return false;
        if (getCompositeRule() != CompositeRule.OVER)
            return false;
        ColorSpace crCS = getOperationColorSpace();
        ColorSpace g2dCS = GraphicsUtil.getDestinationColorSpace(g2d);
        if ((g2dCS == null) || (g2dCS != crCS)) {
            return false;
        }
        Iterator i = getSources().iterator();
        while (i.hasNext()) {
            GraphicsUtil.drawImage(g2d, (Filter)i.next());
        }
        return true;
    }
    public RenderedImage createRendering(RenderContext rc) {
        if (srcs.size() == 0)
            return null;
        RenderingHints rh = rc.getRenderingHints();
        if (rh == null) rh = new RenderingHints(null);
        AffineTransform at = rc.getTransform();
        Shape aoi = rc.getAreaOfInterest();
        Rectangle2D aoiR;
        if (aoi == null)
            aoiR = getBounds2D();
        else {
            aoiR = aoi.getBounds2D();
            Rectangle2D bounds2d = getBounds2D();
            if ( ! bounds2d.intersects(aoiR) )
                return null;
            Rectangle2D.intersect(aoiR, bounds2d, aoiR);
        }
        Rectangle devRect = at.createTransformedShape(aoiR).getBounds();
        rc = new RenderContext(at, aoiR, rh);
        List srcs = new ArrayList();
        Iterator i = getSources().iterator();
        while (i.hasNext()) {
            Filter filt = (Filter)i.next();
            RenderedImage ri = filt.createRendering(rc);
            if (ri != null) {
                CachableRed cr;
                cr = convertSourceCS(ri);
                srcs.add(cr);
            } else {
                switch (rule.getRule()) {
                case CompositeRule.RULE_IN:
                    return null;
                case CompositeRule.RULE_OUT:
                    srcs.clear();
                    break;
                case CompositeRule.RULE_ARITHMETIC:
                    srcs.add(new FloodRed(devRect));
                    break;
                default:
                    break;
                }
            }
        }
        if (srcs.size() == 0)
            return null;
        CachableRed cr = new CompositeRed(srcs, rule);
        return cr;
    }
}
