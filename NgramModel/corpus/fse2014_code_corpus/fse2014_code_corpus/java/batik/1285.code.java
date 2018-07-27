package org.apache.batik.swing;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.swing.gvt.AbstractImageZoomInteractor;
import org.apache.batik.swing.gvt.AbstractPanInteractor;
import org.apache.batik.swing.gvt.AbstractResetTransformInteractor;
import org.apache.batik.swing.gvt.AbstractRotateInteractor;
import org.apache.batik.swing.gvt.AbstractZoomInteractor;
import org.apache.batik.swing.gvt.Interactor;
import org.apache.batik.swing.svg.JSVGComponent;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.swing.svg.SVGUserAgent;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLConstants;
import org.apache.batik.util.gui.JErrorPane;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGDocument;
public class JSVGCanvas extends JSVGComponent {
    public static final String SCROLL_RIGHT_ACTION = "ScrollRight";
    public static final String SCROLL_LEFT_ACTION = "ScrollLeft";
    public static final String SCROLL_UP_ACTION = "ScrollUp";
    public static final String SCROLL_DOWN_ACTION = "ScrollDown";
    public static final String FAST_SCROLL_RIGHT_ACTION = "FastScrollRight";
    public static final String FAST_SCROLL_LEFT_ACTION = "FastScrollLeft";
    public static final String FAST_SCROLL_UP_ACTION = "FastScrollUp";
    public static final String FAST_SCROLL_DOWN_ACTION = "FastScrollDown";
    public static final String ZOOM_IN_ACTION = "ZoomIn";
    public static final String ZOOM_OUT_ACTION = "ZoomOut";
    public static final String RESET_TRANSFORM_ACTION = "ResetTransform";
    private boolean isZoomInteractorEnabled = true;
    private boolean isImageZoomInteractorEnabled = true;
    private boolean isPanInteractorEnabled = true;
    private boolean isRotateInteractorEnabled = true;
    private boolean isResetTransformInteractorEnabled = true;
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    protected String uri;
    protected LocationListener locationListener = new LocationListener();
    protected Map toolTipMap = null;
    protected EventListener toolTipListener = new ToolTipModifier();
    protected EventTarget   lastTarget = null;
    protected Map toolTipDocs = null;
    protected static final Object MAP_TOKEN = new Object();
    protected long lastToolTipEventTimeStamp;
    protected EventTarget lastToolTipEventTarget;
    public JSVGCanvas() {
        this(null, true, true);
        addMouseMotionListener(locationListener);
    }
    public JSVGCanvas(SVGUserAgent ua,
                      boolean eventsEnabled,
                      boolean selectableText) {
        super(ua, eventsEnabled, selectableText);
        setPreferredSize(new Dimension(200, 200));
        setMinimumSize(new Dimension(100, 100));
        List intl = getInteractors();
        intl.add(zoomInteractor);
        intl.add(imageZoomInteractor);
        intl.add(panInteractor);
        intl.add(rotateInteractor);
        intl.add(resetTransformInteractor);
        installActions();
        if (eventsEnabled) {
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent evt) {
                    requestFocus();
                }
            });
            installKeyboardActions();
        }
        addMouseMotionListener(locationListener);
    }
    protected void installActions() {
        ActionMap actionMap = getActionMap();
        actionMap.put(SCROLL_RIGHT_ACTION, new ScrollRightAction(10));
        actionMap.put(SCROLL_LEFT_ACTION, new ScrollLeftAction(10));
        actionMap.put(SCROLL_UP_ACTION, new ScrollUpAction(10));
        actionMap.put(SCROLL_DOWN_ACTION, new ScrollDownAction(10));
        actionMap.put(FAST_SCROLL_RIGHT_ACTION, new ScrollRightAction(30));
        actionMap.put(FAST_SCROLL_LEFT_ACTION, new ScrollLeftAction(30));
        actionMap.put(FAST_SCROLL_UP_ACTION, new ScrollUpAction(30));
        actionMap.put(FAST_SCROLL_DOWN_ACTION, new ScrollDownAction(30));
        actionMap.put(ZOOM_IN_ACTION, new ZoomInAction());
        actionMap.put(ZOOM_OUT_ACTION, new ZoomOutAction());
        actionMap.put(RESET_TRANSFORM_ACTION, new ResetTransformAction());
    }
    public void setDisableInteractions(boolean b) {
        super.setDisableInteractions(b);
        ActionMap actionMap = getActionMap();
        actionMap.get(SCROLL_RIGHT_ACTION)     .setEnabled(!b);
        actionMap.get(SCROLL_LEFT_ACTION)      .setEnabled(!b);
        actionMap.get(SCROLL_UP_ACTION)        .setEnabled(!b);
        actionMap.get(SCROLL_DOWN_ACTION)      .setEnabled(!b);
        actionMap.get(FAST_SCROLL_RIGHT_ACTION).setEnabled(!b);
        actionMap.get(FAST_SCROLL_LEFT_ACTION) .setEnabled(!b);
        actionMap.get(FAST_SCROLL_UP_ACTION)   .setEnabled(!b);
        actionMap.get(FAST_SCROLL_DOWN_ACTION) .setEnabled(!b);
        actionMap.get(ZOOM_IN_ACTION)          .setEnabled(!b);
        actionMap.get(ZOOM_OUT_ACTION)         .setEnabled(!b);
        actionMap.get(RESET_TRANSFORM_ACTION)  .setEnabled(!b);
    }
    protected void installKeyboardActions() {
        InputMap inputMap = getInputMap(JComponent.WHEN_FOCUSED);
        KeyStroke key;
        key = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
        inputMap.put(key, SCROLL_RIGHT_ACTION);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
        inputMap.put(key, SCROLL_LEFT_ACTION);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
        inputMap.put(key, SCROLL_UP_ACTION);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
        inputMap.put(key, SCROLL_DOWN_ACTION);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_MASK);
        inputMap.put(key, FAST_SCROLL_RIGHT_ACTION);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_MASK);
        inputMap.put(key, FAST_SCROLL_LEFT_ACTION);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_MASK);
        inputMap.put(key, FAST_SCROLL_UP_ACTION);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_MASK);
        inputMap.put(key, FAST_SCROLL_DOWN_ACTION);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_MASK);
        inputMap.put(key, ZOOM_IN_ACTION);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK);
        inputMap.put(key, ZOOM_OUT_ACTION);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_MASK);
        inputMap.put(key, RESET_TRANSFORM_ACTION);
    }
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);
    }
    public void addPropertyChangeListener(String propertyName,
                                          PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(propertyName, pcl);
    }
    public void removePropertyChangeListener(String propertyName,
                                             PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(propertyName, pcl);
    }
    public void setEnableZoomInteractor(boolean b) {
        if (isZoomInteractorEnabled != b) {
            boolean oldValue = isZoomInteractorEnabled;
            isZoomInteractorEnabled = b;
            if (isZoomInteractorEnabled) {
                getInteractors().add(zoomInteractor);
            } else {
                getInteractors().remove(zoomInteractor);
            }
            pcs.firePropertyChange("enableZoomInteractor", oldValue, b);
        }
    }
    public boolean getEnableZoomInteractor() {
        return isZoomInteractorEnabled;
    }
    public void setEnableImageZoomInteractor(boolean b) {
        if (isImageZoomInteractorEnabled != b) {
            boolean oldValue = isImageZoomInteractorEnabled;
            isImageZoomInteractorEnabled = b;
            if (isImageZoomInteractorEnabled) {
                getInteractors().add(imageZoomInteractor);
            } else {
                getInteractors().remove(imageZoomInteractor);
            }
            pcs.firePropertyChange("enableImageZoomInteractor", oldValue, b);
        }
    }
    public boolean getEnableImageZoomInteractor() {
        return isImageZoomInteractorEnabled;
    }
    public void setEnablePanInteractor(boolean b) {
        if (isPanInteractorEnabled != b) {
            boolean oldValue = isPanInteractorEnabled;
            isPanInteractorEnabled = b;
            if (isPanInteractorEnabled) {
                getInteractors().add(panInteractor);
            } else {
                getInteractors().remove(panInteractor);
            }
            pcs.firePropertyChange("enablePanInteractor", oldValue, b);
        }
    }
    public boolean getEnablePanInteractor() {
        return isPanInteractorEnabled;
    }
    public void setEnableRotateInteractor(boolean b) {
        if (isRotateInteractorEnabled != b) {
            boolean oldValue = isRotateInteractorEnabled;
            isRotateInteractorEnabled = b;
            if (isRotateInteractorEnabled) {
                getInteractors().add(rotateInteractor);
            } else {
                getInteractors().remove(rotateInteractor);
            }
            pcs.firePropertyChange("enableRotateInteractor", oldValue, b);
        }
    }
    public boolean getEnableRotateInteractor() {
        return isRotateInteractorEnabled;
    }
    public void setEnableResetTransformInteractor(boolean b) {
        if (isResetTransformInteractorEnabled != b) {
            boolean oldValue = isResetTransformInteractorEnabled;
            isResetTransformInteractorEnabled = b;
            if (isResetTransformInteractorEnabled) {
                getInteractors().add(resetTransformInteractor);
            } else {
                getInteractors().remove(resetTransformInteractor);
            }
            pcs.firePropertyChange("enableResetTransformInteractor",
                                   oldValue,
                                   b);
        }
    }
    public boolean getEnableResetTransformInteractor() {
        return isResetTransformInteractorEnabled;
    }
    public String getURI() {
        return uri;
    }
    public void setURI(String newURI) {
        String oldValue = uri;
        this.uri = newURI;
        if (uri != null) {
            loadSVGDocument(uri);
        } else {
            setSVGDocument(null);
        }
        pcs.firePropertyChange("URI", oldValue, uri);
    }
    protected UserAgent createUserAgent() {
        return new CanvasUserAgent();
    }
    protected Listener createListener() {
        return new CanvasSVGListener();
    }
    protected class CanvasSVGListener extends SVGListener {
        public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
            super.documentLoadingStarted(e);
            JSVGCanvas.this.setToolTipText(null);
        }
    }
    protected void installSVGDocument(SVGDocument doc) {
        if (toolTipDocs != null) {
            Iterator i = toolTipDocs.keySet().iterator();
            while (i.hasNext()) {
                SVGDocument ttdoc;
                ttdoc = (SVGDocument)i.next();
                if (ttdoc == null) continue;
                NodeEventTarget root;
                root = (NodeEventTarget)ttdoc.getRootElement();
                if (root == null) continue;
                root.removeEventListenerNS
                    (XMLConstants.XML_EVENTS_NAMESPACE_URI,
                     SVGConstants.SVG_EVENT_MOUSEOVER,
                     toolTipListener, false);
                root.removeEventListenerNS
                    (XMLConstants.XML_EVENTS_NAMESPACE_URI,
                     SVGConstants.SVG_EVENT_MOUSEOUT,
                     toolTipListener, false);
            }
            toolTipDocs = null;
        }
        lastTarget = null;
        if (toolTipMap != null) {
            toolTipMap.clear();
        }
        super.installSVGDocument(doc);
    }
    public class ResetTransformAction extends AbstractAction {
        public void actionPerformed(ActionEvent evt) {
            fragmentIdentifier = null;
            resetRenderingTransform();
        }
    }
    public class AffineAction extends AbstractAction {
        AffineTransform at;
        public AffineAction(AffineTransform at) {
            this.at = at;
        }
        public void actionPerformed(ActionEvent evt) {
            if (gvtRoot == null) {
                return;
            }
            AffineTransform rat = getRenderingTransform();
            if (at != null) {
                Dimension dim = getSize();
                int x = dim.width / 2;
                int y = dim.height / 2;
                AffineTransform t = AffineTransform.getTranslateInstance(x, y);
                t.concatenate(at);
                t.translate(-x, -y);
                t.concatenate(rat);
                setRenderingTransform(t);
            }
        }
    }
    public class ZoomAction extends AffineAction {
        public ZoomAction(double scale) {
            super(AffineTransform.getScaleInstance(scale, scale));
        }
        public ZoomAction(double scaleX, double scaleY) {
            super(AffineTransform.getScaleInstance(scaleX, scaleY));
        }
    }
    public class ZoomInAction extends ZoomAction {
        ZoomInAction() { super(2); }
    }
    public class ZoomOutAction extends ZoomAction {
        ZoomOutAction() { super(.5); }
    }
    public class RotateAction extends AffineAction {
        public RotateAction(double theta) {
            super(AffineTransform.getRotateInstance(theta));
        }
    }
    public class ScrollAction extends AffineAction {
        public ScrollAction(double tx, double ty) {
            super(AffineTransform.getTranslateInstance(tx, ty));
        }
    }
    public class ScrollRightAction extends ScrollAction {
        public ScrollRightAction(int inc) {
            super(-inc, 0);
        }
    }
    public class ScrollLeftAction extends ScrollAction {
        public ScrollLeftAction(int inc) {
            super(inc, 0);
        }
    }
    public class ScrollUpAction extends ScrollAction {
        public ScrollUpAction(int inc) {
            super(0, inc);
        }
    }
    public class ScrollDownAction extends ScrollAction {
        public ScrollDownAction(int inc) {
            super(0, -inc);
        }
    }
    protected Interactor zoomInteractor = new AbstractZoomInteractor() {
        public boolean startInteraction(InputEvent ie) {
            int mods = ie.getModifiers();
            return
                ie.getID() == MouseEvent.MOUSE_PRESSED &&
                (mods & InputEvent.BUTTON1_MASK) != 0 &&
                (mods & InputEvent.CTRL_MASK) != 0;
        }
    };
    protected Interactor imageZoomInteractor
        = new AbstractImageZoomInteractor() {
        public boolean startInteraction(InputEvent ie) {
            int mods = ie.getModifiers();
            return
                ie.getID() == MouseEvent.MOUSE_PRESSED &&
                (mods & InputEvent.BUTTON3_MASK) != 0 &&
                (mods & InputEvent.SHIFT_MASK) != 0;
        }
    };
    protected Interactor panInteractor = new AbstractPanInteractor() {
        public boolean startInteraction(InputEvent ie) {
            int mods = ie.getModifiers();
            return
                ie.getID() == MouseEvent.MOUSE_PRESSED &&
                (mods & InputEvent.BUTTON1_MASK) != 0 &&
                (mods & InputEvent.SHIFT_MASK) != 0;
        }
    };
    protected Interactor rotateInteractor = new AbstractRotateInteractor() {
        public boolean startInteraction(InputEvent ie) {
            int mods = ie.getModifiers();
            return
                ie.getID() == MouseEvent.MOUSE_PRESSED &&
                (mods & InputEvent.BUTTON3_MASK) != 0 &&
                (mods & InputEvent.CTRL_MASK) != 0;
        }
    };
    protected Interactor resetTransformInteractor =
        new AbstractResetTransformInteractor() {
        public boolean startInteraction(InputEvent ie) {
            int mods = ie.getModifiers();
            return
                ie.getID() == MouseEvent.MOUSE_CLICKED &&
                (mods & InputEvent.BUTTON3_MASK) != 0 &&
                (mods & InputEvent.SHIFT_MASK) != 0 &&
                (mods & InputEvent.CTRL_MASK) != 0;
        }
    };
    protected class CanvasUserAgent extends BridgeUserAgent
        implements XMLConstants {
        final String TOOLTIP_TITLE_ONLY
            = "JSVGCanvas.CanvasUserAgent.ToolTip.titleOnly";
        final String TOOLTIP_DESC_ONLY
            = "JSVGCanvas.CanvasUserAgent.ToolTip.descOnly";
        final String TOOLTIP_TITLE_AND_TEXT
            = "JSVGCanvas.CanvasUserAgent.ToolTip.titleAndDesc";
        public void handleElement(Element elt, Object data){
            super.handleElement(elt, data);
            if (!isInteractive()) return;
            if (!SVGConstants.SVG_NAMESPACE_URI.equals(elt.getNamespaceURI()))
                return;
            if (elt.getParentNode() ==
                elt.getOwnerDocument().getDocumentElement()) {
                return;
            }
            Element parent;
            if (data instanceof Element) parent = (Element)data;
            else                         parent = (Element)elt.getParentNode();
            Element descPeer = null;
            Element titlePeer = null;
            if (elt.getLocalName().equals(SVGConstants.SVG_TITLE_TAG)) {
                if (data == Boolean.TRUE)
                    titlePeer = elt;
                descPeer = getPeerWithTag(parent,
                                           SVGConstants.SVG_NAMESPACE_URI,
                                           SVGConstants.SVG_DESC_TAG);
            } else if (elt.getLocalName().equals(SVGConstants.SVG_DESC_TAG)) {
                if (data == Boolean.TRUE)
                    descPeer = elt;
                titlePeer = getPeerWithTag(parent,
                                           SVGConstants.SVG_NAMESPACE_URI,
                                           SVGConstants.SVG_TITLE_TAG);
            }
            String titleTip = null;
            if (titlePeer != null) {
                titlePeer.normalize();
                if (titlePeer.getFirstChild() != null)
                    titleTip = titlePeer.getFirstChild().getNodeValue();
            }
            String descTip = null;
            if (descPeer != null) {
                descPeer.normalize();
                if (descPeer.getFirstChild() != null)
                    descTip = descPeer.getFirstChild().getNodeValue();
            }
            final String toolTip;
            if ((titleTip != null) && (titleTip.length() != 0)) {
                if ((descTip != null) && (descTip.length() != 0)) {
                    toolTip = Messages.formatMessage
                        (TOOLTIP_TITLE_AND_TEXT,
                         new Object[] { toFormattedHTML(titleTip),
                                        toFormattedHTML(descTip)});
                } else {
                    toolTip = Messages.formatMessage
                        (TOOLTIP_TITLE_ONLY,
                         new Object[]{toFormattedHTML(titleTip)});
                }
            } else {
                if ((descTip != null) && (descTip.length() != 0)) {
                    toolTip = Messages.formatMessage
                        (TOOLTIP_DESC_ONLY,
                         new Object[]{toFormattedHTML(descTip)});
                } else {
                    toolTip = null;
                }
            }
            if (toolTip == null) {
                removeToolTip(parent);
                return;
            }
            if (lastTarget != parent) {
                setToolTip(parent, toolTip);
            } else {
                Object o = null;
                if (toolTipMap != null) {
                    o = toolTipMap.get(parent);
                    toolTipMap.put(parent, toolTip);
                }
                if (o != null) {
                    EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                setToolTipText(toolTip);
                                MouseEvent e = new MouseEvent
                                    (JSVGCanvas.this,
                                     MouseEvent.MOUSE_MOVED,
                                     System.currentTimeMillis(),
                                     0,
                                     locationListener.getLastX(),
                                     locationListener.getLastY(),
                                     0,
                                     false);
                                ToolTipManager.sharedInstance().mouseMoved(e);
                            }
                        });
                } else {
                    EventQueue.invokeLater(new ToolTipRunnable(toolTip));
                }
            }
        }
        public String toFormattedHTML(String str) {
            StringBuffer sb = new StringBuffer(str);
            replace(sb, XML_CHAR_AMP, XML_ENTITY_AMP);  
            replace(sb, XML_CHAR_LT, XML_ENTITY_LT);
            replace(sb, XML_CHAR_GT, XML_ENTITY_GT);
            replace(sb, XML_CHAR_QUOT, XML_ENTITY_QUOT);
            replace(sb, '\n', "<br>");
            return sb.toString();
        }
        protected void replace(StringBuffer sb, char c, String r) {
            String v = sb.toString();
            int i = v.length();
            while( (i=v.lastIndexOf(c, i-1)) != -1 ) {
                sb.deleteCharAt(i);
                sb.insert(i, r);
            }
        }
        public Element getPeerWithTag(Element parent,
                                      String nameSpaceURI,
                                      String localName) {
            Element p = parent;
            if (p == null) {
                return null;
            }
            for (Node n=p.getFirstChild(); n!=null; n = n.getNextSibling()) {
                if (!nameSpaceURI.equals(n.getNamespaceURI())){
                    continue;
                }
                if (!localName.equals(n.getLocalName())){
                    continue;
                }
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    return (Element)n;
                }
            }
            return null;
        }
        public boolean hasPeerWithTag(Element elt,
                                      String nameSpaceURI,
                                      String localName){
            return !(getPeerWithTag(elt, nameSpaceURI, localName) == null);
        }
        public void setToolTip(Element elt, String toolTip){
            if (toolTipMap == null) {
                toolTipMap = new WeakHashMap();
            }
            if (toolTipDocs == null) {
                toolTipDocs = new WeakHashMap();
            }
            SVGDocument doc = (SVGDocument)elt.getOwnerDocument();
            if (toolTipDocs.put(doc, MAP_TOKEN) == null) {
                NodeEventTarget root;
                root = (NodeEventTarget)doc.getRootElement();
                root.addEventListenerNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                                        SVGConstants.SVG_EVENT_MOUSEOVER,
                                        toolTipListener,
                                        false, null);
                root.addEventListenerNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                                        SVGConstants.SVG_EVENT_MOUSEOUT,
                                        toolTipListener,
                                        false, null);
            }
            toolTipMap.put(elt, toolTip);
            if (elt == lastTarget)
                EventQueue.invokeLater(new ToolTipRunnable(toolTip));
        }
        public void removeToolTip(Element elt) {
            if (toolTipMap != null)
                toolTipMap.remove(elt);
            if (lastTarget == elt) { 
                EventQueue.invokeLater(new ToolTipRunnable(null));
            }
        }
        public void displayError(String message) {
            if (svgUserAgent != null) {
                super.displayError(message);
            } else {
                JOptionPane pane =
                    new JOptionPane(message, JOptionPane.ERROR_MESSAGE);
                JDialog dialog =
                    pane.createDialog(JSVGCanvas.this, "ERROR");
                dialog.setModal(false);
                dialog.setVisible(true); 
            }
        }
        public void displayError(Exception ex) {
            if (svgUserAgent != null) {
                super.displayError(ex);
            } else {
                JErrorPane pane =
                    new JErrorPane(ex, JOptionPane.ERROR_MESSAGE);
                JDialog dialog = pane.createDialog(JSVGCanvas.this, "ERROR");
                dialog.setModal(false);
                dialog.setVisible(true); 
            }
        }
    }
    public void setLastToolTipEvent(long t, EventTarget et) {
        lastToolTipEventTimeStamp = t;
        lastToolTipEventTarget = et;
    }
    public boolean matchLastToolTipEvent(long t, EventTarget et) {
        return lastToolTipEventTimeStamp == t
            && lastToolTipEventTarget == et;
    }
    protected class LocationListener extends MouseMotionAdapter {
        protected int lastX, lastY;
        public LocationListener () {
            lastX = 0; lastY = 0;
        }
        public void mouseMoved(MouseEvent evt) {
            lastX = evt.getX();
            lastY = evt.getY();
        }
        public int getLastX() {
            return lastX;
        }
        public int getLastY() {
            return lastY;
        }
    }
    protected class ToolTipModifier implements EventListener {
        protected CanvasUserAgent canvasUserAgent;
        public ToolTipModifier() {
        }
        public void handleEvent(Event evt){
            if (matchLastToolTipEvent(evt.getTimeStamp(), evt.getTarget())) {
                return;
            }
            setLastToolTipEvent(evt.getTimeStamp(), evt.getTarget());
            EventTarget prevLastTarget = lastTarget;
            if (SVGConstants.SVG_EVENT_MOUSEOVER.equals(evt.getType())) {
                lastTarget = evt.getTarget();
            } else if (SVGConstants.SVG_EVENT_MOUSEOUT.equals(evt.getType())) {
                org.w3c.dom.events.MouseEvent mouseEvt;
                mouseEvt = ((org.w3c.dom.events.MouseEvent)evt);
                lastTarget = mouseEvt.getRelatedTarget();
            }
            if (toolTipMap != null) {
                Element e = (Element)lastTarget;
                Object o = null;
                while (e != null) {
                    o = toolTipMap.get(e);
                    if (o != null) {
                        break;
                    }
                    e = CSSEngine.getParentCSSStylableElement(e);
                }
                final String theToolTip = (String)o;
                if (prevLastTarget != lastTarget)
                    EventQueue.invokeLater(new ToolTipRunnable(theToolTip));
            }
        }
    }
    protected class ToolTipRunnable implements Runnable {
        String theToolTip;
        public ToolTipRunnable(String toolTip) {
            this.theToolTip = toolTip;
        }
        public void run() {
            setToolTipText(theToolTip);
            MouseEvent e;
            if (theToolTip != null) {
                e = new MouseEvent
                    (JSVGCanvas.this,
                     MouseEvent.MOUSE_ENTERED,
                     System.currentTimeMillis(),
                     0,
                     locationListener.getLastX(),
                     locationListener.getLastY(),
                     0,
                     false);
                ToolTipManager.sharedInstance().mouseEntered(e);
                e = new MouseEvent
                    (JSVGCanvas.this,
                     MouseEvent.MOUSE_MOVED,
                     System.currentTimeMillis(),
                     0,
                     locationListener.getLastX(),
                     locationListener.getLastY(),
                     0,
                     false);
                ToolTipManager.sharedInstance().mouseMoved(e);
            } else {
                e = new MouseEvent
                    (JSVGCanvas.this,
                     MouseEvent.MOUSE_MOVED,
                     System.currentTimeMillis(),
                     0,
                     locationListener.getLastX(),
                     locationListener.getLastY(),
                     0,
                     false);
                ToolTipManager.sharedInstance().mouseMoved(e);
            }
        }
    }
}
