package org.apache.batik.bridge;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Collection;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.ext.awt.geom.RectListManager;
public class RepaintManager {
    static final int COPY_OVERHEAD      = 10000;
    static final int COPY_LINE_OVERHEAD = 10;
    protected ImageRenderer renderer;
    public RepaintManager(ImageRenderer r) {
        renderer = r;
    }
    public Collection updateRendering(Collection areas)
        throws InterruptedException {
        renderer.flush(areas);
        List rects = new ArrayList(areas.size());
        AffineTransform at = renderer.getTransform();
        Iterator i = areas.iterator();
        while (i.hasNext()) {
            Shape s = (Shape)i.next();
            s = at.createTransformedShape(s);
            Rectangle2D r2d = s.getBounds2D();
            int x0 = (int)Math.floor(r2d.getX());
            int y0 = (int)Math.floor(r2d.getY());
            int x1 = (int)Math.ceil(r2d.getX()+r2d.getWidth());
            int y1 = (int)Math.ceil(r2d.getY()+r2d.getHeight());
            Rectangle r = new Rectangle(x0-1, y0-1, x1-x0+3, y1-y0+3);
            rects.add(r);
        }
        RectListManager devRLM = null;
        try {
            devRLM = new RectListManager(rects);
            devRLM.mergeRects(COPY_OVERHEAD, COPY_LINE_OVERHEAD);
        } catch(Exception e) {
            e.printStackTrace();
        }
        renderer.repaint(devRLM);
        return devRLM;
    }
    public void setupRenderer(AffineTransform u2d,
                              boolean dbr,
                              Shape aoi,
                              int width,
                              int height) {
        renderer.setTransform(u2d);
        renderer.setDoubleBuffered(dbr);
        renderer.updateOffScreen(width, height);
        renderer.clearOffScreen();
    }
    public BufferedImage getOffScreen(){
        return renderer.getOffScreen();
    }
}
