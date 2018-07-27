package org.apache.batik.gvt;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.PadRable8Bit;
public class PatternPaint implements Paint {
    private GraphicsNode node;
    private Rectangle2D patternRegion;
    private AffineTransform patternTransform;
    private Filter tile;
    private boolean overflow;
    private PatternPaintContext lastContext;
    public PatternPaint(GraphicsNode node,
                        Rectangle2D patternRegion,
                        boolean overflow,
                        AffineTransform patternTransform){
        if (node == null) {
            throw new IllegalArgumentException();
        }
        if (patternRegion == null) {
            throw new IllegalArgumentException();
        }
        this.node             = node;
        this.patternRegion    = patternRegion;
        this.overflow         = overflow;
        this.patternTransform = patternTransform;
        CompositeGraphicsNode comp = new CompositeGraphicsNode();
        comp.getChildren().add(node);
        Filter gnr = comp.getGraphicsNodeRable(true);
        Rectangle2D padBounds = (Rectangle2D)patternRegion.clone();
        if (overflow) {
            Rectangle2D nodeBounds = comp.getBounds();
            padBounds.add(nodeBounds);
        }
        tile = new PadRable8Bit(gnr, padBounds, PadMode.ZERO_PAD);
    }
    public GraphicsNode getGraphicsNode(){
        return node;
    }
    public Rectangle2D getPatternRect(){
        return (Rectangle2D)patternRegion.clone();
    }
    public AffineTransform getPatternTransform(){
        return patternTransform;
    }
    public boolean getOverflow() {
        return overflow;
    }
    public PaintContext createContext(ColorModel      cm,
                                      Rectangle       deviceBounds,
                                      Rectangle2D     userBounds,
                                      AffineTransform xform,
                                      RenderingHints  hints) {
        if (patternTransform != null) {
            xform = new AffineTransform(xform);
            xform.concatenate(patternTransform);
        }
        if ((lastContext!= null) &&
            lastContext.getColorModel().equals(cm)) {
            double[] p = new double[6];
            double[] q = new double[6];
            xform.getMatrix(p);
            lastContext.getUsr2Dev().getMatrix(q);
            if ((p[0] == q[0]) && (p[1] == q[1]) &&
                (p[2] == q[2]) && (p[3] == q[3])) {
                if ((p[4] == q[4]) && (p[5] == q[5]))
                    return lastContext;
                else
                    return new PatternPaintContextWrapper
                        (lastContext,
                         (int)(q[4]-p[4]+0.5),
                         (int)(q[5]-p[5]+0.5));
            }
        }
        lastContext = new PatternPaintContext(cm, xform,
                                       hints, tile,
                                       patternRegion,
                                       overflow);
        return lastContext;
    }
    public int getTransparency(){
        return TRANSLUCENT;
    }
    static class PatternPaintContextWrapper implements PaintContext {
        PatternPaintContext ppc;
        int xShift, yShift;
        PatternPaintContextWrapper(PatternPaintContext ppc,
                            int xShift, int yShift) {
            this.ppc = ppc;
            this.xShift = xShift;
            this.yShift = yShift;
        }
        public void dispose(){ }
        public ColorModel getColorModel(){
            return ppc.getColorModel();
        }
        public Raster getRaster(int x, int y, int width, int height){
            return ppc.getRaster(x+xShift, y+yShift, width, height);
        }
    }
}
