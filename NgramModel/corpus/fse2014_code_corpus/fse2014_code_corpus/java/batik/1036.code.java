package org.apache.batik.gvt.renderer;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Iterator;
import org.apache.batik.ext.awt.geom.RectListManager;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.HaltingThread;
public class MacRenderer implements ImageRenderer {
    static final int COPY_OVERHEAD      = 1000;
    static final int COPY_LINE_OVERHEAD = 10;
    static final AffineTransform IDENTITY = new AffineTransform();
    protected RenderingHints renderingHints;
    protected AffineTransform usr2dev;
    protected GraphicsNode rootGN;
    protected int offScreenWidth;
    protected int offScreenHeight;
    protected boolean isDoubleBuffered;
    protected BufferedImage currImg;
    protected BufferedImage workImg;
    protected RectListManager damagedAreas;
    public static int IMAGE_TYPE = BufferedImage.TYPE_INT_ARGB_PRE;
    public static Color TRANSPARENT_WHITE = new Color(255, 255, 255, 0);
    protected static RenderingHints defaultRenderingHints;
    static {
        defaultRenderingHints = new RenderingHints(null);
        defaultRenderingHints.put(RenderingHints.KEY_ANTIALIASING,
                                  RenderingHints.VALUE_ANTIALIAS_ON);
        defaultRenderingHints.put(RenderingHints.KEY_INTERPOLATION,
                                  RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }
    public MacRenderer() {
        renderingHints = new RenderingHints(null);
        renderingHints.add(defaultRenderingHints);
        usr2dev = new AffineTransform();
    }
    public MacRenderer(RenderingHints rh,
                       AffineTransform at){
        renderingHints = new RenderingHints(null);
        renderingHints.add(rh);
        if (at == null) usr2dev = new AffineTransform();
        else            usr2dev = new AffineTransform(at);
    }
    public void dispose() {
        rootGN  = null;
        currImg = null;
        workImg = null;
        renderingHints = null;
        usr2dev = null;
        if ( damagedAreas != null ){
            damagedAreas.clear();
        }
        damagedAreas = null;
    }
    public void setTree(GraphicsNode treeRoot) {
        rootGN = treeRoot;
    }
    public GraphicsNode getTree() {
        return rootGN;
    }
    public void setTransform(AffineTransform usr2dev) {
        if(usr2dev == null)
            this.usr2dev = new AffineTransform();
        else
            this.usr2dev = new AffineTransform(usr2dev);
        if (workImg == null) return;
        synchronized (workImg) {
            Graphics2D g2d = workImg.createGraphics();
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, workImg.getWidth(), workImg.getHeight());
            g2d.dispose();
        }
        damagedAreas = null;
    }
    public AffineTransform getTransform() {
        return usr2dev;
    }
    public void setRenderingHints(RenderingHints rh) {
        this.renderingHints = new RenderingHints(null);
        this.renderingHints.add(rh);
        damagedAreas = null;
    }
    public RenderingHints getRenderingHints() {
        return renderingHints;
    }
    public boolean isDoubleBuffered(){
        return isDoubleBuffered;
    }
    public void setDoubleBuffered(boolean isDoubleBuffered){
        if (this.isDoubleBuffered == isDoubleBuffered)
            return;
        this.isDoubleBuffered = isDoubleBuffered;
        if (isDoubleBuffered) {
            workImg = null;  
        } else {
            workImg = currImg;
            damagedAreas = null;
        }
    }
    public void updateOffScreen(int width, int height) {
        offScreenWidth  = width;
        offScreenHeight = height;
    }
    public BufferedImage getOffScreen() {
        if (rootGN == null)
            return null;
        return currImg;
    }
    public void clearOffScreen() {
        if (isDoubleBuffered)
            return;
        updateWorkingBuffers();
        if (workImg == null) return;
        synchronized (workImg) {
            Graphics2D g2d = workImg.createGraphics();
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, workImg.getWidth(), workImg.getHeight());
            g2d.dispose();
        }
        damagedAreas = null;
    }
    public void flush() {
    }
    public void flush(Rectangle r) {
    }
    public void flush(Collection areas) {
    }
    protected void updateWorkingBuffers() {
        if (rootGN == null) {
            currImg = null;
            workImg = null;
            return;
        }
        int         w  = offScreenWidth;
        int         h  = offScreenHeight;
        if ((workImg == null)         ||
            (workImg.getWidth()  < w) ||
            (workImg.getHeight() < h)) {
            workImg = new BufferedImage(w, h, IMAGE_TYPE);
        }
        if (!isDoubleBuffered) {
            currImg = workImg;
        }
    }
    public void repaint(Shape area) {
        if (area == null) return;
        RectListManager rlm = new RectListManager();
        rlm.add(usr2dev.createTransformedShape(area).getBounds());
        repaint(rlm);
    }
    public void repaint(RectListManager devRLM) {
        if (devRLM == null)
            return;
        updateWorkingBuffers();
        if ((rootGN == null) || (workImg == null))
            return;
        try {
        synchronized (workImg) {
            Graphics2D g2d = GraphicsUtil.createGraphics
                (workImg, renderingHints);
            Rectangle dr;
            dr = new Rectangle(0, 0, offScreenWidth, offScreenHeight);
            if ((isDoubleBuffered) &&
                (currImg != null) &&
                (damagedAreas  != null)) {
                damagedAreas.subtract(devRLM, COPY_OVERHEAD,
                                      COPY_LINE_OVERHEAD);
                damagedAreas.mergeRects(COPY_OVERHEAD,
                                        COPY_LINE_OVERHEAD);
                Iterator iter = damagedAreas.iterator();
                g2d.setComposite(AlphaComposite.Src);
                while (iter.hasNext()) {
                    Rectangle r = (Rectangle)iter.next();
                    if (!dr.intersects(r)) continue;
                    r = dr.intersection(r);
                    g2d.setClip     (r.x, r.y, r.width, r.height);
                    g2d.setComposite(AlphaComposite.Clear);
                    g2d.fillRect    (r.x, r.y, r.width, r.height);
                    g2d.setComposite(AlphaComposite.SrcOver);
                    g2d.drawImage   (currImg, 0, 0, null);
                }
            }
            Iterator iter = devRLM.iterator();
            while (iter.hasNext()) {
                Rectangle r = (Rectangle)iter.next();
                if (!dr.intersects(r)) continue;
                r = dr.intersection(r);
                g2d.setTransform(IDENTITY);
                g2d.setClip(r.x, r.y, r.width, r.height);
                g2d.setComposite(AlphaComposite.Clear);
                g2d.fillRect(r.x, r.y, r.width, r.height);
                g2d.setComposite(AlphaComposite.SrcOver);
                g2d.transform(usr2dev);
                rootGN.paint(g2d);
            }
            g2d.dispose();
        }
        } catch (Throwable t) { t.printStackTrace(); }
        if (HaltingThread.hasBeenHalted())
            return;
        if (isDoubleBuffered) {
            BufferedImage tmpImg = workImg;
            workImg = currImg;
            currImg = tmpImg;
            damagedAreas = devRLM;
        }
    }
}
