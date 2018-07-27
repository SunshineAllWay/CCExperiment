package org.apache.batik.swing.gvt;
import java.awt.Cursor;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
public abstract class AbstractPanInteractor extends InteractorAdapter {
    public static final Cursor PAN_CURSOR = new Cursor(Cursor.MOVE_CURSOR);
    protected boolean finished = true;
    protected int xStart;
    protected int yStart;
    protected int xCurrent;
    protected int yCurrent;
    protected Cursor previousCursor;
    public boolean endInteraction() {
        return finished;
    }
    public void mousePressed(MouseEvent e) {
        if (!finished) {
            mouseExited(e);
            return;
        }
        finished = false;
        xStart = e.getX();
        yStart = e.getY();
        JGVTComponent c = (JGVTComponent)e.getSource();
        previousCursor = c.getCursor();
        c.setCursor(PAN_CURSOR);
    }
    public void mouseReleased(MouseEvent e) {
        if (finished) {
            return;
        }
        finished = true;
        JGVTComponent c = (JGVTComponent)e.getSource();
        xCurrent = e.getX();
        yCurrent = e.getY();
        AffineTransform at =
            AffineTransform.getTranslateInstance(xCurrent - xStart,
                                                 yCurrent - yStart);
        AffineTransform rt =
            (AffineTransform)c.getRenderingTransform().clone();
        rt.preConcatenate(at);
        c.setRenderingTransform(rt);
        if (c.getCursor() == PAN_CURSOR) {
            c.setCursor(previousCursor);
        }
    }
    public void mouseExited(MouseEvent e) {
        finished = true;
        JGVTComponent c = (JGVTComponent)e.getSource();
        c.setPaintingTransform(null);
        if (c.getCursor() == PAN_CURSOR) {
            c.setCursor(previousCursor);
        }
    }
    public void mouseDragged(MouseEvent e) {
        JGVTComponent c = (JGVTComponent)e.getSource();
        xCurrent = e.getX();
        yCurrent = e.getY();
        AffineTransform at =
            AffineTransform.getTranslateInstance(xCurrent - xStart,
                                                 yCurrent - yStart);
        c.setPaintingTransform(at);
    }
}
