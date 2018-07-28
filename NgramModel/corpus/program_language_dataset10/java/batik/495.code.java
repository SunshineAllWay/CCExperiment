package org.apache.batik.dom.events;
import org.w3c.dom.DOMException;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventException;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
public interface NodeEventTarget extends EventTarget {
    EventSupport getEventSupport();
    NodeEventTarget getParentNodeEventTarget();
    boolean dispatchEvent(Event evt) throws EventException, DOMException;
    void addEventListenerNS(String namespaceURI,
                            String type,
                            EventListener listener,
                            boolean useCapture,
                            Object evtGroup);
    void removeEventListenerNS(String namespaceURI,
                               String type,
                               EventListener listener,
                               boolean useCapture);
}
