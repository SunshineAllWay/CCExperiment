package org.apache.batik.gvt.event;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import org.apache.batik.gvt.GraphicsNode;
public class GraphicsNodeMouseEvent extends GraphicsNodeInputEvent {
    static final int MOUSE_FIRST = 500;
    public static final int MOUSE_CLICKED = MOUSE_FIRST;
    public static final int MOUSE_PRESSED = MOUSE_FIRST + 1;
    public static final int MOUSE_RELEASED = MOUSE_FIRST + 2;
    public static final int MOUSE_MOVED = MOUSE_FIRST + 3;
    public static final int MOUSE_ENTERED = MOUSE_FIRST + 4;
    public static final int MOUSE_EXITED = MOUSE_FIRST + 5;
    public static final int MOUSE_DRAGGED = MOUSE_FIRST + 6;
    float x;
    float y;
    int clientX;
    int clientY;
    int screenX;
    int screenY;
    int clickCount;
    int button;
    GraphicsNode relatedNode = null;
    public GraphicsNodeMouseEvent(GraphicsNode source, int id,
                                  long when, int modifiers, int lockState,
                                  int button, float x, float y, 
                                  int clientX, int clientY,
                                  int screenX, int screenY, 
                                  int clickCount,
                                  GraphicsNode relatedNode) {
        super(source, id, when, modifiers, lockState);
        this.button = button;
        this.x = x;
        this.y = y;
        this.clientX = clientX;
        this.clientY = clientY;
        this.screenX = screenX;
        this.screenY = screenY;
        this.clickCount = clickCount;
        this.relatedNode = relatedNode;
    }
    public GraphicsNodeMouseEvent(GraphicsNode source,
                                  MouseEvent evt,
                                  int button,
                                  int lockState) {
        super(source, evt, lockState);
        this.button = button;
        this.x = evt.getX();
        this.y = evt.getY();
        this.clickCount = evt.getClickCount();
    }
    public int getButton() {
        return button;
    }
    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
    public float getClientX() {
        return clientX;
    }
    public float getClientY() {
        return clientY;
    }
    public int getScreenX() {
        return screenX;
    }
    public int getScreenY() {
        return screenY;
    }
    public Point getScreenPoint() {
        return new Point(screenX, screenY);
    }
    public Point getClientPoint() {
        return new Point(clientX, clientY);
    }
    public Point2D getPoint2D() {
        return new Point2D.Float(x, y);
    }
    public int getClickCount() {
        return clickCount;
    }
    public GraphicsNode getRelatedNode() {
        return relatedNode;
    }
}
