package org.apache.batik.apps.svgbrowser;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.Overlay;
import org.w3c.dom.Element;
public class ElementOverlayManager {
    protected Color elementOverlayStrokeColor = Color.black;
    protected Color elementOverlayColor = Color.white;
    protected boolean xorMode = true;
    protected JSVGCanvas canvas;
    protected Overlay elementOverlay = new ElementOverlay();
    protected ArrayList elements;
    protected ElementOverlayController controller;
    protected boolean isOverlayEnabled = true;
    public ElementOverlayManager(JSVGCanvas canvas) {
        this.canvas = canvas;
        elements = new ArrayList();
        canvas.getOverlays().add(elementOverlay);
    }
    public void addElement(Element elem) {
        elements.add(elem);
    }
    public void removeElement(Element elem) {
        if (elements.remove(elem)) {
        }
    }
    public void removeElements() {
        elements.clear();
        repaint();
    }
    protected Rectangle getAllElementsBounds() {
        Rectangle resultBound = null;
        int n = elements.size();
        for (int i = 0; i < n; i++) {
            Element currentElement = (Element) elements.get(i);
            Rectangle currentBound = getElementBounds(currentElement);
            if (resultBound == null) {
                resultBound = currentBound;
            } else {
                resultBound.add(currentBound);
            }
        }
        return resultBound;
    }
    protected Rectangle getElementBounds(Element elem) {
        return getElementBounds(canvas.getUpdateManager().getBridgeContext()
                .getGraphicsNode(elem));
    }
    protected Rectangle getElementBounds(GraphicsNode node) {
        if (node == null) {
            return null;
        }
        AffineTransform at = canvas.getRenderingTransform();
        Shape s = at.createTransformedShape(node.getOutline());
        return outset(s.getBounds(), 1);
    }
    protected Rectangle outset(Rectangle r, int amount) {
        r.x -= amount;
        r.y -= amount;
        r.width += 2 * amount;
        r.height += 2 * amount;
        return r;
    }
    public void repaint() {
        canvas.repaint();
    }
    public class ElementOverlay implements Overlay {
        public void paint(Graphics g) {
            if (controller.isOverlayEnabled() && isOverlayEnabled()) {
                int n = elements.size();
                for (int i = 0; i < n; i++) {
                    Element currentElement = (Element) elements.get(i);
                    GraphicsNode nodeToPaint = canvas.getUpdateManager()
                            .getBridgeContext().getGraphicsNode(currentElement);
                    if (nodeToPaint != null) {
                        AffineTransform elementsAt =
                            nodeToPaint.getGlobalTransform();
                        Shape selectionHighlight = nodeToPaint.getOutline();
                        AffineTransform at = canvas.getRenderingTransform();
                        at.concatenate(elementsAt);
                        Shape s = at.createTransformedShape(selectionHighlight);
                        if (s == null) {
                            break;
                        }
                        Graphics2D g2d = (Graphics2D) g;
                        if (xorMode) {
                            g2d.setColor(Color.black);
                            g2d.setXORMode(Color.yellow);
                            g2d.fill(s);
                            g2d.draw(s);
                        } else {
                            g2d.setColor(elementOverlayColor);
                            g2d.setStroke(new BasicStroke(1.8f));
                            g2d.setColor(elementOverlayStrokeColor);
                            g2d.draw(s);
                        }
                    }
                }
            }
        }
    }
    public Color getElementOverlayColor() {
        return elementOverlayColor;
    }
    public void setElementOverlayColor(Color selectionOverlayColor) {
        this.elementOverlayColor = selectionOverlayColor;
    }
    public Color getElementOverlayStrokeColor() {
        return elementOverlayStrokeColor;
    }
    public void setElementOverlayStrokeColor
            (Color selectionOverlayStrokeColor) {
        this.elementOverlayStrokeColor = selectionOverlayStrokeColor;
    }
    public boolean isXorMode() {
        return xorMode;
    }
    public void setXorMode(boolean xorMode) {
        this.xorMode = xorMode;
    }
    public Overlay getElementOverlay() {
        return elementOverlay;
    }
    public void removeOverlay() {
        canvas.getOverlays().remove(elementOverlay);
    }
    public void setController(ElementOverlayController controller) {
        this.controller = controller;
    }
    public boolean isOverlayEnabled() {
        return isOverlayEnabled;
    }
    public void setOverlayEnabled(boolean isOverlayEnabled) {
        this.isOverlayEnabled = isOverlayEnabled;
    }
}
