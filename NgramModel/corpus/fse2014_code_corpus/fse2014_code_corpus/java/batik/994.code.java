package org.apache.batik.gvt.filter;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import java.util.Map;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.SVGComposite;
import org.apache.batik.ext.awt.image.renderable.AbstractRable;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.PaintRable;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.TranslateRed;
import org.apache.batik.gvt.GraphicsNode;
public class GraphicsNodeRable8Bit 
    extends    AbstractRable 
    implements GraphicsNodeRable, PaintRable {
    private AffineTransform cachedGn2dev   = null;
    private AffineTransform cachedUsr2dev  = null;
    private CachableRed     cachedRed      = null;
    private Rectangle2D     cachedBounds = null;
    private boolean usePrimitivePaint = true;
    public boolean getUsePrimitivePaint() {
        return usePrimitivePaint;
    }
    public void setUsePrimitivePaint(boolean usePrimitivePaint) {
        this.usePrimitivePaint = usePrimitivePaint;
    }
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
    public void clearCache() {
        cachedRed     = null;
        cachedUsr2dev = null;
        cachedGn2dev  = null;
        cachedBounds  = null;
    }
    public GraphicsNodeRable8Bit(GraphicsNode node){
        if(node == null)
            throw new IllegalArgumentException();
        this.node = node;
        this.usePrimitivePaint = true;
    }
    public GraphicsNodeRable8Bit(GraphicsNode node,
                                 Map props){
        super((Filter)null, props);
        if(node == null)
            throw new IllegalArgumentException();
        this.node = node;
        this.usePrimitivePaint = true;
    }
    public GraphicsNodeRable8Bit(GraphicsNode node, 
                                 boolean      usePrimitivePaint){
        if(node == null)
            throw new IllegalArgumentException();
        this.node = node;
        this.usePrimitivePaint = usePrimitivePaint;
    }
    public Rectangle2D getBounds2D(){
        if (usePrimitivePaint){
            Rectangle2D primitiveBounds = node.getPrimitiveBounds();
            if(primitiveBounds == null)
                return new Rectangle2D.Double(0, 0, 0, 0);
            return (Rectangle2D)(primitiveBounds.clone());
        }
        Rectangle2D bounds = node.getBounds();
        if(bounds == null){
            return new Rectangle2D.Double(0, 0, 0, 0);
        }
        AffineTransform at = node.getTransform();
        if (at != null){
           bounds = at.createTransformedShape(bounds).getBounds2D();
        }
        return bounds;
    }
    public boolean isDynamic(){
        return false;
    }
    public boolean paintRable(Graphics2D g2d) {
        Composite c = g2d.getComposite();
        if (!SVGComposite.OVER.equals(c))
            return false;
        ColorSpace g2dCS = GraphicsUtil.getDestinationColorSpace(g2d);
        if ((g2dCS == null) ||
            (g2dCS != ColorSpace.getInstance(ColorSpace.CS_sRGB))){
            return false;
        }
        GraphicsNode gn = getGraphicsNode();
        if (getUsePrimitivePaint()){
            gn.primitivePaint(g2d);
        }
        else{
            gn.paint(g2d);
        }
        return true;
    }
    public RenderedImage createRendering(RenderContext renderContext){
        AffineTransform usr2dev = renderContext.getTransform();
        AffineTransform gn2dev;
        if (usr2dev == null) {
            usr2dev = new AffineTransform();
            gn2dev  = usr2dev;
        } else {
            gn2dev = (AffineTransform)usr2dev.clone();
        }
        AffineTransform gn2usr = node.getTransform();
        if (gn2usr != null) {
            gn2dev.concatenate(gn2usr);
        }
        Rectangle2D bounds2D = getBounds2D();
        if ((cachedBounds != null)                            &&
            (cachedGn2dev != null)                            &&
            (cachedBounds.equals(bounds2D))                   &&
            (gn2dev.getScaleX()  == cachedGn2dev.getScaleX()) &&
            (gn2dev.getScaleY()  == cachedGn2dev.getScaleY()) &&
            (gn2dev.getShearX()  == cachedGn2dev.getShearX()) &&
            (gn2dev.getShearY()  == cachedGn2dev.getShearY()))
        {
            double deltaX = (usr2dev.getTranslateX() - 
                             cachedUsr2dev.getTranslateX());
            double deltaY = (usr2dev.getTranslateY() - 
                             cachedUsr2dev.getTranslateY());
            if ((deltaX ==0) && (deltaY == 0))
                return cachedRed;
            if ((deltaX == (int)deltaX) &&
                (deltaY == (int)deltaY)) {
                return new TranslateRed
                    (cachedRed, 
                     (int)Math.round(cachedRed.getMinX()+deltaX),
                     (int)Math.round(cachedRed.getMinY()+deltaY));
            }
        }
        if (false) {
            System.out.println("Not using Cached Red: " + usr2dev);
            System.out.println("Old:                  " + cachedUsr2dev);
        }
        if((bounds2D.getWidth()  > 0) && 
           (bounds2D.getHeight() > 0)) {
            cachedUsr2dev = (AffineTransform)usr2dev.clone();
            cachedGn2dev  = gn2dev;
            cachedBounds  = bounds2D;
            cachedRed =  new GraphicsNodeRed8Bit
                (node, usr2dev, usePrimitivePaint, 
                 renderContext.getRenderingHints());
            return cachedRed;
        }
        cachedUsr2dev = null;
        cachedGn2dev  = null;
        cachedBounds  = null;
        cachedRed     = null;
        return null;
    }
}
