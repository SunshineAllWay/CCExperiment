package org.apache.batik.gvt.filter;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import org.apache.batik.ext.awt.image.CompositeRule;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.renderable.AbstractRable;
import org.apache.batik.ext.awt.image.renderable.AffineRable8Bit;
import org.apache.batik.ext.awt.image.renderable.CompositeRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.PadRable8Bit;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
public class BackgroundRable8Bit
    extends    AbstractRable {
    private GraphicsNode node;
    public GraphicsNode getGraphicsNode(){
        return node;
    }
    public void setGraphicsNode(GraphicsNode node){
        if(node == null){
            throw new IllegalArgumentException();
        }
        this.node = node;
    }
    public BackgroundRable8Bit(GraphicsNode node){
        if(node == null)
            throw new IllegalArgumentException();
        this.node = node;
    }
    static Rectangle2D addBounds(CompositeGraphicsNode cgn,
                                 GraphicsNode child,
                                 Rectangle2D  init) {
        List children = cgn.getChildren();
        Iterator i = children.iterator();
        Rectangle2D r2d = null;
        while (i.hasNext()) {
            GraphicsNode gn = (GraphicsNode)i.next();
            if (gn == child)
                break;
            Rectangle2D cr2d = gn.getBounds();
            if (cr2d == null) continue;
            AffineTransform at = gn.getTransform();
            if (at != null)
                cr2d = at.createTransformedShape(cr2d).getBounds2D();
            if (r2d == null) r2d = (Rectangle2D)cr2d.clone();
            else             r2d.add(cr2d);
        }
        if (r2d == null) {
            if (init == null)
                return CompositeGraphicsNode.VIEWPORT;
            return init;
        }
        if (init == null)
            return r2d;
        init.add(r2d);
        return init;
    }
    static Rectangle2D getViewportBounds(GraphicsNode gn,
                                         GraphicsNode child) {
        Rectangle2D r2d = null;
        if (gn instanceof CompositeGraphicsNode) {
            CompositeGraphicsNode cgn = (CompositeGraphicsNode)gn;
            r2d = cgn.getBackgroundEnable();
        }
        if (r2d == null)
            r2d = getViewportBounds(gn.getParent(), gn);
        if (r2d == null)
            return null;
        if (r2d == CompositeGraphicsNode.VIEWPORT) {
            if (child == null)
                return (Rectangle2D)gn.getPrimitiveBounds().clone();
            CompositeGraphicsNode cgn = (CompositeGraphicsNode)gn;
            return addBounds(cgn, child, null);
        }
        AffineTransform at = gn.getTransform();
        if (at != null) {
            try {
                at = at.createInverse();
                r2d = at.createTransformedShape(r2d).getBounds2D();
            } catch (NoninvertibleTransformException nte) {
                r2d = null;
            }
        }
        if (child != null) {
            CompositeGraphicsNode cgn = (CompositeGraphicsNode)gn;
            r2d = addBounds(cgn, child, r2d);
        } else {
            Rectangle2D gnb = gn.getPrimitiveBounds();
            if (gnb != null)
                r2d.add(gnb);
        }
        return r2d;
    }
    static Rectangle2D getBoundsRecursive(GraphicsNode gn,
                                          GraphicsNode child) {
        Rectangle2D r2d = null;
        if (gn == null) {
            return null;
        }
        if (gn instanceof CompositeGraphicsNode) {
            CompositeGraphicsNode cgn = (CompositeGraphicsNode)gn;
            r2d = cgn.getBackgroundEnable();
        }
        if (r2d != null)
            return  r2d;
        r2d = getBoundsRecursive(gn.getParent(), gn);
        if (r2d == null) {
            return new Rectangle2D.Float(0, 0, 0, 0);
        }
        if (r2d == CompositeGraphicsNode.VIEWPORT)
            return r2d;
        AffineTransform at = gn.getTransform();
        if (at != null) {
            try {
                at = at.createInverse();
                r2d = at.createTransformedShape(r2d).getBounds2D();
            } catch (NoninvertibleTransformException nte) {
                r2d = null;
            }
        }
        return r2d;
    }
    public Rectangle2D getBounds2D() {
        Rectangle2D r2d = getBoundsRecursive(node, null);
        if (r2d == CompositeGraphicsNode.VIEWPORT) {
            r2d = getViewportBounds(node, null);
        }
        return r2d;
    }
    public Filter getBackground(GraphicsNode gn,
                                GraphicsNode child,
                                Rectangle2D aoi) {
        if (gn == null) {
            throw new IllegalArgumentException
                ("BackgroundImage requested yet no parent has " +
                 "'enable-background:new'");
        }
        Rectangle2D r2d = null;
        if (gn instanceof CompositeGraphicsNode) {
            CompositeGraphicsNode cgn = (CompositeGraphicsNode)gn;
            r2d = cgn.getBackgroundEnable();
        }
        List srcs = new ArrayList();      
        if (r2d == null) {
            Rectangle2D paoi = aoi;
            AffineTransform at = gn.getTransform();
            if (at != null)
                paoi = at.createTransformedShape(aoi).getBounds2D();
            Filter f = getBackground(gn.getParent(), gn, paoi);
            if ((f != null) && f.getBounds2D().intersects(aoi)) {
                srcs.add(f);
            }
        }
        if (child != null) {
            CompositeGraphicsNode cgn = (CompositeGraphicsNode)gn;
            List children = cgn.getChildren();
            Iterator i = children.iterator();
            while (i.hasNext()) {
                GraphicsNode childGN = (GraphicsNode)i.next();
                if (childGN == child)
                    break;
                Rectangle2D cbounds = childGN.getBounds();
                if (cbounds == null) continue;
                AffineTransform at = childGN.getTransform();
                if (at != null)
                    cbounds = at.createTransformedShape(cbounds).getBounds2D();
                if (aoi.intersects(cbounds)) {
                    srcs.add(childGN.getEnableBackgroundGraphicsNodeRable
                             (true));
                }
            }
        }
        if (srcs.size() == 0)
            return null;
        Filter ret = null;
        if (srcs.size() == 1)
            ret = (Filter)srcs.get(0);
        else
            ret = new CompositeRable8Bit(srcs, CompositeRule.OVER, false);
        if (child != null) {
            AffineTransform at = child.getTransform();
            if (at != null) {
                try {
                    at = at.createInverse();
                    ret = new AffineRable8Bit(ret, at);
                } catch (NoninvertibleTransformException nte) {
                    ret = null;
                }
            }
        }
        return ret;
    }
    public boolean isDynamic(){
        return false;
    }
    public RenderedImage createRendering(RenderContext renderContext){
        Rectangle2D r2d = getBounds2D();
        Shape aoi = renderContext.getAreaOfInterest();
        if (aoi != null) {
            Rectangle2D aoiR2d = aoi.getBounds2D();
            if ( ! r2d.intersects(aoiR2d) )
                return null;
            Rectangle2D.intersect(r2d, aoiR2d, r2d);
        }
        Filter background = getBackground(node, null, r2d);
        if ( background == null)
            return null;
        background = new PadRable8Bit(background, r2d, PadMode.ZERO_PAD);
        RenderedImage ri = background.createRendering
            (new RenderContext(renderContext.getTransform(), r2d,
                               renderContext.getRenderingHints()));
        return ri;
    }
}
