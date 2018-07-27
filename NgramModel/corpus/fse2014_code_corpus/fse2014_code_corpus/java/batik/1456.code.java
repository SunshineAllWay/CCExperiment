package org.w3c.dom.events;
import org.w3c.dom.DOMException;
public interface DocumentEvent {
    public Event createEvent(String eventType)
                             throws DOMException;
    public boolean canDispatch(String namespaceURI, 
                               String type);
}
