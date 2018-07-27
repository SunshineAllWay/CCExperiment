package org.w3c.dom.events;
import org.w3c.dom.views.AbstractView;
public interface MouseEvent extends UIEvent {
    public int getScreenX();
    public int getScreenY();
    public int getClientX();
    public int getClientY();
    public boolean getCtrlKey();
    public boolean getShiftKey();
    public boolean getAltKey();
    public boolean getMetaKey();
    public short getButton();
    public EventTarget getRelatedTarget();
    public void initMouseEvent(String typeArg, 
                               boolean canBubbleArg, 
                               boolean cancelableArg, 
                               AbstractView viewArg, 
                               int detailArg, 
                               int screenXArg, 
                               int screenYArg, 
                               int clientXArg, 
                               int clientYArg, 
                               boolean ctrlKeyArg, 
                               boolean altKeyArg, 
                               boolean shiftKeyArg, 
                               boolean metaKeyArg, 
                               short buttonArg, 
                               EventTarget relatedTargetArg);
    public boolean getModifierState(String keyIdentifierArg);
    public void initMouseEventNS(String namespaceURI, 
                                 String typeArg, 
                                 boolean canBubbleArg, 
                                 boolean cancelableArg, 
                                 AbstractView viewArg, 
                                 int detailArg, 
                                 int screenXArg, 
                                 int screenYArg, 
                                 int clientXArg, 
                                 int clientYArg, 
                                 short buttonArg, 
                                 EventTarget relatedTargetArg, 
                                 String modifiersList);
}
