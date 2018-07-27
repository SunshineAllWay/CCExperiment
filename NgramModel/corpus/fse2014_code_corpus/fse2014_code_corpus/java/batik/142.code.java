package org.apache.batik.bridge;
import org.apache.batik.dom.events.DOMUIEvent;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MouseEvent;
public class FocusManager {
    protected EventTarget lastFocusEventTarget;
    protected Document document;
    protected EventListener mouseclickListener;
    protected EventListener domFocusInListener;
    protected EventListener domFocusOutListener;
    protected EventListener mouseoverListener;
    protected EventListener mouseoutListener;
    public FocusManager(Document doc) {
        document = doc;
        addEventListeners(doc);
    }
    protected void addEventListeners(Document doc) {
        NodeEventTarget target = (NodeEventTarget) doc;
        mouseclickListener = new MouseClickTracker();
        target.addEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "click",
             mouseclickListener, true, null);
        mouseoverListener = new MouseOverTracker();
        target.addEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "mouseover",
             mouseoverListener, true, null);
        mouseoutListener = new MouseOutTracker();
        target.addEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "mouseout",
             mouseoutListener, true, null);
        domFocusInListener = new DOMFocusInTracker();
        target.addEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMFocusIn",
             domFocusInListener, true, null);
        domFocusOutListener = new DOMFocusOutTracker();
        target.addEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMFocusOut",
             domFocusOutListener, true, null);
    }
    protected void removeEventListeners(Document doc) {
        NodeEventTarget target = (NodeEventTarget) doc;
        target.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "click",
             mouseclickListener, true);
        target.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "mouseover",
             mouseoverListener, true);
        target.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "mouseout",
             mouseoutListener, true);
        target.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMFocusIn",
             domFocusInListener, true);
        target.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMFocusOut",
             domFocusOutListener, true);
    }
    public EventTarget getCurrentEventTarget() {
        return lastFocusEventTarget;
    }
    public void dispose() {
        if (document == null) return;
        removeEventListeners(document);
        lastFocusEventTarget = null;
        document = null;
    }
    protected class MouseClickTracker implements EventListener {
        public void handleEvent(Event evt) {
            MouseEvent mevt = (MouseEvent)evt;
            fireDOMActivateEvent(evt.getTarget(), mevt.getDetail());
        }
    }
    protected class DOMFocusInTracker implements EventListener {
        public void handleEvent(Event evt) {
            EventTarget newTarget = evt.getTarget();
            if (lastFocusEventTarget != null && 
                lastFocusEventTarget != newTarget) {
                fireDOMFocusOutEvent(lastFocusEventTarget, newTarget);
            }
            lastFocusEventTarget = evt.getTarget();
        }
    }
    protected class DOMFocusOutTracker implements EventListener {
        public DOMFocusOutTracker() {
        }
        public void handleEvent(Event evt) {
            lastFocusEventTarget = null;
        }
    }
    protected class MouseOverTracker implements EventListener {
        public void handleEvent(Event evt) {
            MouseEvent me = (MouseEvent) evt;
            EventTarget target = evt.getTarget();
            EventTarget relatedTarget = me.getRelatedTarget();
            fireDOMFocusInEvent(target, relatedTarget);
        }
    }
    protected class MouseOutTracker implements EventListener {
        public void handleEvent(Event evt) {
            MouseEvent me = (MouseEvent) evt;
            EventTarget target = evt.getTarget();
            EventTarget relatedTarget = me.getRelatedTarget();
            fireDOMFocusOutEvent(target, relatedTarget);
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
        target.dispatchEvent(uiEvt);
    }
    protected void fireDOMActivateEvent(EventTarget target, int detailArg) {
        DocumentEvent docEvt = 
            (DocumentEvent)((Element)target).getOwnerDocument();
        DOMUIEvent uiEvt = (DOMUIEvent)docEvt.createEvent("UIEvents");
        uiEvt.initUIEventNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                            "DOMActivate",
                            true,    
                            true,    
                            null,    
                            0);      
        target.dispatchEvent(uiEvt);
    }
}
