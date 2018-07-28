package org.apache.batik.swing.gvt;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Shape;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import java.awt.image.BufferedImage;
import java.text.CharacterIterator;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComponent;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.event.AWTEventDispatcher;
import org.apache.batik.gvt.event.EventDispatcher;
import org.apache.batik.gvt.event.SelectionAdapter;
import org.apache.batik.gvt.event.SelectionEvent;
import org.apache.batik.gvt.renderer.ConcreteImageRendererFactory;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.gvt.renderer.ImageRendererFactory;
import org.apache.batik.gvt.text.Mark;
import org.apache.batik.util.HaltingThread;
import org.apache.batik.util.Platform;
public class JGVTComponent extends JComponent {
    protected Listener listener;
    protected GVTTreeRenderer gvtTreeRenderer;
    protected GraphicsNode gvtRoot;
    protected ImageRendererFactory rendererFactory =
        new ConcreteImageRendererFactory();
    protected ImageRenderer renderer;
    protected List gvtTreeRendererListeners =
        Collections.synchronizedList(new LinkedList());
    protected boolean needRender;
    protected boolean progressivePaint;
    protected HaltingThread progressivePaintThread;
    protected BufferedImage image;
    protected AffineTransform initialTransform = new AffineTransform();
    protected AffineTransform renderingTransform = new AffineTransform();
    protected AffineTransform paintingTransform;
    protected List interactors = new LinkedList();
    protected Interactor interactor;
    protected List overlays = new LinkedList();
    protected List jgvtListeners = null;
    protected AWTEventDispatcher eventDispatcher;
    protected TextSelectionManager textSelectionManager;
    protected boolean doubleBufferedRendering;
    protected boolean eventsEnabled;
    protected boolean selectableText;
    protected boolean useUnixTextSelection = true;
    protected boolean suspendInteractions;
    protected boolean disableInteractions;
    public JGVTComponent() {
        this(false, false);
    }
    public JGVTComponent(boolean eventsEnabled,
                         boolean selectableText) {
        setBackground(Color.white);
        this.eventsEnabled = eventsEnabled;
        this.selectableText = selectableText;
        listener = createListener();
        addAWTListeners();
        addGVTTreeRendererListener(listener);
        addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    if (updateRenderingTransform())
                        scheduleGVTRendering();
                }
            });
    }
    protected Listener createListener() {
        return new Listener();
    }
    protected void addAWTListeners() {
        addKeyListener(listener);
        addMouseListener(listener);
        addMouseMotionListener(listener);
        addMouseWheelListener(listener);
    }
    public void setDisableInteractions(boolean b) {
        disableInteractions = b;
    }
    public boolean getDisableInteractions() {
        return disableInteractions;
    }
    public void setUseUnixTextSelection(boolean b) {
        useUnixTextSelection = b;
    }
    public void getUseUnixTextSelection(boolean b) {
        useUnixTextSelection = b;
    }
    public List getInteractors() {
        return interactors;
    }
    public List getOverlays() {
        return overlays;
    }
    public BufferedImage getOffScreen() {
        return image;
    }
    public void addJGVTComponentListener(JGVTComponentListener listener) {
        if (jgvtListeners == null)
            jgvtListeners = new LinkedList();
        jgvtListeners.add(listener);
    }
    public void removeJGVTComponentListener(JGVTComponentListener listener) {
        if (jgvtListeners == null) return;
        jgvtListeners.remove(listener);
    }
    public void resetRenderingTransform() {
        setRenderingTransform(initialTransform);
    }
    public void stopProcessing() {
        if (gvtTreeRenderer != null) {
            needRender = false;
            gvtTreeRenderer.halt();
            haltProgressivePaintThread();
        }
    }
    public GraphicsNode getGraphicsNode() {
        return gvtRoot;
    }
    public void setGraphicsNode(GraphicsNode gn) {
        setGraphicsNode(gn, true);
        initialTransform = new AffineTransform();
        updateRenderingTransform();
        setRenderingTransform(initialTransform, true);
    }
    protected void setGraphicsNode(GraphicsNode gn, boolean createDispatcher) {
        gvtRoot = gn;
        if (gn != null && createDispatcher) {
            initializeEventHandling();
        }
        if (eventDispatcher != null) {
            eventDispatcher.setRootNode(gn);
        }
    }
    protected void initializeEventHandling() {
        if (eventsEnabled) {
            eventDispatcher = createEventDispatcher();
            if (selectableText) {
                textSelectionManager = createTextSelectionManager
                    (eventDispatcher);
                textSelectionManager.addSelectionListener
                     (new UnixTextSelectionListener());
            }
        }
    }
    protected AWTEventDispatcher createEventDispatcher() {
        return new AWTEventDispatcher();
    }
    protected TextSelectionManager 
        createTextSelectionManager(EventDispatcher ed) {
        return new TextSelectionManager(this, ed);
    }
    public TextSelectionManager getTextSelectionManager() {
        return textSelectionManager;
    }
    public void setSelectionOverlayColor(Color color) {
        if (textSelectionManager != null) {
            textSelectionManager.setSelectionOverlayColor(color);
        }
    }
    public Color getSelectionOverlayColor() {
        if (textSelectionManager != null) {
            return textSelectionManager.getSelectionOverlayColor();
        } else {
            return null;
        }
    }
    public void setSelectionOverlayStrokeColor(Color color) {
        if (textSelectionManager != null) {
            textSelectionManager.setSelectionOverlayStrokeColor(color);
        }
    }
    public Color getSelectionOverlayStrokeColor() {
        if (textSelectionManager != null) {
            return textSelectionManager.getSelectionOverlayStrokeColor();
        } else {
            return null;
        }
    }
    public void setSelectionOverlayXORMode(boolean state) {
        if (textSelectionManager != null) {
            textSelectionManager.setSelectionOverlayXORMode(state);
        }
    }
    public boolean isSelectionOverlayXORMode() {
        if (textSelectionManager != null) {
            return textSelectionManager.isSelectionOverlayXORMode();
        } else {
            return false;
        }
    }
    public void select(Mark start, Mark end) {
        if (textSelectionManager != null) {
            textSelectionManager.setSelection(start, end);
        }
    }
    public void deselectAll() {
        if (textSelectionManager != null) {
            textSelectionManager.clearSelection();
        }
    }
    public void setProgressivePaint(boolean b) {
        if (progressivePaint != b) {
            progressivePaint = b;
            haltProgressivePaintThread();
        }
    }
    public boolean getProgressivePaint() {
        return progressivePaint;
    }
    public Rectangle getRenderRect() {
        Dimension d = getSize();
        return new Rectangle(0, 0, d.width, d.height);
    }
    public void immediateRepaint() {
        if (EventQueue.isDispatchThread()) {
            Rectangle visRect = getRenderRect();
            if (doubleBufferedRendering)
                repaint(visRect.x,     visRect.y,
                        visRect.width, visRect.height);
            else
                paintImmediately(visRect.x,     visRect.y,
                                 visRect.width, visRect.height);
        } else {
            try {
                EventQueue.invokeAndWait(new Runnable() {
                        public void run() {
                            Rectangle visRect = getRenderRect();
                            if (doubleBufferedRendering)
                                repaint(visRect.x,     visRect.y,
                                        visRect.width, visRect.height);
                            else
                                paintImmediately(visRect.x,    visRect.y,
                                                 visRect.width,visRect.height);
                        }
                    });
            } catch (Exception e) {
            }
        }
    }
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        Rectangle visRect = getRenderRect();
        g2d.setComposite(AlphaComposite.SrcOver);
        g2d.setPaint(getBackground());
        g2d.fillRect(visRect.x,     visRect.y,
                     visRect.width, visRect.height);
        if (image != null) {
            if (paintingTransform != null) {
                g2d.transform(paintingTransform);
            }
            g2d.drawRenderedImage(image, null);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                 RenderingHints.VALUE_ANTIALIAS_OFF);
            Iterator it = overlays.iterator();
            while (it.hasNext()) {
                ((Overlay)it.next()).paint(g);
            }
        }
    }
    public void setPaintingTransform(AffineTransform at) {
        paintingTransform = at;
        immediateRepaint();
    }
    public AffineTransform getPaintingTransform() {
        return paintingTransform;
    }
    public void setRenderingTransform(AffineTransform at) {
        setRenderingTransform(at, true);
    }
    public void setRenderingTransform(AffineTransform at,
                                      boolean performRedraw) {
        renderingTransform = new AffineTransform(at);
        suspendInteractions = true;
        if (eventDispatcher != null) {
            try {
                eventDispatcher.setBaseTransform
                    (renderingTransform.createInverse());
            } catch (NoninvertibleTransformException e) {
                handleException(e);
            }
        }
        if (jgvtListeners != null) {
            Iterator iter = jgvtListeners.iterator();
            ComponentEvent ce = new ComponentEvent
                (this, JGVTComponentListener.COMPONENT_TRANSFORM_CHANGED);
            while (iter.hasNext()) {
                JGVTComponentListener l = (JGVTComponentListener)iter.next();
                l.componentTransformChanged(ce);
            }
        }
        if (performRedraw)
            scheduleGVTRendering();
    }
    public AffineTransform getInitialTransform() {
        return new AffineTransform(initialTransform);
    }
    public AffineTransform getRenderingTransform() {
        return new AffineTransform(renderingTransform);
    }
    public void setDoubleBufferedRendering(boolean b) {
        doubleBufferedRendering = b;
    }
    public boolean getDoubleBufferedRendering() {
        return doubleBufferedRendering;
    }
    public void addGVTTreeRendererListener(GVTTreeRendererListener l) {
        gvtTreeRendererListeners.add(l);
    }
    public void removeGVTTreeRendererListener(GVTTreeRendererListener l) {
        gvtTreeRendererListeners.remove(l);
    }
    public void flush() {
        renderer.flush();
    }
    public void flush(Rectangle r) {
        renderer.flush(r);
    }
    protected ImageRenderer createImageRenderer() {
        return rendererFactory.createStaticImageRenderer();
    }
    protected void renderGVTTree() {
        Rectangle visRect = getRenderRect();
        if (gvtRoot == null || visRect.width <= 0 || visRect.height <= 0) {
            return;
        }
        if (renderer == null || renderer.getTree() != gvtRoot) {
            renderer = createImageRenderer();
            renderer.setTree(gvtRoot);
        }
        AffineTransform inv;
        try {
            inv = renderingTransform.createInverse();
        } catch (NoninvertibleTransformException e) {
            throw new IllegalStateException( "NoninvertibleTransformEx:" + e.getMessage() );
        }
        Shape s = inv.createTransformedShape(visRect);
        gvtTreeRenderer = new GVTTreeRenderer(renderer, renderingTransform,
                                              doubleBufferedRendering, s,
                                              visRect.width, visRect.height);
        gvtTreeRenderer.setPriority(Thread.MIN_PRIORITY);
        Iterator it = gvtTreeRendererListeners.iterator();
        while (it.hasNext()) {
            gvtTreeRenderer.addGVTTreeRendererListener
                ((GVTTreeRendererListener)it.next());
        }
        if (eventDispatcher != null) {
            eventDispatcher.setEventDispatchEnabled(false);
        }
        gvtTreeRenderer.start();
    }
    protected boolean computeRenderingTransform() {
        initialTransform = new AffineTransform();
        if (!initialTransform.equals(renderingTransform)) {
            setRenderingTransform(initialTransform, false);
            return true;
        }
        return false;
    }
    protected boolean updateRenderingTransform() {
        return false;
    }
    protected void handleException(Exception e) {
    }
    protected void releaseRenderingReferences() {
        eventDispatcher = null;
        if (textSelectionManager != null) {
            overlays.remove(textSelectionManager.getSelectionOverlay());
            textSelectionManager = null;
        }
        renderer = null;
        image = null;
        gvtRoot = null;
    }
    protected void scheduleGVTRendering() {
        if (gvtTreeRenderer != null) {
            needRender = true;
            gvtTreeRenderer.halt();
        } else {
            renderGVTTree();
        }
    }
    private void haltProgressivePaintThread() {
        if (progressivePaintThread != null) {
            progressivePaintThread.halt();
            progressivePaintThread = null;
        }
    }
    protected class Listener
        implements GVTTreeRendererListener,
                   KeyListener,
                   MouseListener,
                   MouseMotionListener,
                   MouseWheelListener {
        boolean checkClick = false;
        boolean hadDrag = false;
        int startX, startY;
        long startTime, fakeClickTime;
        int MAX_DISP = 4*4;
        long CLICK_TIME = 200;
        protected Listener() {
        }
        public void gvtRenderingPrepare(GVTTreeRendererEvent e) {
            suspendInteractions = true;
            if (!progressivePaint && !doubleBufferedRendering) {
                image = null;
            }
        }
        public void gvtRenderingStarted(GVTTreeRendererEvent e) {
            if (progressivePaint && !doubleBufferedRendering) {
                image = e.getImage();
                progressivePaintThread = new HaltingThread() {
                    public void run() {
                        final Thread thisThread = this;
                        try {
                            while (!hasBeenHalted()) {
                                EventQueue.invokeLater(new Runnable() {
                                    public void run() {
                                        if (progressivePaintThread ==
                                            thisThread) {
                                            Rectangle vRect = getRenderRect();
                                            repaint(vRect.x,     vRect.y,
                                                    vRect.width, vRect.height);
                                        }
                                    }
                                });
                                sleep(200);
                            }
                        } catch (InterruptedException ie) {
                        } catch (ThreadDeath td) {
                            throw td;
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                };
                progressivePaintThread.setPriority(Thread.MIN_PRIORITY + 1);
                progressivePaintThread.start();
            }
            if (!doubleBufferedRendering) {
                paintingTransform = null;
                suspendInteractions = false;
            }
        }
        public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
            haltProgressivePaintThread();
            if (doubleBufferedRendering) {
                paintingTransform = null;
                suspendInteractions = false;
            }
            gvtTreeRenderer = null;
            if (needRender) {
                renderGVTTree();
                needRender = false;
            } else {
                image = e.getImage();
                immediateRepaint();
            }
            if (eventDispatcher != null) {
                eventDispatcher.setEventDispatchEnabled(true);
            }
        }
        public void gvtRenderingCancelled(GVTTreeRendererEvent e) {
            renderingStopped();
        }
        public void gvtRenderingFailed(GVTTreeRendererEvent e) {
            renderingStopped();
        }
        private void renderingStopped() {
            haltProgressivePaintThread();
            if (doubleBufferedRendering) {
                suspendInteractions = false;
            }
            gvtTreeRenderer = null;
            if (needRender) {
                renderGVTTree();
                needRender = false;
            } else {
                immediateRepaint();
            }
            if (eventDispatcher != null) {
                eventDispatcher.setEventDispatchEnabled(true);
            }
        }
        public void keyTyped(KeyEvent e) {
            selectInteractor(e);
            if (interactor != null) {
                interactor.keyTyped(e);
                deselectInteractor();
            } else if (eventDispatcher != null) {
                dispatchKeyTyped(e);
            }
        }
        protected void dispatchKeyTyped(KeyEvent e) {
            eventDispatcher.keyTyped(e);
        }
        public void keyPressed(KeyEvent e) {
            selectInteractor(e);
            if (interactor != null) {
                interactor.keyPressed(e);
                deselectInteractor();
            } else if (eventDispatcher != null) {
                dispatchKeyPressed(e);
            }
        }
        protected void dispatchKeyPressed(KeyEvent e) {
            eventDispatcher.keyPressed(e);
        }
        public void keyReleased(KeyEvent e) {
            selectInteractor(e);
            if (interactor != null) {
                interactor.keyReleased(e);
                deselectInteractor();
            } else if (eventDispatcher != null) {
                dispatchKeyReleased(e);
            }
        }
        protected void dispatchKeyReleased(KeyEvent e) {
            eventDispatcher.keyReleased(e);
        }
        public void mouseClicked(MouseEvent e) {
            if (fakeClickTime != e.getWhen())
                handleMouseClicked(e);
        }
        public void handleMouseClicked(MouseEvent e) {
            selectInteractor(e);
            if (interactor != null) {
                interactor.mouseClicked(e);
                deselectInteractor();
            } else if (eventDispatcher != null) {
                dispatchMouseClicked(e);
            }
        }
        protected void dispatchMouseClicked(MouseEvent e) {
            eventDispatcher.mouseClicked(e);
        }
        public void mousePressed(MouseEvent e) {
            startX = e.getX();
            startY = e.getY();
            startTime = e.getWhen();
            checkClick = true;
            selectInteractor(e);
            if (interactor != null) {
                interactor.mousePressed(e);
                deselectInteractor();
            } else if (eventDispatcher != null) {
                dispatchMousePressed(e);
            }
        }
        protected void dispatchMousePressed(MouseEvent e) {
            eventDispatcher.mousePressed(e);
        }
        public void mouseReleased(java.awt.event.MouseEvent e) {
            if ((checkClick) && hadDrag) {
                int dx = startX-e.getX();
                int dy = startY-e.getY();
                long cTime = e.getWhen();
                if ((dx*dx+dy*dy < MAX_DISP) &&
                    (cTime-startTime) < CLICK_TIME) {
                    MouseEvent click = new MouseEvent
                        (e.getComponent(),
                         MouseEvent.MOUSE_CLICKED,
                         e.getWhen(),
                         e.getModifiers(),
                         e.getX(),
                         e.getY(),
                         e.getClickCount(),
                         e.isPopupTrigger());
                    fakeClickTime = click.getWhen();
                    handleMouseClicked(click);
                }
            }
            checkClick = false;
            hadDrag = false;
            selectInteractor(e);
            if (interactor != null) {
                interactor.mouseReleased(e);
                deselectInteractor();
            } else if (eventDispatcher != null) {
                dispatchMouseReleased(e);
            }
        }
        protected void dispatchMouseReleased(MouseEvent e) {
            eventDispatcher.mouseReleased(e);
        }
        public void mouseEntered(MouseEvent e) {
            selectInteractor(e);
            if (interactor != null) {
                interactor.mouseEntered(e);
                deselectInteractor();
            } else if (eventDispatcher != null) {
                dispatchMouseEntered(e);
            }
        }
        protected void dispatchMouseEntered(MouseEvent e) {
            eventDispatcher.mouseEntered(e);
        }
        public void mouseExited(MouseEvent e) {
            selectInteractor(e);
            if (interactor != null) {
                interactor.mouseExited(e);
                deselectInteractor();
            } else if (eventDispatcher != null) {
                dispatchMouseExited(e);
            }
        }
        protected void dispatchMouseExited(MouseEvent e) {
            eventDispatcher.mouseExited(e);
        }
        public void mouseDragged(MouseEvent e) {
            hadDrag = true;
            int dx = startX-e.getX();
            int dy = startY-e.getY();
            if (dx*dx+dy*dy > MAX_DISP)
                checkClick = false;
            selectInteractor(e);
            if (interactor != null) {
                interactor.mouseDragged(e);
                deselectInteractor();
            } else if (eventDispatcher != null) {
                dispatchMouseDragged(e);
            }
        }
        protected void dispatchMouseDragged(MouseEvent e) {
            eventDispatcher.mouseDragged(e);
        }
        public void mouseMoved(MouseEvent e) {
            selectInteractor(e);
            if (interactor != null) {
            	if (Platform.isOSX &&
            		interactor instanceof AbstractZoomInteractor)
            		mouseDragged(e);
            	else
            		interactor.mouseMoved(e);
                deselectInteractor();
            } else if (eventDispatcher != null) {
                dispatchMouseMoved(e);
            }
        }
        protected void dispatchMouseMoved(MouseEvent e) {
            eventDispatcher.mouseMoved(e);
        }
        public void mouseWheelMoved(MouseWheelEvent e) {
             if (eventDispatcher != null) {
                dispatchMouseWheelMoved(e);
            }
        }
        protected void dispatchMouseWheelMoved(MouseWheelEvent e) {
            eventDispatcher.mouseWheelMoved(e);
        }
        protected void selectInteractor(InputEvent ie) {
            if (!disableInteractions &&
                !suspendInteractions &&
                interactor == null &&
                gvtRoot != null) {
                Iterator it = interactors.iterator();
                while (it.hasNext()) {
                    Interactor i = (Interactor)it.next();
                    if (i.startInteraction(ie)) {
                        interactor = i;
                        break;
                    }
                }
            }
        }
        protected void deselectInteractor() {
            if (interactor.endInteraction()) {
                interactor = null;
            }
        }
    }
    protected class UnixTextSelectionListener
        extends SelectionAdapter {
        public void selectionDone(SelectionEvent evt) {
            if (!useUnixTextSelection) return;
            Object o = evt.getSelection();
            if (!(o instanceof CharacterIterator))
                return;
            CharacterIterator iter = (CharacterIterator) o;
            SecurityManager securityManager;
            securityManager = System.getSecurityManager();
            if (securityManager != null) {
                try {
                    securityManager.checkSystemClipboardAccess();
                } catch (SecurityException e) {
                    return; 
                }
            }
            int sz = iter.getEndIndex()-iter.getBeginIndex();
            if (sz == 0) return;
            char[] cbuff = new char[sz];
            cbuff[0] = iter.first();
            for (int i=1; i<cbuff.length;++i) {
                cbuff[i] = iter.next();
            }
            final String strSel = new String(cbuff);
            new Thread() {
                public void run() {
                    Clipboard cb;
                    cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                    StringSelection sel;
                    sel = new StringSelection(strSel);
                    cb.setContents(sel, sel);
                }
            }.start();
        }
    }
}
