package org.apache.batik.gvt.event;
import java.awt.event.InputEvent;
import java.awt.geom.AffineTransform;
import java.util.EventListener;
import java.util.EventObject;
import org.apache.batik.gvt.GraphicsNode;
public interface EventDispatcher {
    void setRootNode(GraphicsNode root);
    GraphicsNode getRootNode();
    void setBaseTransform(AffineTransform t);
    AffineTransform getBaseTransform();
    void dispatchEvent(EventObject e);
    void addGraphicsNodeMouseListener(GraphicsNodeMouseListener l);
    void removeGraphicsNodeMouseListener(GraphicsNodeMouseListener l);
    void addGraphicsNodeMouseWheelListener(GraphicsNodeMouseWheelListener l);
    void removeGraphicsNodeMouseWheelListener(GraphicsNodeMouseWheelListener l);
    void addGraphicsNodeKeyListener(GraphicsNodeKeyListener l);
    void removeGraphicsNodeKeyListener(GraphicsNodeKeyListener l);
    EventListener [] getListeners(Class listenerType);
    void setNodeIncrementEvent(InputEvent e);
    void setNodeDecrementEvent(InputEvent e);
}
