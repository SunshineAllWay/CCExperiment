package org.apache.batik.swing.gvt;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.BasicStroke;
import java.awt.geom.AffineTransform;
import org.apache.batik.gvt.Selectable;
import org.apache.batik.gvt.event.EventDispatcher;
import org.apache.batik.gvt.event.GraphicsNodeMouseEvent;
import org.apache.batik.gvt.event.GraphicsNodeMouseListener;
import org.apache.batik.gvt.event.SelectionEvent;
import org.apache.batik.gvt.event.SelectionListener;
import org.apache.batik.gvt.text.ConcreteTextSelector;
import org.apache.batik.gvt.text.Mark;
public class TextSelectionManager {
    public static final Cursor TEXT_CURSOR = new Cursor(Cursor.TEXT_CURSOR);
    protected ConcreteTextSelector textSelector;
    protected JGVTComponent component;
    protected Overlay selectionOverlay = new SelectionOverlay();
    protected MouseListener mouseListener;
    protected Cursor previousCursor;
    protected Shape selectionHighlight;
    protected SelectionListener textSelectionListener;
    protected Color selectionOverlayColor = new Color(100, 100, 255, 100);
    protected Color selectionOverlayStrokeColor = Color.white;
    protected boolean xorMode = false;
    Object selection = null;
    public TextSelectionManager(JGVTComponent comp,
                                EventDispatcher ed) {
        textSelector = new ConcreteTextSelector();
        textSelectionListener = new TextSelectionListener();
        textSelector.addSelectionListener(textSelectionListener);
        mouseListener = new MouseListener();
        component = comp;
        component.getOverlays().add(selectionOverlay);
        ed.addGraphicsNodeMouseListener(mouseListener);
    }
    public void addSelectionListener(SelectionListener sl) {
        textSelector.addSelectionListener(sl);
    }
    public void removeSelectionListener(SelectionListener sl) {
        textSelector.removeSelectionListener(sl);
    }
    public void setSelectionOverlayColor(Color color) {
        selectionOverlayColor = color;
    }
    public Color getSelectionOverlayColor() {
        return selectionOverlayColor;
    }
    public void setSelectionOverlayStrokeColor(Color color) {
        selectionOverlayStrokeColor = color;
    }
    public Color getSelectionOverlayStrokeColor() {
        return selectionOverlayStrokeColor;
    }
    public void setSelectionOverlayXORMode(boolean state) {
        xorMode = state;
    }
    public boolean isSelectionOverlayXORMode() {
        return xorMode;
    }
    public Overlay getSelectionOverlay() {
        return selectionOverlay;
    }
    public Object getSelection() {
        return selection;
    }
    public void setSelection(Mark start, Mark end) {
        textSelector.setSelection(start, end);
    }
    public void clearSelection() {
        textSelector.clearSelection();
    }
    protected class MouseListener implements GraphicsNodeMouseListener {
        public void mouseClicked(GraphicsNodeMouseEvent evt) {
            if (evt.getSource() instanceof Selectable) {
                textSelector.mouseClicked(evt);
            }
        }
        public void mousePressed(GraphicsNodeMouseEvent evt) {
            if (evt.getSource() instanceof Selectable) {
                textSelector.mousePressed(evt);
            } else if (selectionHighlight != null) {
                textSelector.clearSelection();
            }
        }
        public void mouseReleased(GraphicsNodeMouseEvent evt) {
            textSelector.mouseReleased(evt);
        }
        public void mouseEntered(GraphicsNodeMouseEvent evt) {
            if (evt.getSource() instanceof Selectable) {
                textSelector.mouseEntered(evt);
                previousCursor = component.getCursor();
                if (previousCursor.getType() == Cursor.DEFAULT_CURSOR) {
                    component.setCursor(TEXT_CURSOR);
                }
            }
        }
        public void mouseExited(GraphicsNodeMouseEvent evt) {
            if (evt.getSource() instanceof Selectable) {
                textSelector.mouseExited(evt);
                if (component.getCursor() == TEXT_CURSOR) {
                    component.setCursor(previousCursor);
                }
            }
        }
        public void mouseDragged(GraphicsNodeMouseEvent evt) {
            if (evt.getSource() instanceof Selectable) {
                textSelector.mouseDragged(evt);
            }
        }
        public void mouseMoved(GraphicsNodeMouseEvent evt) { }
    }
    protected class TextSelectionListener implements SelectionListener {
        public void selectionDone(SelectionEvent e) {
            selectionChanged(e);
            selection = e.getSelection();
        }
        public void selectionCleared(SelectionEvent e) {
            selectionStarted(e);
        }
        public void selectionStarted(SelectionEvent e) {
            if (selectionHighlight != null) {
                Rectangle r = getHighlightBounds();
                selectionHighlight = null;
                component.repaint(r);
            }
            selection = null;
        }
        public void selectionChanged(SelectionEvent e) {
            Rectangle r = null;
            AffineTransform at = component.getRenderingTransform();
            if (selectionHighlight != null) {
                r = at.createTransformedShape(selectionHighlight).getBounds();
                outset(r, 1);
            }
            selectionHighlight = e.getHighlightShape();
            if (selectionHighlight != null) {
                if (r != null) {
                    Rectangle r2 = getHighlightBounds();
                    r2.add( r );   
                    component.repaint( r2 );
                } else {
                    component.repaint(getHighlightBounds());
                }
            } else if (r != null) {
                component.repaint(r);
            }
        }
    }
    protected Rectangle outset(Rectangle r, int amount) {
        r.x -= amount;
        r.y -= amount;
        r.width  += 2*amount;
        r.height += 2*amount;
        return r;
    }
    protected Rectangle getHighlightBounds() {
        AffineTransform at = component.getRenderingTransform();
        Shape s = at.createTransformedShape(selectionHighlight);
        return outset(s.getBounds(), 1);
    }
    protected class SelectionOverlay implements Overlay {
        public void paint(Graphics g) {
            if (selectionHighlight != null) {
                AffineTransform at = component.getRenderingTransform();
                Shape s = at.createTransformedShape(selectionHighlight);
                Graphics2D g2d = (Graphics2D)g;
                if (xorMode) {
                    g2d.setColor(Color.black);
                    g2d.setXORMode(Color.white);
                    g2d.fill(s);
                } else {
                    g2d.setColor(selectionOverlayColor);
                    g2d.fill(s);
                    if (selectionOverlayStrokeColor != null) {
                        g2d.setStroke(new BasicStroke(1.0f));
                        g2d.setColor(selectionOverlayStrokeColor);
                        g2d.draw(s);
                    }
                }
            }
        }
    }
}
