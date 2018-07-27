package org.apache.batik.bridge;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.jar.Manifest;
import org.apache.batik.dom.AbstractElement;
import org.apache.batik.dom.events.AbstractEvent;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.script.Interpreter;
import org.apache.batik.script.InterpreterException;
import org.apache.batik.script.ScriptEventWrapper;
import org.apache.batik.script.ScriptHandler;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;
import org.w3c.dom.svg.EventListenerInitializer;
public class BaseScriptingEnvironment {
    public static final String INLINE_SCRIPT_DESCRIPTION
        = "BaseScriptingEnvironment.constant.inline.script.description";
    public static final String EVENT_SCRIPT_DESCRIPTION
        = "BaseScriptingEnvironment.constant.event.script.description";
    public static boolean isDynamicDocument(BridgeContext ctx, Document doc) {
        Element elt = doc.getDocumentElement();
        if ((elt != null) &&
            SVGConstants.SVG_NAMESPACE_URI.equals(elt.getNamespaceURI())) {
            if (elt.getAttributeNS
                (null, SVGConstants.SVG_ONABORT_ATTRIBUTE).length() > 0) {
                return true;
            }
            if (elt.getAttributeNS
                (null, SVGConstants.SVG_ONERROR_ATTRIBUTE).length() > 0) {
                return true;
            }
            if (elt.getAttributeNS
                (null, SVGConstants.SVG_ONRESIZE_ATTRIBUTE).length() > 0) {
                return true;
            }
            if (elt.getAttributeNS
                (null, SVGConstants.SVG_ONUNLOAD_ATTRIBUTE).length() > 0) {
                return true;
            }
            if (elt.getAttributeNS
                (null, SVGConstants.SVG_ONSCROLL_ATTRIBUTE).length() > 0) {
                return true;
            }
            if (elt.getAttributeNS
                (null, SVGConstants.SVG_ONZOOM_ATTRIBUTE).length() > 0) {
                return true;
            }
            return isDynamicElement(ctx, doc.getDocumentElement());
        }
        return false;
    }
    public static boolean isDynamicElement(BridgeContext ctx, Element elt) {
        List bridgeExtensions = ctx.getBridgeExtensions(elt.getOwnerDocument());
        return isDynamicElement(elt, ctx, bridgeExtensions);
    }
    public static boolean isDynamicElement
        (Element elt, BridgeContext ctx, List bridgeExtensions) {
        Iterator i = bridgeExtensions.iterator();
        while (i.hasNext()) {
            BridgeExtension bridgeExtension = (BridgeExtension) i.next();
            if (bridgeExtension.isDynamicElement(elt)) {
                return true;
            }
        }
        if (SVGConstants.SVG_NAMESPACE_URI.equals(elt.getNamespaceURI())) {
            if (elt.getAttributeNS
                (null, SVGConstants.SVG_ONKEYUP_ATTRIBUTE).length() > 0) {
                return true;
            }
            if (elt.getAttributeNS
                (null, SVGConstants.SVG_ONKEYDOWN_ATTRIBUTE).length() > 0) {
                return true;
            }
            if (elt.getAttributeNS
                (null, SVGConstants.SVG_ONKEYPRESS_ATTRIBUTE).length() > 0) {
                return true;
            }
            if (elt.getAttributeNS
                (null, SVGConstants.SVG_ONLOAD_ATTRIBUTE).length() > 0) {
                return true;
            }
            if (elt.getAttributeNS
                (null, SVGConstants.SVG_ONERROR_ATTRIBUTE).length() > 0) {
                return true;
            }
            if (elt.getAttributeNS
                (null, SVGConstants.SVG_ONACTIVATE_ATTRIBUTE).length() > 0) {
                return true;
            }
            if (elt.getAttributeNS
                (null, SVGConstants.SVG_ONCLICK_ATTRIBUTE).length() > 0) {
                return true;
            }
            if (elt.getAttributeNS
                (null, SVGConstants.SVG_ONFOCUSIN_ATTRIBUTE).length() > 0) {
                return true;
            }
            if (elt.getAttributeNS
                (null, SVGConstants.SVG_ONFOCUSOUT_ATTRIBUTE).length() > 0) {
                return true;
            }
            if (elt.getAttributeNS
                (null, SVGConstants.SVG_ONMOUSEDOWN_ATTRIBUTE).length() > 0) {
                return true;
            }
            if (elt.getAttributeNS
                (null, SVGConstants.SVG_ONMOUSEMOVE_ATTRIBUTE).length() > 0) {
                return true;
            }
            if (elt.getAttributeNS
                (null, SVGConstants.SVG_ONMOUSEOUT_ATTRIBUTE).length() > 0) {
                return true;
            }
            if (elt.getAttributeNS
                (null, SVGConstants.SVG_ONMOUSEOVER_ATTRIBUTE).length() > 0) {
                return true;
            }
            if (elt.getAttributeNS
                (null, SVGConstants.SVG_ONMOUSEUP_ATTRIBUTE).length() > 0) {
                return true;
            }
        }
        for (Node n = elt.getFirstChild();
             n != null;
             n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (isDynamicElement(ctx, (Element)n)) {
                    return true;
                }
            }
        }
        return false;
    }
    protected static final String EVENT_NAME = "event";
    protected static final String ALTERNATE_EVENT_NAME = "evt";
    protected static final String APPLICATION_ECMASCRIPT =
        "application/ecmascript";
    protected BridgeContext bridgeContext;
    protected UserAgent userAgent;
    protected Document document;
    protected ParsedURL docPURL;
    protected Set languages = new HashSet();
    protected Interpreter interpreter;
    protected Map windowObjects = new HashMap();
    protected WeakHashMap executedScripts = new WeakHashMap();
    public BaseScriptingEnvironment(BridgeContext ctx) {
        bridgeContext = ctx;
        document = ctx.getDocument();
        docPURL = new ParsedURL(((SVGDocument)document).getURL());
        userAgent     = bridgeContext.getUserAgent();
    }
    public org.apache.batik.script.Window getWindow(Interpreter interp,
                                                    String lang) {
        org.apache.batik.script.Window w =
            (org.apache.batik.script.Window) windowObjects.get(interp);
        if (w == null) {
            w = interp == null ? new Window(null, null)
                               : createWindow(interp, lang);
            windowObjects.put(interp, w);
        }
        return w;
    }
    public org.apache.batik.script.Window getWindow() {
        return getWindow(null, null);
    }
    protected org.apache.batik.script.Window createWindow(Interpreter interp,
                                                          String lang) {
        return new Window(interp, lang);
    }
    public Interpreter getInterpreter() {
        if (interpreter != null)
            return interpreter;
        SVGSVGElement root = (SVGSVGElement)document.getDocumentElement();
        String lang = root.getContentScriptType();
        return getInterpreter(lang);
    }
    public Interpreter getInterpreter(String lang) {
        interpreter = bridgeContext.getInterpreter(lang);
        if (interpreter == null) {
            if (languages.contains(lang)) {
                return null;
            }
            languages.add(lang);
            return null;
        }
        if (!languages.contains(lang)) {
            languages.add(lang);
            initializeEnvironment(interpreter, lang);
        }
        return interpreter;
    }
    public void initializeEnvironment(Interpreter interp, String lang) {
        interp.bindObject("window", getWindow(interp, lang));
    }
    public void loadScripts() {
        NodeList scripts = document.getElementsByTagNameNS
            (SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_SCRIPT_TAG);
        int len = scripts.getLength();
        for (int i = 0; i < len; i++) {
            AbstractElement script = (AbstractElement) scripts.item(i);
            loadScript(script);
        }
    }
    protected void loadScript(AbstractElement script) {
        if (executedScripts.containsKey(script)) {
            return;
        }
        {
            Node n = script;
            do {
                n = n.getParentNode();
                if (n == null) {
                    return;
                }
            } while (n.getNodeType() != Node.DOCUMENT_NODE);
        }
        String type = script.getAttributeNS
            (null, SVGConstants.SVG_TYPE_ATTRIBUTE);
        if (type.length() == 0) {
            type = SVGConstants.SVG_SCRIPT_TYPE_DEFAULT_VALUE;
        }
        if (type.equals(SVGConstants.SVG_SCRIPT_TYPE_JAVA)) {
            try {
                String href = XLinkSupport.getXLinkHref(script);
                ParsedURL purl = new ParsedURL(script.getBaseURI(), href);
                checkCompatibleScriptURL(type, purl);
                DocumentJarClassLoader cll;
                URL docURL = null;
                try {
                    docURL = new URL(docPURL.toString());
                } catch (MalformedURLException mue) {
                }
                cll = new DocumentJarClassLoader
                    (new URL(purl.toString()), docURL);
                URL url = cll.findResource("META-INF/MANIFEST.MF");
                if (url == null) {
                    return;
                }
                Manifest man = new Manifest(url.openStream());
                String sh;
                executedScripts.put(script, null);
                sh = man.getMainAttributes().getValue("Script-Handler");
                if (sh != null) {
                    ScriptHandler h;
                    h = (ScriptHandler)cll.loadClass(sh).newInstance();
                    h.run(document, getWindow());
                }
                sh = man.getMainAttributes().getValue("SVG-Handler-Class");
                if (sh != null) {
                    EventListenerInitializer initializer;
                    initializer =
                        (EventListenerInitializer)cll.loadClass(sh).newInstance();
                    getWindow();
                    initializer.initializeEventListeners((SVGDocument)document);
                }
            } catch (Exception e) {
                if (userAgent != null) {
                    userAgent.displayError(e);
                }
            }
            return;
        }
        Interpreter interpreter = getInterpreter(type);
        if (interpreter == null) {
            return;
        }
        try {
            String href = XLinkSupport.getXLinkHref(script);
            String desc = null;
            Reader reader = null;
            if (href.length() > 0) {
                desc = href;
                ParsedURL purl = new ParsedURL(script.getBaseURI(), href);
                checkCompatibleScriptURL(type, purl);
                InputStream is = purl.openStream();
                String mediaType = purl.getContentTypeMediaType();
                String enc = purl.getContentTypeCharset();
                if (enc != null) {
                    try {
                        reader = new InputStreamReader(is, enc);
                    } catch (UnsupportedEncodingException uee) {
                        enc = null;
                    }
                }
                if (reader == null) {
                    if (APPLICATION_ECMASCRIPT.equals(mediaType)) {
                        if (purl.hasContentTypeParameter("version")) {
                            return;
                        }
                        PushbackInputStream pbis =
                            new PushbackInputStream(is, 8);
                        byte[] buf = new byte[4];
                        int read = pbis.read(buf);
                        if (read > 0) {
                            pbis.unread(buf, 0, read);
                            if (read >= 2) {
                                if (buf[0] == (byte)0xff &&
                                        buf[1] == (byte)0xfe) {
                                    if (read >= 4 && buf[2] == 0 &&
                                            buf[3] == 0) {
                                        enc = "UTF32-LE";
                                        pbis.skip(4);
                                    } else {
                                        enc = "UTF-16LE";
                                        pbis.skip(2);
                                    }
                                } else if (buf[0] == (byte)0xfe &&
                                        buf[1] == (byte)0xff) {
                                    enc = "UTF-16BE";
                                    pbis.skip(2);
                                } else if (read >= 3
                                        && buf[0] == (byte)0xef 
                                        && buf[1] == (byte)0xbb
                                        && buf[2] == (byte)0xbf) {
                                    enc = "UTF-8";
                                    pbis.skip(3);
                                } else if (read >= 4 && buf[0] == 0 &&
                                        buf[1] == 0 &&
                                        buf[2] == (byte)0xfe &&
                                        buf[3] == (byte)0xff) {
                                    enc = "UTF-32BE";
                                    pbis.skip(4);
                                }
                            }
                            if (enc == null) {
                                enc = "UTF-8";
                            }
                        }
                        reader = new InputStreamReader(pbis, enc);
                    } else {
                        reader = new InputStreamReader(is);
                    }
                }
            } else {
                checkCompatibleScriptURL(type, docPURL);
                DocumentLoader dl = bridgeContext.getDocumentLoader();
                Element e = script;
                SVGDocument d = (SVGDocument)e.getOwnerDocument();
                int line = dl.getLineNumber(script);
                desc = Messages.formatMessage
                    (INLINE_SCRIPT_DESCRIPTION,
                     new Object [] {d.getURL(),
                                    "<"+script.getNodeName()+">",
                                    new Integer(line)});
                Node n = script.getFirstChild();
                if (n != null) {
                    StringBuffer sb = new StringBuffer();
                    while (n != null) {
                        if (n.getNodeType() == Node.CDATA_SECTION_NODE
                            || n.getNodeType() == Node.TEXT_NODE)
                            sb.append(n.getNodeValue());
                        n = n.getNextSibling();
                    }
                    reader = new StringReader(sb.toString());
                } else {
                    return;
                }
            }
            executedScripts.put(script, null);
            interpreter.evaluate(reader, desc);
        } catch (IOException e) {
            if (userAgent != null) {
                userAgent.displayError(e);
            }
            return;
        } catch (InterpreterException e) {
            System.err.println("InterpExcept: " + e);
            handleInterpreterException(e);
            return;
        } catch (SecurityException e) {
            if (userAgent != null) {
                userAgent.displayError(e);
            }
        }
    }
    protected void checkCompatibleScriptURL(String scriptType,
                                          ParsedURL scriptPURL){
        userAgent.checkLoadScript(scriptType, scriptPURL, docPURL);
    }
    public void dispatchSVGLoadEvent() {
        SVGSVGElement root = (SVGSVGElement)document.getDocumentElement();
        String lang = root.getContentScriptType();
        long documentStartTime = System.currentTimeMillis();
        bridgeContext.getAnimationEngine().start(documentStartTime);
        dispatchSVGLoad(root, true, lang);
    }
    protected void dispatchSVGLoad(Element elt,
                                   boolean checkCanRun,
                                   String lang) {
        for (Node n = elt.getFirstChild();
             n != null;
             n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                dispatchSVGLoad((Element)n, checkCanRun, lang);
            }
        }
        DocumentEvent de = (DocumentEvent)elt.getOwnerDocument();
        AbstractEvent ev = (AbstractEvent) de.createEvent("SVGEvents");
        String type;
        if (bridgeContext.isSVG12()) {
            type = "load";
        } else {
            type = "SVGLoad";
        }
        ev.initEventNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                       type,
                       false,
                       false);
        NodeEventTarget t = (NodeEventTarget)elt;
        final String s =
            elt.getAttributeNS(null, SVGConstants.SVG_ONLOAD_ATTRIBUTE);
        if (s.length() == 0) {
            t.dispatchEvent(ev);
            return;
        }
        final Interpreter interp = getInterpreter();
        if (interp == null) {
            t.dispatchEvent(ev);
            return;
        }
        if (checkCanRun) {
            checkCompatibleScriptURL(lang, docPURL);
            checkCanRun = false; 
        }
        DocumentLoader dl = bridgeContext.getDocumentLoader();
        SVGDocument d = (SVGDocument)elt.getOwnerDocument();
        int line = dl.getLineNumber(elt);
        final String desc = Messages.formatMessage
            (EVENT_SCRIPT_DESCRIPTION,
             new Object [] {d.getURL(),
                            SVGConstants.SVG_ONLOAD_ATTRIBUTE,
                            new Integer(line)});
        EventListener l = new EventListener() {
                public void handleEvent(Event evt) {
                    try {
                        Object event;
                        if (evt instanceof ScriptEventWrapper) {
                            event = ((ScriptEventWrapper) evt).getEventObject();
                        } else {
                            event = evt;
                        }
                        interp.bindObject(EVENT_NAME, event);
                        interp.bindObject(ALTERNATE_EVENT_NAME, event);
                        interp.evaluate(new StringReader(s), desc);
                    } catch (IOException io) {
                    } catch (InterpreterException e) {
                        handleInterpreterException(e);
                    }
                }
            };
        t.addEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, type,
             l, false, null);
        t.dispatchEvent(ev);
        t.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, type,
             l, false);
    }
    protected void dispatchSVGZoomEvent() {
        if (bridgeContext.isSVG12()) {
            dispatchSVGDocEvent("zoom");
        } else {
            dispatchSVGDocEvent("SVGZoom");
        }
    }
    protected void dispatchSVGScrollEvent() {
        if (bridgeContext.isSVG12()) {
            dispatchSVGDocEvent("scroll");
        } else {
            dispatchSVGDocEvent("SVGScroll");
        }
    }
    protected void dispatchSVGResizeEvent() {
        if (bridgeContext.isSVG12()) {
            dispatchSVGDocEvent("resize");
        } else {
            dispatchSVGDocEvent("SVGResize");
        }
    }
    protected void dispatchSVGDocEvent(String eventType) {
        SVGSVGElement root =
            (SVGSVGElement)document.getDocumentElement();
        EventTarget t = root;
        DocumentEvent de = (DocumentEvent)document;
        AbstractEvent ev = (AbstractEvent) de.createEvent("SVGEvents");
        ev.initEventNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                       eventType,
                       false,
                       false);
        t.dispatchEvent(ev);
    }
    protected void handleInterpreterException(InterpreterException ie) {
        if (userAgent != null) {
            Exception ex = ie.getException();
            userAgent.displayError((ex == null) ? ie : ex);
        }
    }
    protected void handleSecurityException(SecurityException se) {
        if (userAgent != null) {
            userAgent.displayError(se);
        }
    }
    protected class Window implements org.apache.batik.script.Window {
        protected Interpreter interpreter;
        protected String language;
        public Window(Interpreter interp, String lang) {
            interpreter = interp;
            language = lang;
        }
        public Object setInterval(final String script, long interval) {
            return null;
        }
        public Object setInterval(final Runnable r, long interval) {
            return null;
        }
        public void clearInterval(Object interval) {
        }
        public Object setTimeout(final String script, long timeout) {
            return null;
        }
        public Object setTimeout(final Runnable r, long timeout) {
            return null;
        }
        public void clearTimeout(Object timeout) {
        }
        public Node parseXML(String text, Document doc) {
            return null;
        }
        public String printNode(Node n) {
            return null;
        }
        public void getURL(String uri, org.apache.batik.script.Window.URLResponseHandler h) {
            getURL(uri, h, "UTF8");
        }
        public void getURL(String uri,
                           org.apache.batik.script.Window.URLResponseHandler h,
                           String enc) {
        }
        public void postURL(String uri, String content,
                            org.apache.batik.script.Window.URLResponseHandler h) {
            postURL(uri, content, h, "text/plain", null);
        }
        public void postURL(String uri, String content,
                            org.apache.batik.script.Window.URLResponseHandler h,
                     String mimeType) {
            postURL(uri, content, h, mimeType, null);
        }
        public void postURL(String uri,
                            String content,
                            org.apache.batik.script.Window.URLResponseHandler h,
                            String mimeType,
                            String fEnc) {
        }
        public void alert(String message) {
        }
        public boolean confirm(String message) {
            return false;
        }
        public String prompt(String message) {
            return null;
        }
        public String prompt(String message, String defVal) {
            return null;
        }
        public BridgeContext getBridgeContext() {
            return bridgeContext;
        }
        public Interpreter getInterpreter() {
            return interpreter;
        }
        public org.w3c.dom.Location getLocation() {
            return null;
        }
        public org.w3c.dom.Window getParent() {
            return null;
        }
    }
}
