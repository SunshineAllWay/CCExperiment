package org.apache.batik.swing.gvt;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
public class AbstractImageZoomInteractor extends InteractorAdapter {
    protected boolean finished = true;
    protected int xStart;
    protected int yStart;
    protected int xCurrent;
    protected int yCurrent;
    public boolean endInteraction() {
        return finished;
    }
    public void mousePressed(MouseEvent e) {
        if (!finished) {
            JGVTComponent c = (JGVTComponent)e.getSource();
            c.setPaintingTransform(null);
            return;
        }
        finished = false;
        xStart = e.getX();
        yStart = e.getY();
    }
    public void mouseReleased(MouseEvent e) {
        finished = true;
        JGVTComponent c = (JGVTComponent)e.getSource();
        AffineTransform pt = c.getPaintingTransform();
        if (pt != null) {
            AffineTransform rt = (AffineTransform)c.getRenderingTransform().clone();
            rt.preConcatenate(pt);
            c.setRenderingTransform(rt);
        }
    }
    public void mouseDragged(MouseEvent e) {
        AffineTransform at;
        JGVTComponent c = (JGVTComponent)e.getSource();
        xCurrent = e.getX();
        yCurrent = e.getY();
        at = AffineTransform.getTranslateInstance(xStart, yStart);
        int dy = yCurrent - yStart;
        double s;
        if (dy < 0) {
            dy -= 10;
            s = (dy > -15) ? 1.0 : -15.0/dy;
        } else {
            dy += 10;
            s = (dy <  15) ? 1.0 : dy/15.0;
        }
        at.scale(s, s);
        at.translate(-xStart, -yStart);
        c.setPaintingTransform(at);
    }
}
