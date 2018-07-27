package org.w3c.dom.events;
import org.w3c.dom.DOMException;
public interface EventTarget {
    public void addEventListener(String type, 
                                 EventListener listener, 
                                 boolean useCapture);
    public void removeEventListener(String type, 
                                    EventListener listener, 
                                    boolean useCapture);
    public boolean dispatchEvent(Event evt)
                                 throws EventException, DOMException;
    public void addEventListenerNS(String namespaceURI, 
                                   String type, 
                                   EventListener listener, 
                                   boolean useCapture, 
                                   Object evtGroup);
    public void removeEventListenerNS(String namespaceURI, 
                                      String type, 
                                      EventListener listener, 
                                      boolean useCapture);
}
