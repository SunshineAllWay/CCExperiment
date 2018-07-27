package org.apache.batik.gvt.svg12;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.lang.ref.SoftReference;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.gvt.AbstractGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Element;
public class MultiResGraphicsNode
    extends AbstractGraphicsNode implements SVGConstants {
    SoftReference [] srcs;
    Element       [] srcElems;
    Dimension     [] minSz;
    Dimension     [] maxSz;
    Rectangle2D      bounds;
    BridgeContext  ctx;
    Element multiImgElem;
    public MultiResGraphicsNode(Element multiImgElem,
                                Rectangle2D  bounds,
                                Element   [] srcElems,
                                Dimension [] minSz,
                                Dimension [] maxSz,
                                BridgeContext ctx) {
        this.multiImgElem = multiImgElem;
        this.srcElems     = new Element  [srcElems.length];
        this.minSz        = new Dimension[srcElems.length];
        this.maxSz        = new Dimension[srcElems.length];
        this.ctx          = ctx;
        for (int i=0; i<srcElems.length; i++) {
            this.srcElems[i] = srcElems[i];
            this.minSz[i]    = minSz[i];
            this.maxSz[i]    = maxSz[i];
        }
        this.srcs = new SoftReference[srcElems.length];
        this.bounds = bounds;
    }
    public void primitivePaint(Graphics2D g2d) {
        AffineTransform at = g2d.getTransform();
        double scx = Math.sqrt(at.getShearY()*at.getShearY()+
                               at.getScaleX()*at.getScaleX());
        double scy = Math.sqrt(at.getShearX()*at.getShearX()+
                               at.getScaleY()*at.getScaleY());
        GraphicsNode gn = null;
        int idx =-1;
        double w = bounds.getWidth()*scx;
        double minDist = calcDist(w, minSz[0], maxSz[0]);
        int    minIdx = 0;
        for (int i=0; i<minSz.length; i++) {
            double dist = calcDist(w, minSz[i], maxSz[i]);
            if (dist < minDist) {
                minDist = dist;
                minIdx = i;
            } 
            if (((minSz[i] == null) || (w >= minSz[i].width)) &&
                ((maxSz[i] == null) || (w <= maxSz[i].width))) {
                if ((idx == -1) || (minIdx == i)) {
                    idx = i;
                }
            }
        }
        if (idx == -1)
            idx = minIdx;
        gn = getGraphicsNode(idx);
        if (gn == null) return;
        Rectangle2D gnBounds = gn.getBounds();
        if (gnBounds == null) return;
        double gnDevW = gnBounds.getWidth()*scx;
        double gnDevH = gnBounds.getHeight()*scy;
        double gnDevX = gnBounds.getX()*scx;
        double gnDevY = gnBounds.getY()*scy;
        double gnDevX0, gnDevX1, gnDevY0, gnDevY1;
        if (gnDevW < 0) {
            gnDevX0 = gnDevX+gnDevW;
            gnDevX1 = gnDevX;
        } else {
            gnDevX0 = gnDevX;
            gnDevX1 = gnDevX+gnDevW;
        }
        if (gnDevH < 0) {
            gnDevY0 = gnDevY+gnDevH;
            gnDevY1 = gnDevY;
        } else {
            gnDevY0 = gnDevY;
            gnDevY1 = gnDevY+gnDevH;
        }
        gnDevW = (int)(Math.ceil(gnDevX1)-Math.floor(gnDevX0));
        gnDevH = (int)(Math.ceil(gnDevY1)-Math.floor(gnDevY0));
        scx = (gnDevW/gnBounds.getWidth())/scx;
        scy = (gnDevH/gnBounds.getHeight())/scy;
        AffineTransform nat = g2d.getTransform();
        nat = new AffineTransform(nat.getScaleX()*scx, nat.getShearY()*scx,
                                 nat.getShearX()*scy, nat.getScaleY()*scy,
                                 nat.getTranslateX(), nat.getTranslateY());
        g2d.setTransform(nat);
        gn.paint(g2d);
    }
    public double calcDist(double loc, Dimension min, Dimension max) {
        if (min == null) {
            if (max == null) 
                return 10E10; 
            else
                return Math.abs(loc-max.width);
        } else {
            if (max == null) 
                return Math.abs(loc-min.width);
            else {
                double mid = (max.width+min.width)/2.0;
                return Math.abs(loc-mid);
            }
        }
    }
    public Rectangle2D getPrimitiveBounds() {
        return bounds;
    }
    public Rectangle2D getGeometryBounds(){
        return bounds;
    }
    public Rectangle2D getSensitiveBounds(){
        return bounds;
    }
    public Shape getOutline() {
        return bounds;
    }
    public GraphicsNode getGraphicsNode(int idx) {
        if (srcs[idx] != null) {
            Object o = srcs[idx].get();
            if (o != null) 
                return (GraphicsNode)o;
        }
        try {
            GVTBuilder builder = ctx.getGVTBuilder();
            GraphicsNode gn;
            gn = builder.build(ctx, srcElems[idx]);
            srcs[idx] = new SoftReference(gn);
            return gn;
        } catch (Exception ex) { ex.printStackTrace();  }
        return null;
    }
}    
