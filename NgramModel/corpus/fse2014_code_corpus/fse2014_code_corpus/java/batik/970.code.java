package org.apache.batik.gvt;
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.batik.gvt.event.GraphicsNodeChangeAdapter;
import org.apache.batik.gvt.event.GraphicsNodeChangeEvent;
import org.apache.batik.ext.awt.image.renderable.Filter;
public class UpdateTracker extends GraphicsNodeChangeAdapter {
    Map dirtyNodes = null;
    Map fromBounds = new HashMap();
    protected static Rectangle2D NULL_RECT = new Rectangle();
    public UpdateTracker(){
    }
    public boolean hasChanged() {
        return (dirtyNodes != null);
    }
    public List getDirtyAreas() {
        if (dirtyNodes == null)
            return null;
        List ret = new LinkedList();
        Set keys = dirtyNodes.keySet();
        Iterator i = keys.iterator();
        while (i.hasNext()) {
            WeakReference gnWRef = (WeakReference)i.next();
            GraphicsNode  gn     = (GraphicsNode)gnWRef.get();
            if (gn == null) continue;
            AffineTransform oat;
            oat = (AffineTransform)dirtyNodes.get(gnWRef);
            if (oat != null){
                oat = new AffineTransform(oat);
            }
            Rectangle2D srcORgn = (Rectangle2D)fromBounds.remove(gnWRef);
            Rectangle2D srcNRgn = null;
            AffineTransform nat = null;
            if (!(srcORgn instanceof ChngSrcRect)) {
                srcNRgn = gn.getBounds();
                nat = gn.getTransform();
                if (nat != null)
                    nat = new AffineTransform(nat);
            }
            do {
                gn = gn.getParent();
                if (gn == null)
                    break; 
                Filter f= gn.getFilter();
                if ( f != null) {
                    srcNRgn = f.getBounds2D();
                    nat = null;
                }
                AffineTransform at = gn.getTransform();
                gnWRef = gn.getWeakReference();
                AffineTransform poat = (AffineTransform)dirtyNodes.get(gnWRef);
                if (poat == null) poat = at;
                if (poat != null) {
                    if (oat != null)
                        oat.preConcatenate(poat);
                    else
                        oat = new AffineTransform(poat);
                }
                if (at != null){
                    if (nat != null)
                        nat.preConcatenate(at);
                    else
                        nat = new AffineTransform(at);
                }
            } while (true);
            if (gn == null) {
                Shape oRgn = srcORgn;
                if ((oRgn != null) && (oRgn != NULL_RECT)) {
                    if (oat != null)
                        oRgn = oat.createTransformedShape(srcORgn);
                    ret.add(oRgn);
                }
                if (srcNRgn != null) {
                    Shape nRgn = srcNRgn;
                    if (nat != null)
                        nRgn = nat.createTransformedShape(srcNRgn);
                    if (nRgn != null)
                        ret.add(nRgn);
                }
            }
        }
        fromBounds.clear();
        dirtyNodes.clear();
        return ret;
    }
    public Rectangle2D getNodeDirtyRegion(GraphicsNode gn,
                                          AffineTransform at) {
        WeakReference gnWRef = gn.getWeakReference();
        AffineTransform nat = (AffineTransform)dirtyNodes.get(gnWRef);
        if (nat == null) nat = gn.getTransform();
        if (nat != null) {
            at = new AffineTransform(at);
            at.concatenate(nat);
        }
        Filter f= gn.getFilter();
        Rectangle2D ret = null;
        if (gn instanceof CompositeGraphicsNode) {
            CompositeGraphicsNode cgn = (CompositeGraphicsNode)gn;
            Iterator iter = cgn.iterator();
            while (iter.hasNext()) {
                GraphicsNode childGN = (GraphicsNode)iter.next();
                Rectangle2D r2d = getNodeDirtyRegion(childGN, at);
                if (r2d != null) {
                    if (f != null) {
                        Shape s = at.createTransformedShape(f.getBounds2D());
                        ret = s.getBounds2D();
                        break;
                    }
                    if ((ret == null) || (ret == NULL_RECT)) ret = r2d;
                    else ret.add(r2d);
                }
            }
        } else {
            ret = (Rectangle2D)fromBounds.remove(gnWRef);
            if (ret == null) {
                if (f != null) ret = f.getBounds2D();
                else           ret = gn.getBounds();
            } else if (ret == NULL_RECT)
                ret = null;
            if (ret != null)
                ret = at.createTransformedShape(ret).getBounds2D();
        }
        return ret;
    }
    public Rectangle2D getNodeDirtyRegion(GraphicsNode gn) {
        return getNodeDirtyRegion(gn, new AffineTransform());
    }
    public void changeStarted(GraphicsNodeChangeEvent gnce) {
        GraphicsNode gn = gnce.getGraphicsNode();
        WeakReference gnWRef = gn.getWeakReference();
        boolean doPut = false;
        if (dirtyNodes == null) {
            dirtyNodes = new HashMap();
            doPut = true;
        } else if (!dirtyNodes.containsKey(gnWRef))
            doPut = true;
        if (doPut) {
            AffineTransform at = gn.getTransform();
            if (at != null) at = (AffineTransform)at.clone();
            else            at = new AffineTransform();
            dirtyNodes.put(gnWRef, at);
        }
        GraphicsNode chngSrc = gnce.getChangeSrc();
        Rectangle2D rgn = null;
        if (chngSrc != null) {
            Rectangle2D drgn = getNodeDirtyRegion(chngSrc);
            if (drgn != null)
                rgn = new ChngSrcRect(drgn);
        } else {
            rgn = gn.getBounds();
        }
        Rectangle2D r2d = (Rectangle2D)fromBounds.remove(gnWRef);
        if (rgn != null) {
            if ((r2d != null) && (r2d != NULL_RECT)) {
                r2d.add(rgn);
            }
            else             r2d = rgn;
        }
        if (r2d == null)
            r2d = NULL_RECT;
        fromBounds.put(gnWRef, r2d);
    }
    class ChngSrcRect extends Rectangle2D.Float {
        ChngSrcRect(Rectangle2D r2d) {
            super((float)r2d.getX(), (float)r2d.getY(),
                  (float)r2d.getWidth(), (float)r2d.getHeight());
        }
    }
    public void clear() {
        dirtyNodes = null;
    }
}
