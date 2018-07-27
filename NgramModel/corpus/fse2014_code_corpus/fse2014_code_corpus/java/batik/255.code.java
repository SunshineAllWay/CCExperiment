package org.apache.batik.bridge.svg12;
import org.apache.batik.bridge.FocusManager;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.events.AbstractEvent;
import org.apache.batik.dom.events.DOMUIEvent;
import org.apache.batik.dom.events.EventSupport;
import org.apache.batik.dom.svg12.XBLEventSupport;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;
public class SVG12FocusManager extends FocusManager {
    public SVG12FocusManager(Document doc) {
        super(doc);
    }
    protected void addEventListeners(Document doc) {
        AbstractNode n = (AbstractNode) doc;
        XBLEventSupport es = (XBLEventSupport) n.initializeEventSupport();
        mouseclickListener = new MouseClickTracker();
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "click",
             mouseclickListener, true);
        mouseoverListener = new MouseOverTracker();
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "mouseover",
             mouseoverListener, true);
        mouseoutListener = new MouseOutTracker();
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "mouseout",
             mouseoutListener, true);
        domFocusInListener = new DOMFocusInTracker();
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMFocusIn",
             domFocusInListener, true);
        domFocusOutListener = new DOMFocusOutTracker();
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMFocusOut",
             domFocusOutListener, true);
    }
    protected void removeEventListeners(Document doc) {
        AbstractNode n = (AbstractNode) doc;
        XBLEventSupport es = (XBLEventSupport) n.getEventSupport();
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "click",
             mouseclickListener, true);
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "mouseover",
             mouseoverListener, true);
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "mouseout",
             mouseoutListener, true);
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMFocusIn",
             domFocusInListener, true);
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMFocusOut",
             domFocusOutListener, true);
    }
    protected class MouseClickTracker extends FocusManager.MouseClickTracker {
        public void handleEvent(Event evt) {
            super.handleEvent(EventSupport.getUltimateOriginalEvent(evt));
        }
    }
    protected class DOMFocusInTracker extends FocusManager.DOMFocusInTracker {
        public void handleEvent(Event evt) {
            super.handleEvent(EventSupport.getUltimateOriginalEvent(evt));
        }
    }
    protected class MouseOverTracker extends FocusManager.MouseOverTracker {
        public void handleEvent(Event evt) {
            super.handleEvent(EventSupport.getUltimateOriginalEvent(evt));
        }
    }
    protected class MouseOutTracker extends FocusManager.MouseOutTracker {
        public void handleEvent(Event evt) {
            super.handleEvent(EventSupport.getUltimateOriginalEvent(evt));
        }
    }
    protected void fireDOMFocusInEvent(EventTarget target,
                                       EventTarget relatedTarget) {
        DocumentEvent docEvt = 
            (DocumentEvent)((Element)target).getOwnerDocument();
        DOMUIEvent uiEvt = (DOMUIEvent)docEvt.createEvent("UIEvents");
        uiEvt.initUIEventNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                            "DOMFocusIn",
                            true,
                            false,  
                            null,   
                            0);     
        int limit = DefaultXBLManager.computeBubbleLimit((Node) relatedTarget,
                                                         (Node) target);
        ((AbstractEvent) uiEvt).setBubbleLimit(limit);
        target.dispatchEvent(uiEvt);
    }
    protected void fireDOMFocusOutEvent(EventTarget target,
                                        EventTarget relatedTarget) {
        DocumentEvent docEvt = 
            (DocumentEvent)((Element)target).getOwnerDocument();
        DOMUIEvent uiEvt = (DOMUIEvent)docEvt.createEvent("UIEvents");
        uiEvt.initUIEventNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                            "DOMFocusOut",
                            true,
                            false,  
                            null,   
                            0);     
        int limit = DefaultXBLManager.computeBubbleLimit((Node) target,
                                                         (Node) relatedTarget);
        ((AbstractEvent) uiEvt).setBubbleLimit(limit);
        target.dispatchEvent(uiEvt);
    }
}
