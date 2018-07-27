package org.apache.batik.gvt.renderer;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Collection;
import java.util.Iterator;
import org.apache.batik.ext.awt.geom.RectListManager;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.PadRed;
import org.apache.batik.util.HaltingThread;
public class DynamicRenderer extends StaticRenderer {
    static final int COPY_OVERHEAD      = 1000;
    static final int COPY_LINE_OVERHEAD = 10;
    public DynamicRenderer() {
        super();
    }
    public DynamicRenderer(RenderingHints rh,
                           AffineTransform at){
        super(rh, at);
    }
    RectListManager damagedAreas;
    protected CachableRed setupCache(CachableRed img) {
        return img;
    }
    public void flush(Rectangle r) {
        return;
    }
    public void flush(Collection areas) {
        return;
    }
    protected void updateWorkingBuffers() {
        if (rootFilter == null) {
            rootFilter = rootGN.getGraphicsNodeRable(true);
            rootCR = null;
        }
        rootCR = renderGNR();
        if (rootCR == null) {
            workingRaster = null;
            workingOffScreen = null;
            workingBaseRaster = null;
            currentOffScreen = null;
            currentBaseRaster = null;
            currentRaster = null;
            return;
        }
        SampleModel sm = rootCR.getSampleModel();
        int         w  = offScreenWidth;
        int         h  = offScreenHeight;
        if ((workingBaseRaster == null) ||
            (workingBaseRaster.getWidth()  < w) ||
            (workingBaseRaster.getHeight() < h)) {
            sm = sm.createCompatibleSampleModel(w, h);
            workingBaseRaster
                = Raster.createWritableRaster(sm, new Point(0,0));
            workingRaster = workingBaseRaster.createWritableChild
                (0, 0, w, h, 0, 0, null);
            workingOffScreen =  new BufferedImage
                (rootCR.getColorModel(),
                 workingRaster,
                 rootCR.getColorModel().isAlphaPremultiplied(), null);
        }
        if (!isDoubleBuffered) {
            currentOffScreen  = workingOffScreen;
            currentBaseRaster = workingBaseRaster;
            currentRaster     = workingRaster;
        }
    }
    public void repaint(RectListManager devRLM) {
        if (devRLM == null)
            return;
        CachableRed cr;
        WritableRaster syncRaster;
        WritableRaster copyRaster;
        updateWorkingBuffers();
        if ((rootCR == null)           ||
            (workingBaseRaster == null)) {
            return;
        }
        cr = rootCR;
        syncRaster = workingBaseRaster;
        copyRaster = workingRaster;
        Rectangle srcR = rootCR.getBounds();
        Rectangle dstR = workingRaster.getBounds();
        if ((dstR.x < srcR.x) ||
            (dstR.y < srcR.y) ||
            (dstR.x+dstR.width  > srcR.x+srcR.width) ||
            (dstR.y+dstR.height > srcR.y+srcR.height))
            cr = new PadRed(cr, dstR, PadMode.ZERO_PAD, null);
        boolean repaintAll = false;
        Rectangle dr = copyRaster.getBounds();
        Rectangle sr = null;
        if (currentRaster != null) {
            sr = currentRaster.getBounds();
        }
        synchronized (syncRaster) {
            if (repaintAll) {
                cr.copyData(copyRaster);
            } else {
                java.awt.Graphics2D g2d = null;
                if (false) {
                    BufferedImage tmpBI = new BufferedImage
                        (workingOffScreen.getColorModel(),
                         copyRaster.createWritableTranslatedChild(0, 0),
                         workingOffScreen.isAlphaPremultiplied(), null);
                    g2d = GraphicsUtil.createGraphics(tmpBI);
                    g2d.translate(-copyRaster.getMinX(),
                                  -copyRaster.getMinY());
                }
                if ((isDoubleBuffered) &&
                    (currentRaster != null) &&
                    (damagedAreas  != null)) {
                    damagedAreas.subtract(devRLM, COPY_OVERHEAD,
                                          COPY_LINE_OVERHEAD);
                    damagedAreas.mergeRects(COPY_OVERHEAD,
                                            COPY_LINE_OVERHEAD);
                    Color fillColor   = new Color( 0, 0, 255, 50 );
                    Color borderColor = new Color( 0, 0,   0, 50 );
                    Iterator iter = damagedAreas.iterator();
                    while (iter.hasNext()) {
                        Rectangle r = (Rectangle)iter.next();
                        if (!dr.intersects(r)) continue;
                        r = dr.intersection(r);
                        if (sr != null && !sr.intersects(r)) continue;
                        r = sr.intersection(r);
                        Raster src = currentRaster.createWritableChild
                            (r.x, r.y, r.width, r.height, r.x, r.y, null);
                        GraphicsUtil.copyData(src, copyRaster);
                        if (g2d != null) {
                            g2d.setPaint( fillColor );
                            g2d.fill(r);
                            g2d.setPaint( borderColor );
                            g2d.draw(r);
                        }
                    }
                }
                Color fillColor   = new Color( 255, 0, 0, 50 );
                Color borderColor = new Color(   0, 0, 0, 50 );
                Iterator iter = devRLM.iterator();
                while (iter.hasNext()) {
                    Rectangle r = (Rectangle)iter.next();
                    if (!dr.intersects(r)) continue;
                    r = dr.intersection(r);
                    WritableRaster dst = copyRaster.createWritableChild
                        (r.x, r.y, r.width, r.height, r.x, r.y, null);
                    cr.copyData(dst);
                    if (g2d != null) {
                        g2d.setPaint( fillColor );
                        g2d.fill(r);
                        g2d.setPaint( borderColor );
                        g2d.draw(r);
                    }
                }
            }
        }
        if (HaltingThread.hasBeenHalted()) {
            return;
        }
        BufferedImage tmpBI = workingOffScreen;
        workingBaseRaster = currentBaseRaster;
        workingRaster     = currentRaster;
        workingOffScreen  = currentOffScreen;
        currentRaster     = copyRaster;
        currentBaseRaster = syncRaster;
        currentOffScreen  = tmpBI;
        damagedAreas = devRLM;
    }
}
