package org.apache.batik.bridge;
import java.awt.Cursor;
import java.awt.geom.Dimension2D;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.apache.batik.bridge.svg12.SVG12BridgeContext;
import org.apache.batik.bridge.svg12.SVG12BridgeExtension;
import org.apache.batik.css.engine.CSSContext;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSEngineEvent;
import org.apache.batik.css.engine.CSSEngineListener;
import org.apache.batik.css.engine.CSSEngineUserAgent;
import org.apache.batik.css.engine.SVGCSSEngine;
import org.apache.batik.css.engine.SystemColorSupport;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.svg.AnimatedAttributeListener;
import org.apache.batik.dom.svg.AnimatedLiveAttributeValue;
import org.apache.batik.dom.svg.SVGContext;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.svg.SVGOMElement;
import org.apache.batik.dom.svg.SVGStylableElement;
import org.apache.batik.dom.xbl.XBLManager;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.TextPainter;
import org.apache.batik.script.Interpreter;
import org.apache.batik.script.InterpreterPool;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.CleanerThread;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.Service;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MouseEvent;
import org.w3c.dom.events.MutationEvent;
import org.w3c.dom.svg.SVGDocument;
public class BridgeContext implements ErrorConstants, CSSContext {
    protected Document document;
    protected boolean isSVG12;
    protected GVTBuilder gvtBuilder;
    protected Map interpreterMap = new HashMap(7);
    private Map fontFamilyMap;
    protected Map viewportMap = new WeakHashMap();
    protected List viewportStack = new LinkedList();
    protected UserAgent userAgent;
    protected Map elementNodeMap;
    protected Map nodeElementMap;
    protected Map namespaceURIMap;
    protected Bridge defaultBridge;
    protected Set reservedNamespaceSet;
    protected Map elementDataMap;
    protected InterpreterPool interpreterPool;
    protected DocumentLoader documentLoader;
    protected Dimension2D documentSize;
    protected TextPainter textPainter;
    public static final int STATIC      = 0;
    public static final int INTERACTIVE = 1;
    public static final int DYNAMIC     = 2;
    protected int dynamicStatus = STATIC;
    protected UpdateManager updateManager;
    protected XBLManager xblManager;
    protected BridgeContext primaryContext;
    protected HashSet childContexts = new HashSet();
    protected SVGAnimationEngine animationEngine;
    protected int animationLimitingMode;
    protected float animationLimitingAmount;
    private static InterpreterPool sharedPool = new InterpreterPool();
    protected BridgeContext() {}
    public BridgeContext(UserAgent userAgent) {
        this(userAgent,
             sharedPool,
             new DocumentLoader(userAgent));
    }
    public BridgeContext(UserAgent userAgent,
                         DocumentLoader loader) {
        this(userAgent, sharedPool, loader);
    }
    public BridgeContext(UserAgent userAgent,
                         InterpreterPool interpreterPool,
                         DocumentLoader documentLoader) {
        this.userAgent = userAgent;
        this.viewportMap.put(userAgent, new UserAgentViewport(userAgent));
        this.interpreterPool = interpreterPool;
        this.documentLoader = documentLoader;
    }
    protected void finalize() {
        if (primaryContext != null) {
            dispose();
        }
    }
    public BridgeContext createSubBridgeContext(SVGOMDocument newDoc) {
        BridgeContext subCtx;
        CSSEngine eng = newDoc.getCSSEngine();
        if (eng != null) {
            subCtx = (BridgeContext) newDoc.getCSSEngine().getCSSContext();
            return subCtx;
        }
        subCtx = createBridgeContext(newDoc);
        subCtx.primaryContext = primaryContext != null ? primaryContext : this;
        subCtx.primaryContext.childContexts.add(new WeakReference(subCtx));
        subCtx.dynamicStatus = dynamicStatus;
        subCtx.setGVTBuilder(getGVTBuilder());
        subCtx.setTextPainter(getTextPainter());
        subCtx.setDocument(newDoc);
        subCtx.initializeDocument(newDoc);
        if (isInteractive())
            subCtx.addUIEventListeners(newDoc);
        return subCtx;
    }
    public BridgeContext createBridgeContext(SVGOMDocument doc) {
        if (doc.isSVG12()) {
            return new SVG12BridgeContext(getUserAgent(), getDocumentLoader());
        }
        return new BridgeContext(getUserAgent(), getDocumentLoader());
    }
    protected void initializeDocument(Document document) {
        SVGOMDocument doc = (SVGOMDocument)document;
        CSSEngine eng = doc.getCSSEngine();
        if (eng == null) {
            SVGDOMImplementation impl;
            impl = (SVGDOMImplementation)doc.getImplementation();
            eng = impl.createCSSEngine(doc, this);
            eng.setCSSEngineUserAgent(new CSSEngineUserAgentWrapper(userAgent));
            doc.setCSSEngine(eng);
            eng.setMedia(userAgent.getMedia());
            String uri = userAgent.getUserStyleSheetURI();
            if (uri != null) {
                try {
                    ParsedURL url = new ParsedURL(uri);
                    eng.setUserAgentStyleSheet
                        (eng.parseStyleSheet(url, "all"));
                } catch (Exception e) {
                    userAgent.displayError(e);
                }
            }
            eng.setAlternateStyleSheet(userAgent.getAlternateStyleSheet());
        }
    }
    public CSSEngine getCSSEngineForElement(Element e) {
        SVGOMDocument doc = (SVGOMDocument)e.getOwnerDocument();
        return doc.getCSSEngine();
    }
    public void setTextPainter(TextPainter textPainter) {
        this.textPainter = textPainter;
    }
    public TextPainter getTextPainter() {
        return textPainter;
    }
    public Document getDocument() {
        return document;
    }
    protected void setDocument(Document document) {
        if (this.document != document){
            fontFamilyMap = null;
        }
        this.document = document;
        this.isSVG12 = ((SVGOMDocument) document).isSVG12();
        registerSVGBridges();
    }
    public Map getFontFamilyMap(){
        if (fontFamilyMap == null){
            fontFamilyMap = new HashMap();
        }
        return fontFamilyMap;
    }
    protected void setFontFamilyMap(Map fontFamilyMap) {
        this.fontFamilyMap = fontFamilyMap;
    }
    public void setElementData(Node n, Object data) {
        if (elementDataMap == null) {
            elementDataMap = new WeakHashMap();
        }
        elementDataMap.put(n, new SoftReference(data));
    }
    public Object getElementData(Node n) {
        if (elementDataMap == null)
            return null;
        Object o = elementDataMap.get(n);
        if (o == null) return null;
        SoftReference sr = (SoftReference)o;
        o = sr.get();
        if (o == null) {
            elementDataMap.remove(n);
        }
        return o;
    }
    public UserAgent getUserAgent() {
        return userAgent;
    }
    protected void setUserAgent(UserAgent userAgent) {
        this.userAgent = userAgent;
    }
    public GVTBuilder getGVTBuilder() {
        return gvtBuilder;
    }
    protected void setGVTBuilder(GVTBuilder gvtBuilder) {
        this.gvtBuilder = gvtBuilder;
    }
    public InterpreterPool getInterpreterPool() {
        return interpreterPool;
    }
    public FocusManager getFocusManager() {
        return focusManager;
    }
    public CursorManager getCursorManager() {
        return cursorManager;
    }
    protected void setInterpreterPool(InterpreterPool interpreterPool) {
        this.interpreterPool = interpreterPool;
    }
    public Interpreter getInterpreter(String language) {
        if (document == null) {
            throw new RuntimeException("Unknown document");
        }
        Interpreter interpreter = (Interpreter)interpreterMap.get(language);
        if (interpreter == null) {
            try {
                interpreter = interpreterPool.createInterpreter(document, 
                                                                language,
                                                                null);
                String[] mimeTypes = interpreter.getMimeTypes();
                for (int i = 0; i < mimeTypes.length; i++) {
                    interpreterMap.put(mimeTypes[i], interpreter);
                }
            } catch (Exception e) {
                if (userAgent != null) {
                    userAgent.displayError(e);
                    return null;
                }
            }
        }
        if (interpreter == null) {
            if (userAgent != null) {
                userAgent.displayError(new Exception("Unknown language: " + language));
            }
        }
        return interpreter;
    }
    public DocumentLoader getDocumentLoader() {
        return documentLoader;
    }
    protected void setDocumentLoader(DocumentLoader newDocumentLoader) {
        this.documentLoader = newDocumentLoader;
    }
    public Dimension2D getDocumentSize() {
        return documentSize;
    }
    protected void setDocumentSize(Dimension2D d) {
        this.documentSize = d;
    }
    public boolean isDynamic() {
        return (dynamicStatus == DYNAMIC);
    }
    public boolean isInteractive() {
        return (dynamicStatus != STATIC);
    }
    public void setDynamicState(int status) {
        dynamicStatus = status;
    }
    public void setDynamic(boolean dynamic) {
        if (dynamic)
            setDynamicState(DYNAMIC);
        else
            setDynamicState(STATIC);
    }
    public void setInteractive(boolean interactive) {
        if (interactive)
            setDynamicState(INTERACTIVE);
        else
            setDynamicState(STATIC);
    }
    public UpdateManager getUpdateManager() {
        return updateManager;
    }
    protected void setUpdateManager(UpdateManager um) {
        updateManager = um;
    }
    protected void setUpdateManager(BridgeContext ctx, UpdateManager um) {
        ctx.setUpdateManager(um);
    }
    protected void setXBLManager(BridgeContext ctx, XBLManager xm) {
        ctx.xblManager = xm;
    }
    public boolean isSVG12() {
        return isSVG12;
    }
    public BridgeContext getPrimaryBridgeContext() {
        if (primaryContext != null) {
            return primaryContext;
        }
        return this;
    }
    public BridgeContext[] getChildContexts() {
        BridgeContext[] res = new BridgeContext[childContexts.size()];
        Iterator it = childContexts.iterator();
        for (int i = 0; i < res.length; i++) {
            WeakReference wr = (WeakReference) it.next();
            res[i] = (BridgeContext) wr.get();
        }
        return res;
    }
    public SVGAnimationEngine getAnimationEngine() {
        if (animationEngine == null) {
            animationEngine = new SVGAnimationEngine(document, this);
            setAnimationLimitingMode();
        }
        return animationEngine;
    }
    public URIResolver createURIResolver(SVGDocument doc, DocumentLoader dl) {
        return new URIResolver(doc, dl);
    }
    public Node getReferencedNode(Element e, String uri) {
        try {
            SVGDocument document = (SVGDocument)e.getOwnerDocument();
            URIResolver ur = createURIResolver(document, documentLoader);
            Node ref = ur.getNode(uri, e);
            if (ref == null) {
                throw new BridgeException(this, e, ERR_URI_BAD_TARGET,
                                          new Object[] {uri});
            } else {
                SVGOMDocument refDoc =
                    (SVGOMDocument) (ref.getNodeType() == Node.DOCUMENT_NODE
                                       ? ref
                                       : ref.getOwnerDocument());
                if (refDoc != document) {
                    createSubBridgeContext(refDoc);
                }
                return ref;
            }
        } catch (MalformedURLException ex) {
            throw new BridgeException(this, e, ex, ERR_URI_MALFORMED,
                                      new Object[] {uri});
        } catch (InterruptedIOException ex) {
            throw new InterruptedBridgeException();
        } catch (IOException ex) {
            throw new BridgeException(this, e, ex, ERR_URI_IO,
                                      new Object[] {uri});
        } catch (SecurityException ex) {
            throw new BridgeException(this, e, ex, ERR_URI_UNSECURE,
                                      new Object[] {uri});
        }
    }
    public Element getReferencedElement(Element e, String uri) {
        Node ref = getReferencedNode(e, uri);
        if (ref != null && ref.getNodeType() != Node.ELEMENT_NODE) {
            throw new BridgeException(this, e, ERR_URI_REFERENCE_A_DOCUMENT,
                                      new Object[] {uri});
        }
        return (Element) ref;
    }
    public Viewport getViewport(Element e) {
        if (viewportStack != null) {
            if (viewportStack.size() == 0) {
                return (Viewport)viewportMap.get(userAgent);
            } else {
                return (Viewport)viewportStack.get(0);
            }
        } else {
            e = SVGUtilities.getParentElement(e);
            while (e != null) {
                Viewport viewport = (Viewport)viewportMap.get(e);
                if (viewport != null) {
                    return viewport;
                }
                e = SVGUtilities.getParentElement(e);
            }
            return (Viewport)viewportMap.get(userAgent);
        }
    }
    public void openViewport(Element e, Viewport viewport) {
        viewportMap.put(e, viewport);
        if (viewportStack == null) {
            viewportStack = new LinkedList();
        }
        viewportStack.add(0, viewport);
    }
    public void removeViewport(Element e) {
        viewportMap.remove(e);
    }
    public void closeViewport(Element e) {
        viewportStack.remove(0);
        if (viewportStack.size() == 0) {
            viewportStack = null;
        }
    }
    public void bind(Node node, GraphicsNode gn) {
        if (elementNodeMap == null) {
            elementNodeMap = new WeakHashMap();
            nodeElementMap = new WeakHashMap();
        }
        elementNodeMap.put(node, new SoftReference(gn));
        nodeElementMap.put(gn, new SoftReference(node));
    }
    public void unbind(Node node) {
        if (elementNodeMap == null) {
            return;
        }
        GraphicsNode gn = null;
        SoftReference sr = (SoftReference)elementNodeMap.get(node);
        if (sr != null)
            gn = (GraphicsNode)sr.get();
        elementNodeMap.remove(node);
        if (gn != null)
            nodeElementMap.remove(gn);
    }
    public GraphicsNode getGraphicsNode(Node node) {
        if (elementNodeMap != null) {
            SoftReference sr = (SoftReference)elementNodeMap.get(node);
            if (sr != null)
                return (GraphicsNode)sr.get();
        }
        return null;
    }
    public Element getElement(GraphicsNode gn) {
        if (nodeElementMap != null) {
            SoftReference sr = (SoftReference)nodeElementMap.get(gn);
            if (sr != null) {
                Node n = (Node) sr.get();
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    return (Element) n;
                }
            }
        }
        return null;
    }
    public boolean hasGraphicsNodeBridge(Element element) {
        if (namespaceURIMap == null || element == null) {
            return false;
        }
        String localName = element.getLocalName();
        String namespaceURI = element.getNamespaceURI();
        namespaceURI = ((namespaceURI == null)? "" : namespaceURI);
        HashMap localNameMap = (HashMap) namespaceURIMap.get(namespaceURI);
        if (localNameMap == null) {
            return false;
        }
        return (localNameMap.get(localName) instanceof GraphicsNodeBridge);
    }
    public DocumentBridge getDocumentBridge() {
        return new SVGDocumentBridge();
    }
    public Bridge getBridge(Element element) {
        if (namespaceURIMap == null || element == null) {
            return null;
        }
        String localName = element.getLocalName();
        String namespaceURI = element.getNamespaceURI();
        namespaceURI = ((namespaceURI == null)? "" : namespaceURI);
        return getBridge(namespaceURI, localName);
    }
    public Bridge getBridge(String namespaceURI, String localName) {
        Bridge bridge = null;
        if (namespaceURIMap != null) {
            HashMap localNameMap = (HashMap) namespaceURIMap.get(namespaceURI);
            if (localNameMap != null) {
                bridge = (Bridge)localNameMap.get(localName);
            }
        }
        if (bridge == null
                && (reservedNamespaceSet == null
                    || !reservedNamespaceSet.contains(namespaceURI))) {
            bridge = defaultBridge;
        }
        if (isDynamic()) {
            return bridge == null ? null : bridge.getInstance();
        } else {
            return bridge;
        }
    }
    public void putBridge(String namespaceURI, String localName, Bridge bridge) {
        if (!(namespaceURI.equals(bridge.getNamespaceURI())
              && localName.equals(bridge.getLocalName()))) {
            throw new Error("Invalid Bridge: "+
                            namespaceURI+"/"+bridge.getNamespaceURI()+" "+
                            localName+"/"+bridge.getLocalName()+" "+
                            bridge.getClass());
        }
        if (namespaceURIMap == null) {
            namespaceURIMap = new HashMap();
        }
        namespaceURI = ((namespaceURI == null)? "" : namespaceURI);
        HashMap localNameMap = (HashMap) namespaceURIMap.get(namespaceURI);
        if (localNameMap == null) {
            localNameMap = new HashMap();
            namespaceURIMap.put(namespaceURI, localNameMap);
        }
        localNameMap.put(localName, bridge);
    }
    public void putBridge(Bridge bridge) {
        putBridge(bridge.getNamespaceURI(), bridge.getLocalName(), bridge);
    }
    public void removeBridge(String namespaceURI, String localName) {
        if (namespaceURIMap == null) {
            return;
        }
        namespaceURI = ((namespaceURI == null)? "" : namespaceURI);
        HashMap localNameMap = (HashMap) namespaceURIMap.get(namespaceURI);
        if (localNameMap != null) {
            localNameMap.remove(localName);
            if (localNameMap.isEmpty()) {
                namespaceURIMap.remove(namespaceURI);
                if (namespaceURIMap.isEmpty()) {
                    namespaceURIMap = null;
                }
            }
        }
    }
    public void setDefaultBridge(Bridge bridge) {
        defaultBridge = bridge;
    }
    public void putReservedNamespaceURI(String namespaceURI) {
        if (namespaceURI == null) {
            namespaceURI = "";
        }
        if (reservedNamespaceSet == null) {
            reservedNamespaceSet = new HashSet();
        }
        reservedNamespaceSet.add(namespaceURI);
    }
    public void removeReservedNamespaceURI(String namespaceURI) {
        if (namespaceURI == null) {
            namespaceURI = "";
        }
        if (reservedNamespaceSet != null) {
            reservedNamespaceSet.remove(namespaceURI);
            if (reservedNamespaceSet.isEmpty()) {
                reservedNamespaceSet = null;
            }
        }
    }
    protected Set eventListenerSet = new HashSet();
    protected EventListener domCharacterDataModifiedEventListener;
    protected EventListener domAttrModifiedEventListener;
    protected EventListener domNodeInsertedEventListener;
    protected EventListener domNodeRemovedEventListener;
    protected CSSEngineListener cssPropertiesChangedListener;
    protected AnimatedAttributeListener animatedAttributeListener;
    protected FocusManager focusManager;
    protected CursorManager cursorManager = new CursorManager(this);
    public void addUIEventListeners(Document doc) {
        NodeEventTarget evtTarget = (NodeEventTarget)doc.getDocumentElement();
        DOMMouseOverEventListener domMouseOverListener =
            new DOMMouseOverEventListener();
        evtTarget.addEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             SVGConstants.SVG_EVENT_MOUSEOVER,
             domMouseOverListener, true, null);
        storeEventListenerNS
            (evtTarget,
             XMLConstants.XML_EVENTS_NAMESPACE_URI,
             SVGConstants.SVG_EVENT_MOUSEOVER,
             domMouseOverListener, true);
        DOMMouseOutEventListener domMouseOutListener =
            new DOMMouseOutEventListener();
        evtTarget.addEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             SVGConstants.SVG_EVENT_MOUSEOUT,
             domMouseOutListener, true, null);
        storeEventListenerNS
            (evtTarget,
             XMLConstants.XML_EVENTS_NAMESPACE_URI,
             SVGConstants.SVG_EVENT_MOUSEOUT,
             domMouseOutListener, true);
    }
    public void removeUIEventListeners(Document doc) {
        EventTarget evtTarget = (EventTarget)doc.getDocumentElement();
        synchronized (eventListenerSet) {
            Iterator i = eventListenerSet.iterator();
            while (i.hasNext()) {
                EventListenerMememto elm = (EventListenerMememto)i.next();
                NodeEventTarget et = elm.getTarget();
                if (et == evtTarget) {
                    EventListener el = elm.getListener();
                    boolean       uc = elm.getUseCapture();
                    String        t  = elm.getEventType();
                    boolean       n  = elm.getNamespaced();
                    if (et == null || el == null || t == null) {
                        continue;
                    }
                    if (n) {
                        String ns = elm.getNamespaceURI();
                        et.removeEventListenerNS(ns, t, el, uc);
                    } else {
                        et.removeEventListener(t, el, uc);
                    }
                }
            }
        }
    }
    public void addDOMListeners() {
        SVGOMDocument doc = (SVGOMDocument)document;
        domAttrModifiedEventListener = new DOMAttrModifiedEventListener();
        doc.addEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMAttrModified",
             domAttrModifiedEventListener, true, null);
        domNodeInsertedEventListener = new DOMNodeInsertedEventListener();
        doc.addEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeInserted",
             domNodeInsertedEventListener, true, null);
        domNodeRemovedEventListener = new DOMNodeRemovedEventListener();
        doc.addEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeRemoved",
             domNodeRemovedEventListener, true, null);
        domCharacterDataModifiedEventListener =
            new DOMCharacterDataModifiedEventListener();
        doc.addEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMCharacterDataModified",
             domCharacterDataModifiedEventListener, true, null);
        animatedAttributeListener = new AnimatedAttrListener();
        doc.addAnimatedAttributeListener(animatedAttributeListener);
        focusManager = new FocusManager(document);
        CSSEngine cssEngine = doc.getCSSEngine();
        cssPropertiesChangedListener = new CSSPropertiesChangedListener();
        cssEngine.addCSSEngineListener(cssPropertiesChangedListener);
    }
    protected void removeDOMListeners() {
        SVGOMDocument doc = (SVGOMDocument)document;
        doc.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMAttrModified",
             domAttrModifiedEventListener, true);
        doc.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMNodeInserted",
             domNodeInsertedEventListener, true);
        doc.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMNodeRemoved",
             domNodeRemovedEventListener, true);
        doc.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMCharacterDataModified",
             domCharacterDataModifiedEventListener, true);
        doc.removeAnimatedAttributeListener(animatedAttributeListener);
        CSSEngine cssEngine = doc.getCSSEngine();
        if (cssEngine != null) {
            cssEngine.removeCSSEngineListener
                (cssPropertiesChangedListener);
            cssEngine.dispose();
            doc.setCSSEngine(null);
        }
    }
    protected void storeEventListener(EventTarget t,
                                      String s,
                                      EventListener l,
                                      boolean b) {
        synchronized (eventListenerSet) {
            eventListenerSet.add(new EventListenerMememto(t, s, l, b, this));
        }
    }
    protected void storeEventListenerNS(EventTarget t,
                                        String n,
                                        String s,
                                        EventListener l,
                                        boolean b) {
        synchronized (eventListenerSet) {
            eventListenerSet.add(new EventListenerMememto(t, n, s, l, b, this));
        }
    }
    public static class SoftReferenceMememto
        extends CleanerThread.SoftReferenceCleared {
        Object mememto;
        Set    set;
        SoftReferenceMememto(Object ref, Object mememto, Set set) {
            super(ref);
            this.mememto = mememto;
            this.set     = set;
        }
        public void cleared() {
            synchronized (set) {
                set.remove(mememto);
                mememto = null;
                set     = null;
            }
        }
    }
    protected static class EventListenerMememto {
        public SoftReference target; 
        public SoftReference listener; 
        public boolean useCapture;
        public String namespaceURI;
        public String eventType;
        public boolean namespaced;
        public EventListenerMememto(EventTarget t,
                                    String s,
                                    EventListener l,
                                    boolean b,
                                    BridgeContext ctx) {
            Set set = ctx.eventListenerSet;
            target = new SoftReferenceMememto(t, this, set);
            listener = new SoftReferenceMememto(l, this, set);
            eventType = s;
            useCapture = b;
        }
        public EventListenerMememto(EventTarget t,
                                    String n,
                                    String s,
                                    EventListener l,
                                    boolean b,
                                    BridgeContext ctx) {
            this(t, s, l, b, ctx);
            namespaceURI = n;
            namespaced = true;
        }
        public EventListener getListener() {
            return (EventListener)listener.get();
        }
        public NodeEventTarget getTarget() {
            return (NodeEventTarget)target.get();
        }
        public boolean getUseCapture() {
            return useCapture;
        }
        public String getNamespaceURI() {
            return namespaceURI;
        }
        public String getEventType() {
            return eventType;
        }
        public boolean getNamespaced() {
            return namespaced;
        }
    }
    public void addGVTListener(Document doc) {
        BridgeEventSupport.addGVTListener(this, doc);
    }
    protected void clearChildContexts() {
        childContexts.clear();
    }
    public void dispose() {
        clearChildContexts();
        synchronized (eventListenerSet) {
            Iterator iter = eventListenerSet.iterator();
            while (iter.hasNext()) {
                EventListenerMememto m = (EventListenerMememto)iter.next();
                NodeEventTarget et = m.getTarget();
                EventListener   el = m.getListener();
                boolean         uc = m.getUseCapture();
                String          t  = m.getEventType();
                boolean         n  = m.getNamespaced();
                if (et == null || el == null || t == null) {
                    continue;
                }
                if (n) {
                    String ns = m.getNamespaceURI();
                    et.removeEventListenerNS(ns, t, el, uc);
                } else {
                    et.removeEventListener(t, el, uc);
                }
            }
        }
        if (document != null) {
            removeDOMListeners();
            AbstractGraphicsNodeBridge.disposeTree(document);
        }
        if (animationEngine != null) {
            animationEngine.dispose();
            animationEngine = null;
        }
        Iterator iter = interpreterMap.values().iterator();
        while (iter.hasNext()) {
            Interpreter interpreter = (Interpreter)iter.next();
            if (interpreter != null)
                interpreter.dispose();
        }
        interpreterMap.clear();
        if (focusManager != null) {
            focusManager.dispose();
        }
        if (elementDataMap != null) {
            elementDataMap.clear();
        }
        if (nodeElementMap != null) {
            nodeElementMap.clear();
        }
        if (elementNodeMap != null) {
            elementNodeMap.clear();
        }        
    }
    protected static SVGContext getSVGContext(Node node) {
        if (node instanceof SVGOMElement) {
            return ((SVGOMElement) node).getSVGContext();
        } else if (node instanceof SVGOMDocument) {
            return ((SVGOMDocument) node).getSVGContext();
        } else {
            return null;
        }
    }
    protected static BridgeUpdateHandler getBridgeUpdateHandler(Node node) {
        SVGContext ctx = getSVGContext(node);
        return (ctx == null) ? null : (BridgeUpdateHandler)ctx;
    }
    protected class DOMAttrModifiedEventListener implements EventListener {
        public DOMAttrModifiedEventListener() {
        }
        public void handleEvent(Event evt) {
            Node node = (Node)evt.getTarget();
            BridgeUpdateHandler h = getBridgeUpdateHandler(node);
            if (h != null) {
                try {
                    h.handleDOMAttrModifiedEvent((MutationEvent)evt);
                } catch (Exception e) {
                    userAgent.displayError(e);
                }
            }
        }
    }
    protected class DOMMouseOutEventListener implements EventListener {
        public DOMMouseOutEventListener() {
        }
        public void handleEvent(Event evt) {
            MouseEvent me = (MouseEvent)evt;
            Element newTarget = (Element)me.getRelatedTarget();
            Cursor cursor = CursorManager.DEFAULT_CURSOR;
            if (newTarget != null)
                cursor = CSSUtilities.convertCursor
                    (newTarget, BridgeContext.this);
            if (cursor == null)
                cursor = CursorManager.DEFAULT_CURSOR;
            userAgent.setSVGCursor(cursor);
        }
    }
    protected class DOMMouseOverEventListener implements EventListener {
        public DOMMouseOverEventListener() {
        }
        public void handleEvent(Event evt) {
            Element target = (Element)evt.getTarget();
            Cursor cursor = CSSUtilities.convertCursor(target, BridgeContext.this);
            if (cursor != null) {
                userAgent.setSVGCursor(cursor);
            }
        }
    }
    protected class DOMNodeInsertedEventListener implements EventListener {
        public DOMNodeInsertedEventListener() {
        }
        public void handleEvent(Event evt) {
            MutationEvent me = (MutationEvent)evt;
            BridgeUpdateHandler h =
                getBridgeUpdateHandler(me.getRelatedNode());
            if (h != null) {
                try {
                    h.handleDOMNodeInsertedEvent(me);
                } catch (InterruptedBridgeException ibe) {
                } catch (Exception e) {
                    userAgent.displayError(e);
                }
            }
        }
    }
    protected class DOMNodeRemovedEventListener implements EventListener {
        public DOMNodeRemovedEventListener() {
        }
        public void handleEvent(Event evt) {
            Node node = (Node)evt.getTarget();
            BridgeUpdateHandler h = getBridgeUpdateHandler(node);
            if (h != null) {
                try {
                    h.handleDOMNodeRemovedEvent((MutationEvent)evt);
                } catch (Exception e) {
                    userAgent.displayError(e);
                }
            }
        }
    }
    protected class DOMCharacterDataModifiedEventListener
            implements EventListener {
        public DOMCharacterDataModifiedEventListener() {
        }
        public void handleEvent(Event evt) {
            Node node = (Node)evt.getTarget();
            while (node != null && !(node instanceof SVGOMElement)) {
                node = (Node) ((AbstractNode) node).getParentNodeEventTarget();
            }
            BridgeUpdateHandler h = getBridgeUpdateHandler(node);
            if (h != null) {
                try {
                    h.handleDOMCharacterDataModified((MutationEvent)evt);
                } catch (Exception e) {
                    userAgent.displayError(e);
                }
            }
        }
    }
    protected class CSSPropertiesChangedListener implements CSSEngineListener {
        public CSSPropertiesChangedListener() {
        }
        public void propertiesChanged(CSSEngineEvent evt) {
            Element elem = evt.getElement();
            SVGContext ctx = getSVGContext(elem);
            if (ctx == null) {
                GraphicsNode pgn = getGraphicsNode(elem.getParentNode());
                if ((pgn == null) || !(pgn instanceof CompositeGraphicsNode)) {
                    return;
                }
                CompositeGraphicsNode parent = (CompositeGraphicsNode)pgn;
                int [] properties = evt.getProperties();
                for (int i=0; i < properties.length; ++i) {
                    if (properties[i] == SVGCSSEngine.DISPLAY_INDEX) {
                        if (!CSSUtilities.convertDisplay(elem)) {
                            break;
                        }
                        GVTBuilder builder = getGVTBuilder();
                        GraphicsNode childNode = builder.build
                            (BridgeContext.this, elem);
                        if (childNode == null) {
                            break;
                        }
                        int idx = -1;
                        for(Node ps = elem.getPreviousSibling(); ps != null;
                            ps = ps.getPreviousSibling()) {
                            if (ps.getNodeType() != Node.ELEMENT_NODE)
                                continue;
                            Element pse = (Element)ps;
                            GraphicsNode gn = getGraphicsNode(pse);
                            if (gn == null)
                                continue;
                            idx = parent.indexOf(gn);
                            if (idx == -1)
                                continue;
                            break;
                        }
                        idx++;
                        parent.add(idx, childNode);
                        break;
                    }
                }
            } if (ctx != null && (ctx instanceof BridgeUpdateHandler)) {
                ((BridgeUpdateHandler)ctx).handleCSSEngineEvent(evt);
            }
        }
    }
    protected class AnimatedAttrListener
        implements AnimatedAttributeListener {
        public AnimatedAttrListener() {
        }
        public void animatedAttributeChanged(Element e,
                                             AnimatedLiveAttributeValue alav) {
            BridgeUpdateHandler h = getBridgeUpdateHandler(e);
            if (h != null) {
                try {
                    h.handleAnimatedAttributeChanged(alav);
                } catch (Exception ex) {
                    userAgent.displayError(ex);
                }
            }
        }
        public void otherAnimationChanged(Element e, String type) {
            BridgeUpdateHandler h = getBridgeUpdateHandler(e);
            if (h != null) {
                try {
                    h.handleOtherAnimationChanged(type);
                } catch (Exception ex) {
                    userAgent.displayError(ex);
                }
            }
        }
    }
    public Value getSystemColor(String ident) {
        return SystemColorSupport.getSystemColor(ident);
    }
    public Value getDefaultFontFamily() {
        SVGOMDocument      doc  = (SVGOMDocument)document;
        SVGStylableElement root = (SVGStylableElement)doc.getRootElement();
        String str = userAgent.getDefaultFontFamily();
        return doc.getCSSEngine().parsePropertyValue
            (root,SVGConstants.CSS_FONT_FAMILY_PROPERTY, str);
    }
    public float getLighterFontWeight(float f) {
        return userAgent.getLighterFontWeight(f);
    }
    public float getBolderFontWeight(float f) {
        return userAgent.getBolderFontWeight(f);
    }
    public float getPixelUnitToMillimeter() {
        return userAgent.getPixelUnitToMillimeter();
    }
    public float getPixelToMillimeter() {
        return getPixelUnitToMillimeter();
    }
    public float getMediumFontSize() {
        return userAgent.getMediumFontSize();
    }
    public float getBlockWidth(Element elt) {
        return getViewport(elt).getWidth();
    }
    public float getBlockHeight(Element elt) {
        return getViewport(elt).getHeight();
    }
    public void
        checkLoadExternalResource(ParsedURL resourceURL,
                                  ParsedURL docURL) throws SecurityException {
        userAgent.checkLoadExternalResource(resourceURL,
                                            docURL);
    }
    public boolean isDynamicDocument(Document doc) {
        return BaseScriptingEnvironment.isDynamicDocument(this, doc);
    }
    public boolean isInteractiveDocument(Document doc) {
        Element root = ((SVGDocument)doc).getRootElement();
        if (!SVGConstants.SVG_NAMESPACE_URI.equals(root.getNamespaceURI()))
            return false;
        return checkInteractiveElement(root);
    }
    public boolean checkInteractiveElement(Element e) {
        return checkInteractiveElement
            ((SVGDocument)e.getOwnerDocument(), e);
    }
    public boolean checkInteractiveElement(SVGDocument doc,
                                           Element e) {
        String tag = e.getLocalName();
        if (SVGConstants.SVG_A_TAG.equals(tag))
            return true;
        if (SVGConstants.SVG_TITLE_TAG.equals(tag)) {
            return (e.getParentNode() != doc.getRootElement());
        }
        if (SVGConstants.SVG_DESC_TAG.equals(tag)) {
            return (e.getParentNode() != doc.getRootElement());
        }
        if (SVGConstants.SVG_CURSOR_TAG.equals(tag))
            return true;
        if (e.getAttribute(CSSConstants.CSS_CURSOR_PROPERTY).length() >0)
            return true;
        final String svg_ns = SVGConstants.SVG_NAMESPACE_URI;
        for (Node n = e.getFirstChild();
             n != null;
             n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element)n;
                if (svg_ns.equals(child.getNamespaceURI()))
                    if (checkInteractiveElement(child))
                        return true;
            }
        }
        return false;
    }
    public void setAnimationLimitingNone() {
        animationLimitingMode = 0;
        if (animationEngine != null) {
            setAnimationLimitingMode();
        }
    }
    public void setAnimationLimitingCPU(float pc) {
        animationLimitingMode = 1;
        animationLimitingAmount = pc;
        if (animationEngine != null) {
            setAnimationLimitingMode();
        }
    }
    public void setAnimationLimitingFPS(float fps) {
        animationLimitingMode = 2;
        animationLimitingAmount = fps;
        if (animationEngine != null) {
            setAnimationLimitingMode();
        }
    }
    protected void setAnimationLimitingMode() {
        switch (animationLimitingMode) {
            case 0: 
                animationEngine.setAnimationLimitingNone();
                break;
            case 1: 
                animationEngine.setAnimationLimitingCPU
                    (animationLimitingAmount);
                break;
            case 2: 
                animationEngine.setAnimationLimitingFPS
                    (animationLimitingAmount);
                break;
        }
    }
    protected List extensions = null;
    public void registerSVGBridges() {
        UserAgent ua = getUserAgent();
        List ext = getBridgeExtensions(document);
        Iterator iter = ext.iterator();
        while(iter.hasNext()) {
            BridgeExtension be = (BridgeExtension)iter.next();
            be.registerTags(this);
            ua.registerExtension(be);
        }
    }
    public List getBridgeExtensions(Document doc) {
        Element root = ((SVGOMDocument)doc).getRootElement();
        String ver = root.getAttributeNS
            (null, SVGConstants.SVG_VERSION_ATTRIBUTE);
        BridgeExtension svgBE;
        if ((ver.length()==0) || ver.equals("1.0") || ver.equals("1.1"))
            svgBE = new SVGBridgeExtension();
        else
            svgBE = new SVG12BridgeExtension();
        float priority = svgBE.getPriority();
        extensions = new LinkedList(getGlobalBridgeExtensions());
        ListIterator li = extensions.listIterator();
        for (;;) {
            if (!li.hasNext()) {
                li.add(svgBE);
                break;
            }
            BridgeExtension lbe = (BridgeExtension)li.next();
            if (lbe.getPriority() > priority) {
                li.previous();
                li.add(svgBE);
                break;
            }
        }
        return extensions;
    }
    protected static List globalExtensions = null;
    public static synchronized List getGlobalBridgeExtensions() {
        if (globalExtensions != null) {
            return globalExtensions;
        }
        globalExtensions = new LinkedList();
        Iterator iter = Service.providers(BridgeExtension.class);
        while (iter.hasNext()) {
            BridgeExtension be = (BridgeExtension)iter.next();
            float priority  = be.getPriority();
            ListIterator li = globalExtensions.listIterator();
            for (;;) {
                if (!li.hasNext()) {
                    li.add(be);
                    break;
                }
                BridgeExtension lbe = (BridgeExtension)li.next();
                if (lbe.getPriority() > priority) {
                    li.previous();
                    li.add(be);
                    break;
                }
            }
        }
        return globalExtensions;
    }
    public static class CSSEngineUserAgentWrapper implements CSSEngineUserAgent {
        UserAgent ua;
        CSSEngineUserAgentWrapper(UserAgent ua) {
            this.ua = ua;
        }
        public void displayError(Exception ex) { ua.displayError(ex); }
        public void displayMessage(String message) { ua.displayMessage(message); }
    }
}
