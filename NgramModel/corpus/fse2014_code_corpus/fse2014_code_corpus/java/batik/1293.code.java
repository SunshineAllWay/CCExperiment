package org.apache.batik.swing.gvt;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
public class AbstractZoomInteractor extends InteractorAdapter {
    protected boolean finished = true;
    protected int xStart;
    protected int yStart;
    protected int xCurrent;
    protected int yCurrent;
    protected Line2D markerTop;
    protected Line2D markerLeft;
    protected Line2D markerBottom;
    protected Line2D markerRight;
    protected Overlay overlay = new ZoomOverlay();
    protected BasicStroke markerStroke = new BasicStroke(1,
                                                         BasicStroke.CAP_SQUARE,
                                                         BasicStroke.JOIN_MITER,
                                                         10,
                                                         new float[] { 4, 4 }, 0);
    public boolean endInteraction() {
        return finished;
    }
    public void mousePressed(MouseEvent e) {
        if (!finished) {
            mouseExited(e);
            return;
        }
        finished = false;
        markerTop = null;
        markerLeft = null;
        markerBottom = null;
        markerRight = null;
        xStart = e.getX();
        yStart = e.getY();
        JGVTComponent c = (JGVTComponent)e.getSource();
        c.getOverlays().add(overlay);
    }
    public void mouseReleased(MouseEvent e) {
        finished = true;
        JGVTComponent c = (JGVTComponent)e.getSource();
        c.getOverlays().remove(overlay);
        overlay.paint(c.getGraphics());
        xCurrent = e.getX();
        yCurrent = e.getY();
        if ((xCurrent - xStart) != 0 &&
            (yCurrent - yStart) != 0) {
            int dx = xCurrent - xStart;
            int dy = yCurrent - yStart;
            if (dx < 0) {
                dx = -dx;
                xStart = xCurrent;
            }
            if (dy < 0) {
                dy = -dy;
                yStart = yCurrent;
            }
            Dimension size = c.getSize();
            float scaleX = size.width / (float)dx;
            float scaleY = size.height / (float)dy;
            float scale = (scaleX < scaleY) ? scaleX : scaleY;
            AffineTransform at = new AffineTransform();
            at.scale(scale, scale);
            at.translate(-xStart, -yStart);
            at.concatenate(c.getRenderingTransform());
            c.setRenderingTransform(at);
        }
    }
    public void mouseExited(MouseEvent e) {
        finished = true;
        JGVTComponent c = (JGVTComponent)e.getSource();
        c.getOverlays().remove(overlay);
        overlay.paint(c.getGraphics());
    }
    public void mouseDragged(MouseEvent e) {
        JGVTComponent c = (JGVTComponent)e.getSource();
        overlay.paint(c.getGraphics());
        xCurrent = e.getX();
        yCurrent = e.getY();
        float xMin, yMin, width, height;
        if (xStart < xCurrent) {
            xMin = xStart;
            width = xCurrent - xStart;
        } else {
            xMin = xCurrent;
            width = xStart - xCurrent;
        }
        if (yStart < yCurrent) {
            yMin = yStart;
            height = yCurrent - yStart;
        } else {
            yMin = yCurrent;
            height = yStart - yCurrent;
        }
        Dimension d = c.getSize();
        float compAR = d.width/(float)d.height;
        if (compAR > width/height) {
            width = compAR*height;
        } else {
            height = width/compAR;
        }
        markerTop    = new Line2D.Float(xMin, yMin, xMin+width,  yMin);
        markerLeft   = new Line2D.Float(xMin, yMin, xMin, yMin+height);
        markerBottom = new Line2D.Float(xMin, yMin+height,  
                                        xMin+width,  yMin+height);
        markerRight  = new Line2D.Float(xMin+width,  yMin,  
                                        xMin+width,  yMin+height);
        overlay.paint(c.getGraphics());
    }
    protected class ZoomOverlay implements Overlay {
        public void paint(Graphics g) {
            if (markerTop != null) {
                Graphics2D g2d = (Graphics2D)g;
                g2d.setXORMode(Color.white);
                g2d.setColor(Color.black);
                g2d.setStroke(markerStroke);
                g2d.draw(markerTop);
                g2d.draw(markerLeft);
                g2d.draw(markerBottom);
                g2d.draw(markerRight);
            }
        }
    }
}
