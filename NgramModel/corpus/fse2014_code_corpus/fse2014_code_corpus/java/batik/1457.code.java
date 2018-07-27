package org.w3c.dom.events;
public interface Event {
    public static final short CAPTURING_PHASE           = 1;
    public static final short AT_TARGET                 = 2;
    public static final short BUBBLING_PHASE            = 3;
    public String getType();
    public EventTarget getTarget();
    public EventTarget getCurrentTarget();
    public short getEventPhase();
    public boolean getBubbles();
    public boolean getCancelable();
    public long getTimeStamp();
    public void stopPropagation();
    public void preventDefault();
    public void initEvent(String eventTypeArg, 
                          boolean canBubbleArg, 
                          boolean cancelableArg);
    public String getNamespaceURI();
    public void stopImmediatePropagation();
    public boolean getDefaultPrevented();
    public void initEventNS(String namespaceURIArg, 
                            String eventTypeArg, 
                            boolean canBubbleArg, 
                            boolean cancelableArg);
}
