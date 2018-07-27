package org.apache.batik.bridge.svg12;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.Messages;
import org.apache.batik.bridge.ScriptingEnvironment;
import org.apache.batik.bridge.SVGUtilities;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.AbstractElement;
import org.apache.batik.dom.events.EventSupport;
import org.apache.batik.dom.svg12.SVGGlobal;
import org.apache.batik.dom.svg12.XBLEventSupport;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.dom.util.TriplyIndexedTable;
import org.apache.batik.script.Interpreter;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.SVG12Constants;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.EventListener;
public class SVG12ScriptingEnvironment extends ScriptingEnvironment {
    public static final String HANDLER_SCRIPT_DESCRIPTION
        = "SVG12ScriptingEnvironment.constant.handler.script.description";
    public SVG12ScriptingEnvironment(BridgeContext ctx) {
        super(ctx);
    }
    protected TriplyIndexedTable handlerScriptingListeners;
    protected void addDocumentListeners() {
        domNodeInsertedListener = new DOMNodeInsertedListener();
        domNodeRemovedListener = new DOMNodeRemovedListener();
        domAttrModifiedListener = new DOMAttrModifiedListener();
        AbstractDocument doc = (AbstractDocument) document;
        XBLEventSupport es = (XBLEventSupport) doc.initializeEventSupport();
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeInserted",
             domNodeInsertedListener, false);
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeRemoved",
             domNodeRemovedListener, false);
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMAttrModified",
             domAttrModifiedListener, false);
    }
    protected void removeDocumentListeners() {
        AbstractDocument doc = (AbstractDocument) document;
        XBLEventSupport es = (XBLEventSupport) doc.initializeEventSupport();
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeInserted",
             domNodeInsertedListener, false);
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeRemoved",
             domNodeRemovedListener, false);
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMAttrModified",
             domAttrModifiedListener, false);
    }
    protected class DOMNodeInsertedListener
            extends ScriptingEnvironment.DOMNodeInsertedListener {
        public void handleEvent(Event evt) {
            super.handleEvent(EventSupport.getUltimateOriginalEvent(evt));
        }
    }
    protected class DOMNodeRemovedListener
            extends ScriptingEnvironment.DOMNodeRemovedListener {
        public void handleEvent(Event evt) {
            super.handleEvent(EventSupport.getUltimateOriginalEvent(evt));
        }
    }
    protected class DOMAttrModifiedListener
            extends ScriptingEnvironment.DOMAttrModifiedListener {
        public void handleEvent (Event evt) {
            super.handleEvent(EventSupport.getUltimateOriginalEvent(evt));
        }
    }
    protected void addScriptingListenersOn(Element elt) {
        String eltNS = elt.getNamespaceURI();
        String eltLN = elt.getLocalName();
        if (SVGConstants.SVG_NAMESPACE_URI.equals(eltNS)
                && SVG12Constants.SVG_HANDLER_TAG.equals(eltLN)) {
            AbstractElement tgt = (AbstractElement) elt.getParentNode();
            String eventType = elt.getAttributeNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI,
                 XMLConstants.XML_EVENTS_EVENT_ATTRIBUTE);
            String eventNamespaceURI = XMLConstants.XML_EVENTS_NAMESPACE_URI;
            if (eventType.indexOf(':') != -1) {
                String prefix = DOMUtilities.getPrefix(eventType);
                eventType = DOMUtilities.getLocalName(eventType);
                eventNamespaceURI
                    = ((AbstractElement) elt).lookupNamespaceURI(prefix);
            }
            EventListener listener = new HandlerScriptingEventListener
                (eventNamespaceURI, eventType, (AbstractElement) elt);
            tgt.addEventListenerNS
                (eventNamespaceURI, eventType, listener, false, null);
            if (handlerScriptingListeners == null) {
                handlerScriptingListeners = new TriplyIndexedTable();
            }
            handlerScriptingListeners.put
                (eventNamespaceURI, eventType, elt, listener);
        }
        super.addScriptingListenersOn(elt);
    }
    protected void removeScriptingListenersOn(Element elt) {
        String eltNS = elt.getNamespaceURI();
        String eltLN = elt.getLocalName();
        if (SVGConstants.SVG_NAMESPACE_URI.equals(eltNS)
                && SVG12Constants.SVG_HANDLER_TAG.equals(eltLN)) {
            AbstractElement tgt = (AbstractElement) elt.getParentNode();
            String eventType = elt.getAttributeNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI,
                 XMLConstants.XML_EVENTS_EVENT_ATTRIBUTE);
            String eventNamespaceURI = XMLConstants.XML_EVENTS_NAMESPACE_URI;
            if (eventType.indexOf(':') != -1) {
                String prefix = DOMUtilities.getPrefix(eventType);
                eventType = DOMUtilities.getLocalName(eventType);
                eventNamespaceURI
                    = ((AbstractElement) elt).lookupNamespaceURI(prefix);
            }
            EventListener listener =
                (EventListener) handlerScriptingListeners.put
                    (eventNamespaceURI, eventType, elt, null);
            tgt.removeEventListenerNS
                (eventNamespaceURI, eventType, listener, false);
        }
        super.removeScriptingListenersOn(elt);
    }
    protected class HandlerScriptingEventListener implements EventListener {
        protected String eventNamespaceURI;
        protected String eventType;
        protected AbstractElement handlerElement;
        public HandlerScriptingEventListener(String ns,
                                             String et,
                                             AbstractElement e) {
            eventNamespaceURI = ns;
            eventType = et;
            handlerElement = e;
        }
        public void handleEvent(Event evt) {
            Element elt = (Element)evt.getCurrentTarget();
            String script = handlerElement.getTextContent();
            if (script.length() == 0)
                return;
            DocumentLoader dl = bridgeContext.getDocumentLoader();
            AbstractDocument d
                = (AbstractDocument) handlerElement.getOwnerDocument();
            int line = dl.getLineNumber(handlerElement);
            final String desc = Messages.formatMessage
                (HANDLER_SCRIPT_DESCRIPTION,
                 new Object [] {d.getDocumentURI(),
                                eventNamespaceURI,
                                eventType,
                                new Integer(line)});
            String lang = handlerElement.getAttributeNS
                (null, SVGConstants.SVG_CONTENT_SCRIPT_TYPE_ATTRIBUTE);
            if (lang.length() == 0) {
                Element e = elt;
                while (e != null &&
                       (!SVGConstants.SVG_NAMESPACE_URI.equals
                        (e.getNamespaceURI()) ||
                        !SVGConstants.SVG_SVG_TAG.equals(e.getLocalName()))) {
                    e = SVGUtilities.getParentElement(e);
                }
                if (e == null)
                    return;
                lang = e.getAttributeNS
                    (null, SVGConstants.SVG_CONTENT_SCRIPT_TYPE_ATTRIBUTE);
            }
            runEventHandler(script, evt, lang, desc);
        }
    }
    public org.apache.batik.script.Window createWindow(Interpreter interp,
                                                       String lang) {
        return new Global(interp, lang);
    }
    protected class Global
            extends ScriptingEnvironment.Window
            implements SVGGlobal  {
        public Global(Interpreter interp, String lang) {
            super(interp, lang);
        }
        public void startMouseCapture(EventTarget target, boolean sendAll,
                                      boolean autoRelease) {
            ((SVG12BridgeContext) bridgeContext.getPrimaryBridgeContext())
                .startMouseCapture(target, sendAll, autoRelease);
        }
        public void stopMouseCapture() {
            ((SVG12BridgeContext) bridgeContext.getPrimaryBridgeContext())
                .stopMouseCapture();
        }
    }
}
