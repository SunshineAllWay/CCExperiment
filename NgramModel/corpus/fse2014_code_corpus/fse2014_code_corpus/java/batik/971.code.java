package org.apache.batik.gvt.event;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.lang.reflect.Array;
import java.util.EventListener;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.EventListenerList;
import org.apache.batik.gvt.GraphicsNode;
public class AWTEventDispatcher
        implements EventDispatcher,
                   MouseListener,
                   MouseMotionListener,
                   MouseWheelListener,
                   KeyListener {
    protected GraphicsNode root;
    protected AffineTransform baseTransform;
    protected EventListenerList glisteners;
    protected GraphicsNode lastHit;
    protected GraphicsNode currentKeyEventTarget;
    protected List    eventQueue = new LinkedList();
    protected boolean eventDispatchEnabled = true;
    protected int     eventQueueMaxSize = MAX_QUEUE_SIZE;
    static final int MAX_QUEUE_SIZE = 10;
    private int nodeIncrementEventID = KeyEvent.KEY_PRESSED;
    private int nodeIncrementEventCode = KeyEvent.VK_TAB;
    private int nodeIncrementEventModifiers = 0;
    private int nodeDecrementEventID = KeyEvent.KEY_PRESSED;
    private int nodeDecrementEventCode = KeyEvent.VK_TAB;
    private int nodeDecrementEventModifiers = InputEvent.SHIFT_MASK;
    public AWTEventDispatcher() {
    }
    public void setRootNode(GraphicsNode root) {
        if (this.root != root)
            eventQueue.clear(); 
        this.root = root;
    }
    public GraphicsNode getRootNode() {
        return root;
    }
    public void setBaseTransform(AffineTransform t) {
        if ((baseTransform != t) &&
            ((baseTransform == null) || (!baseTransform.equals(t))))
            eventQueue.clear();
        baseTransform = t;
    }
    public AffineTransform getBaseTransform() {
        return new AffineTransform(baseTransform);
    }
    public void mousePressed(MouseEvent evt) {
        dispatchEvent(evt);
    }
    public void mouseReleased(MouseEvent evt) {
        dispatchEvent(evt);
    }
    public void mouseEntered(MouseEvent evt) {
        dispatchEvent(evt);
    }
    public void mouseExited(MouseEvent evt) {
        dispatchEvent(evt);
    }
    public void mouseClicked(MouseEvent evt) {
        dispatchEvent(evt);
    }
    public void mouseMoved(MouseEvent evt) {
        dispatchEvent(evt);
    }
    public void mouseDragged(MouseEvent evt) {
        dispatchEvent(evt);
    }
    public void mouseWheelMoved(MouseWheelEvent evt) {
        dispatchEvent(evt);
    }
    public void keyPressed(KeyEvent evt) {
        dispatchEvent(evt);
    }
    public void keyReleased(KeyEvent evt) {
        dispatchEvent(evt);
    }
    public void keyTyped(KeyEvent evt) {
        dispatchEvent(evt);
    }
    public void addGraphicsNodeMouseListener(GraphicsNodeMouseListener l) {
        if (glisteners == null) {
            glisteners = new EventListenerList();
        }
        glisteners.add(GraphicsNodeMouseListener.class, l);
    }
    public void removeGraphicsNodeMouseListener(GraphicsNodeMouseListener l) {
        if (glisteners != null) {
            glisteners.remove(GraphicsNodeMouseListener.class, l);
        }
    }
    public void addGraphicsNodeMouseWheelListener
            (GraphicsNodeMouseWheelListener l) {
        if (glisteners == null) {
            glisteners = new EventListenerList();
        }
        glisteners.add(GraphicsNodeMouseWheelListener.class, l);
    }
    public void removeGraphicsNodeMouseWheelListener
            (GraphicsNodeMouseWheelListener l) {
        if (glisteners != null) {
            glisteners.remove(GraphicsNodeMouseWheelListener.class, l);
        }
    }
    public void addGraphicsNodeKeyListener(GraphicsNodeKeyListener l) {
        if (glisteners == null) {
            glisteners = new EventListenerList();
        }
        glisteners.add(GraphicsNodeKeyListener.class, l);
    }
    public void removeGraphicsNodeKeyListener(GraphicsNodeKeyListener l) {
        if (glisteners != null) {
            glisteners.remove(GraphicsNodeKeyListener.class, l);
        }
    }
    public EventListener [] getListeners(Class listenerType) {
        Object array =
            Array.newInstance(listenerType,
                              glisteners.getListenerCount(listenerType));
        Object[] pairElements = glisteners.getListenerList();
        for (int i = 0, j = 0;i < pairElements.length-1; i+=2) {
            if (pairElements[i].equals(listenerType)) {
                Array.set(array, j, pairElements[i+1]);
                ++j;
            }
        }
        return (EventListener[]) array;
    }
    public void setEventDispatchEnabled(boolean b) {
        eventDispatchEnabled = b;
        if (eventDispatchEnabled) {
            while (eventQueue.size() > 0) {
                EventObject evt =  (EventObject)eventQueue.remove(0);
                dispatchEvent(evt);
            }
        }
    }
    public void setEventQueueMaxSize(int n) {
        eventQueueMaxSize = n;
        if (n == 0) eventQueue.clear();
        while(eventQueue.size() > eventQueueMaxSize)
            eventQueue.remove(0);
    }
    public void dispatchEvent(EventObject evt) {
        if (root == null) 
            return;
        if (!eventDispatchEnabled) {
            if (eventQueueMaxSize > 0) {
                eventQueue.add(evt);
                while (eventQueue.size() > eventQueueMaxSize)
                    eventQueue.remove(0);
            }
            return;
        }
        if (evt instanceof MouseWheelEvent) {
            dispatchMouseWheelEvent((MouseWheelEvent) evt);
        } else if (evt instanceof MouseEvent) {
            dispatchMouseEvent((MouseEvent) evt);
        } else if (evt instanceof KeyEvent) {
            InputEvent e = (InputEvent)evt;
            if (isNodeIncrementEvent(e)) {
                incrementKeyTarget();
            } else if (isNodeDecrementEvent(e)) {
                decrementKeyTarget();
            } else {
                dispatchKeyEvent((KeyEvent) evt);
            }
        }
    }
    protected int getCurrentLockState() {
        Toolkit t = Toolkit.getDefaultToolkit();
        int lockState = 0;
        try {
            if (t.getLockingKeyState(KeyEvent.VK_KANA_LOCK)) {
                lockState++;
            }
        } catch (UnsupportedOperationException ex) {
        }
        lockState <<= 1;
        try {
            if (t.getLockingKeyState(KeyEvent.VK_SCROLL_LOCK)) {
                lockState++;
            }
        } catch (UnsupportedOperationException ex) {
        }
        lockState <<= 1;
        try {
            if (t.getLockingKeyState(KeyEvent.VK_NUM_LOCK)) {
                lockState++;
            }
        } catch (UnsupportedOperationException ex) {
        }
        lockState <<= 1;
        try {
            if (t.getLockingKeyState(KeyEvent.VK_CAPS_LOCK)) {
                lockState++;
            }
        } catch (UnsupportedOperationException ex) {
        }
        return lockState;
    }
    protected void dispatchKeyEvent(KeyEvent evt) {
        currentKeyEventTarget = lastHit;
        GraphicsNode target =
            currentKeyEventTarget == null ? root : currentKeyEventTarget;
        processKeyEvent
            (new GraphicsNodeKeyEvent(target,
                                      evt.getID(),
                                      evt.getWhen(),
                                      evt.getModifiersEx(),
                                      getCurrentLockState(),
                                      evt.getKeyCode(),
                                      evt.getKeyChar(),
                                      evt.getKeyLocation()));
    }
    protected void dispatchMouseEvent(MouseEvent evt) {
        GraphicsNodeMouseEvent gvtevt;
        Point2D p = new Point2D.Float(evt.getX(), evt.getY());
        Point2D gnp = p;
        if (baseTransform != null) {
            gnp = baseTransform.transform(p, null);
        }
        GraphicsNode node = root.nodeHitAt(gnp);
        if (node != null) {
            try {
                node.getGlobalTransform().createInverse().transform(gnp, gnp);
            } catch (NoninvertibleTransformException ex) {
            }
        }
        Point screenPos;
        if (!evt.getComponent().isShowing()) {
            screenPos = new Point(0,0);
        } else {
            screenPos = evt.getComponent().getLocationOnScreen();
            screenPos.x += evt.getX();
            screenPos.y += evt.getY();
        }
        int currentLockState = getCurrentLockState();
        if (lastHit != node) {
            if (lastHit != null) {
                gvtevt = new GraphicsNodeMouseEvent(lastHit,
                                                    MouseEvent.
                                                    MOUSE_EXITED,
                                                    evt.getWhen(),
                                                    evt.getModifiersEx(),
                                                    currentLockState,
                                                    evt.getButton(),
                                                    (float)gnp.getX(),
                                                    (float)gnp.getY(),
                                                    (int)Math.floor(p.getX()),  
                                                    (int)Math.floor(p.getY()),  
                                                    screenPos.x,
                                                    screenPos.y,
                                                    evt.getClickCount(),
                                                    node);
                processMouseEvent(gvtevt);
            }
            if (node != null) {
                gvtevt = new GraphicsNodeMouseEvent(node,
                                                    MouseEvent.
                                                    MOUSE_ENTERED,
                                                    evt.getWhen(),
                                                    evt.getModifiersEx(),
                                                    currentLockState,
                                                    evt.getButton(),
                                                    (float)gnp.getX(),
                                                    (float)gnp.getY(),
                                                    (int)Math.floor(p.getX()),
                                                    (int)Math.floor(p.getY()),
                                                    screenPos.x,
                                                    screenPos.y,
                                                    evt.getClickCount(),
                                                    lastHit);
                processMouseEvent(gvtevt);
            }
        }
        if (node != null) {
            gvtevt = new GraphicsNodeMouseEvent(node,
                                                evt.getID(),
                                                evt.getWhen(),
                                                evt.getModifiersEx(),
                                                currentLockState,
                                                evt.getButton(),
                                                (float)gnp.getX(),
                                                (float)gnp.getY(),
                                                (int)Math.floor(p.getX()),
                                                (int)Math.floor(p.getY()),
                                                screenPos.x,
                                                screenPos.y,
                                                evt.getClickCount(),
                                                null);
            processMouseEvent(gvtevt);
        } else {
            gvtevt = new GraphicsNodeMouseEvent(root,
                                                evt.getID(),
                                                evt.getWhen(),
                                                evt.getModifiersEx(),
                                                currentLockState,
                                                evt.getButton(),
                                                (float)gnp.getX(),
                                                (float)gnp.getY(),
                                                (int)Math.floor(p.getX()),
                                                (int)Math.floor(p.getY()),
                                                screenPos.x,
                                                screenPos.y,
                                                evt.getClickCount(),
                                                null);
            processMouseEvent(gvtevt);
        }
        lastHit = node;
    }
    protected void dispatchMouseWheelEvent(MouseWheelEvent evt) {
        if (lastHit != null) {
            processMouseWheelEvent
                (new GraphicsNodeMouseWheelEvent(lastHit,
                                                 evt.getID(),
                                                 evt.getWhen(),
                                                 evt.getModifiersEx(),
                                                 getCurrentLockState(),
                                                 evt.getWheelRotation()));
        }
    }
    protected void processMouseEvent(GraphicsNodeMouseEvent evt) {
        if (glisteners != null) {
            GraphicsNodeMouseListener[] listeners =
                (GraphicsNodeMouseListener[])
                getListeners(GraphicsNodeMouseListener.class);
            switch (evt.getID()) {
            case GraphicsNodeMouseEvent.MOUSE_MOVED:
                for (int i = 0; i < listeners.length; i++) {
                    listeners[i].mouseMoved(evt);
                }
                break;
            case GraphicsNodeMouseEvent.MOUSE_DRAGGED:
                for (int i = 0; i < listeners.length; i++) {
                    listeners[i].mouseDragged(evt);
                }
                break;
            case GraphicsNodeMouseEvent.MOUSE_ENTERED:
                for (int i = 0; i < listeners.length; i++) {
                    listeners[i].mouseEntered(evt);
                }
                break;
            case GraphicsNodeMouseEvent.MOUSE_EXITED:
                for (int i = 0; i < listeners.length; i++) {
                    listeners[i].mouseExited(evt);
                }
                break;
            case GraphicsNodeMouseEvent.MOUSE_CLICKED:
                for (int i = 0; i < listeners.length; i++) {
                    listeners[i].mouseClicked(evt);
                }
                break;
            case GraphicsNodeMouseEvent.MOUSE_PRESSED:
                for (int i = 0; i < listeners.length; i++) {
                    listeners[i].mousePressed(evt);
                }
                break;
            case GraphicsNodeMouseEvent.MOUSE_RELEASED:
                for (int i = 0; i < listeners.length; i++) {
                    listeners[i].mouseReleased(evt);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown Mouse Event type: "+evt.getID());
            }
        }
    }
    protected void processMouseWheelEvent(GraphicsNodeMouseWheelEvent evt) {
        if (glisteners != null) {
            GraphicsNodeMouseWheelListener[] listeners =
                (GraphicsNodeMouseWheelListener[])
                getListeners(GraphicsNodeMouseWheelListener.class);
            for (int i = 0; i < listeners.length; i++) {
                listeners[i].mouseWheelMoved(evt);
            }
        }
    }
    public void processKeyEvent(GraphicsNodeKeyEvent evt) {
        if ((glisteners != null)) {
            GraphicsNodeKeyListener[] listeners =
                (GraphicsNodeKeyListener[])
                getListeners(GraphicsNodeKeyListener.class);
            switch (evt.getID()) {
            case GraphicsNodeKeyEvent.KEY_PRESSED:
                for (int i=0; i<listeners.length; ++i) {
                    listeners[i].keyPressed(evt);
                }
                break;
            case GraphicsNodeKeyEvent.KEY_RELEASED:
                for (int i=0; i<listeners.length; ++i) {
                    listeners[i].keyReleased(evt);
                }
                break;
            case GraphicsNodeKeyEvent.KEY_TYPED:
                for (int i=0; i<listeners.length; ++i) {
                    listeners[i].keyTyped(evt);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown Key Event type: "+evt.getID());
            }
        }
        evt.consume();
    }
    private void incrementKeyTarget() {
        throw new UnsupportedOperationException("Increment not implemented.");
    }
    private void decrementKeyTarget() {
        throw new UnsupportedOperationException("Decrement not implemented.");
    }
    public void setNodeIncrementEvent(InputEvent e) {
        nodeIncrementEventID = e.getID();
        if (e instanceof KeyEvent) {
            nodeIncrementEventCode = ((KeyEvent) e).getKeyCode();
        }
        nodeIncrementEventModifiers = e.getModifiers();
    }
    public void setNodeDecrementEvent(InputEvent e) {
        nodeDecrementEventID = e.getID();
        if (e instanceof KeyEvent) {
            nodeDecrementEventCode = ((KeyEvent) e).getKeyCode();
        }
        nodeDecrementEventModifiers = e.getModifiers();
    }
    protected boolean isNodeIncrementEvent(InputEvent e) {
        if (e.getID() != nodeIncrementEventID) {
            return false;
        }
        if (e instanceof KeyEvent) {
            if (((KeyEvent) e).getKeyCode() != nodeIncrementEventCode) {
                return false;
            }
        }
        if ((e.getModifiers() & nodeIncrementEventModifiers) == 0) {
            return false;
        }
        return true;
    }
    protected boolean isNodeDecrementEvent(InputEvent e) {
        if (e.getID() != nodeDecrementEventID) {
            return false;
        }
        if (e instanceof KeyEvent) {
            if (((KeyEvent) e).getKeyCode() != nodeDecrementEventCode) {
                return false;
            }
        }
        if ((e.getModifiers() & nodeDecrementEventModifiers) == 0) {
            return false;
        }
        return true;
    }
    protected static boolean isMetaDown(int modifiers) {
        return (modifiers & GraphicsNodeInputEvent.META_MASK) != 0;
    }
}
