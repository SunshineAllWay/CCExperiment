package org.apache.batik.gvt.filter;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.rendered.AbstractRed;
import org.apache.batik.ext.awt.image.rendered.AbstractTiledRed;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.Platform;
public class GraphicsNodeRed8Bit extends AbstractRed {
    private GraphicsNode node;
    private AffineTransform node2dev;
    private RenderingHints  hints;
    private boolean usePrimitivePaint;
    public GraphicsNodeRed8Bit(GraphicsNode node,
                               AffineTransform node2dev,
                               boolean usePrimitivePaint,
                               RenderingHints  hints) {
        super(); 
        this.node              = node;
        this.node2dev          = node2dev;
        this.hints             = hints;
        this.usePrimitivePaint = usePrimitivePaint;
        AffineTransform at = node2dev;
        Rectangle2D bounds2D = node.getPrimitiveBounds();
        if (bounds2D == null) bounds2D = new Rectangle2D.Float(0,0,1,1);
        if (!usePrimitivePaint) {
            AffineTransform nodeAt = node.getTransform();
            if (nodeAt != null) {
                at = (AffineTransform)at.clone();
                at.concatenate(nodeAt);
            }
        }
        Rectangle   bounds = at.createTransformedShape(bounds2D).getBounds();
        ColorModel cm = createColorModel();
        int defSz = AbstractTiledRed.getDefaultTileSize();
        int tgX = defSz*(int)Math.floor(bounds.x/defSz);
        int tgY = defSz*(int)Math.floor(bounds.y/defSz);
        int tw  = (bounds.x+bounds.width)-tgX;
        if (tw > defSz) tw = defSz;
        int th  = (bounds.y+bounds.height)-tgY;
        if (th > defSz) th = defSz;
        if ((tw <= 0) || (th <= 0)) {
            tw = 1;
            th = 1;
        }
        SampleModel sm = cm.createCompatibleSampleModel(tw, th);
        init((CachableRed)null, bounds, cm, sm, tgX, tgY, null);
    }
    public WritableRaster copyData(WritableRaster wr) {
        genRect(wr);
        return wr;
    }
    public void genRect(WritableRaster wr) {
        BufferedImage offScreen
            = new BufferedImage(cm, 
                                wr.createWritableTranslatedChild(0,0),
                                cm.isAlphaPremultiplied(),
                                null);
        Graphics2D g = GraphicsUtil.createGraphics(offScreen, hints);
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, wr.getWidth(), wr.getHeight());
        g.setComposite(AlphaComposite.SrcOver);
        g.translate(-wr.getMinX(), -wr.getMinY());
        g.transform(node2dev);
        if (usePrimitivePaint) {
            node.primitivePaint(g);
        }
        else {
            node.paint (g);
        }
        g.dispose();
    }
    public ColorModel createColorModel() {
        if (Platform.isOSX)
            return GraphicsUtil.sRGB_Pre;
        return GraphicsUtil.sRGB_Unpre;
    }
}
