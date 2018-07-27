package org.apache.batik.swing.gvt;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
public class AbstractRotateInteractor extends InteractorAdapter {
    protected boolean finished;
    protected double initialRotation;
    public boolean endInteraction() {
        return finished;
    }
    public void mousePressed(MouseEvent e) {
        finished = false;
        JGVTComponent c = (JGVTComponent)e.getSource();
        Dimension d = c.getSize();
        double dx = e.getX() - d.width / 2;
        double dy = e.getY() - d.height / 2;
        double cos = -dy / Math.sqrt(dx * dx + dy * dy);
        initialRotation = (dx > 0) ? Math.acos(cos) : -Math.acos(cos);
    }
    public void mouseReleased(MouseEvent e) {
        finished = true;
        JGVTComponent c = (JGVTComponent)e.getSource();
        AffineTransform at = rotateTransform(c.getSize(), e.getX(), e.getY());
        at.concatenate(c.getRenderingTransform());
        c.setRenderingTransform(at);
    }
    public void mouseExited(MouseEvent e) {
        finished = true;
        JGVTComponent c = (JGVTComponent)e.getSource();
        c.setPaintingTransform(null);
    }
    public void mouseDragged(MouseEvent e) {
        JGVTComponent c = (JGVTComponent)e.getSource();
        c.setPaintingTransform(rotateTransform(c.getSize(), e.getX(), e.getY()));
    }
    protected AffineTransform rotateTransform(Dimension d, int x, int y) {
        double dx = x - d.width / 2;
        double dy = y - d.height / 2;
        double cos = -dy / Math.sqrt(dx * dx + dy * dy);
        double angle = (dx > 0) ? Math.acos(cos) : -Math.acos(cos);
        angle -= initialRotation;
        return AffineTransform.getRotateInstance(angle, d.width / 2, d.height / 2);
    }
}
