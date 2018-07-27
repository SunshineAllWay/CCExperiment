package org.w3c.dom.events;
public interface CustomEvent extends Event {
    public Object getDetail();
    public void initCustomEventNS(String namespaceURI, 
                                  String typeArg, 
                                  boolean canBubbleArg, 
                                  boolean cancelableArg, 
                                  Object detailArg);
}
