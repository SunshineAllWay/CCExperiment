package org.w3c.dom.events;
import org.w3c.dom.views.AbstractView;
public interface TextEvent extends UIEvent {
    public String getData();
    public void initTextEvent(String typeArg, 
                              boolean canBubbleArg, 
                              boolean cancelableArg, 
                              AbstractView viewArg, 
                              String dataArg);
    public void initTextEventNS(String namespaceURI, 
                                String type, 
                                boolean canBubbleArg, 
                                boolean cancelableArg, 
                                AbstractView viewArg, 
                                String dataArg);
}
