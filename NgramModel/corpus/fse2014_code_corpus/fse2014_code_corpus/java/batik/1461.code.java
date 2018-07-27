package org.w3c.dom.events;
import org.w3c.dom.views.AbstractView;
public interface KeyboardEvent extends UIEvent {
    public static final int DOM_KEY_LOCATION_STANDARD = 0x00;
    public static final int DOM_KEY_LOCATION_LEFT     = 0x01;
    public static final int DOM_KEY_LOCATION_RIGHT    = 0x02;
    public static final int DOM_KEY_LOCATION_NUMPAD   = 0x03;
    public String getKeyIdentifier();
    public int getKeyLocation();
    public boolean getCtrlKey();
    public boolean getShiftKey();
    public boolean getAltKey();
    public boolean getMetaKey();
    public boolean getModifierState(String keyIdentifierArg);
    public void initKeyboardEvent(String typeArg, 
                                  boolean canBubbleArg, 
                                  boolean cancelableArg, 
                                  AbstractView viewArg, 
                                  String keyIdentifierArg, 
                                  int keyLocationArg, 
                                  String modifiersList);
    public void initKeyboardEventNS(String namespaceURI, 
                                    String typeArg, 
                                    boolean canBubbleArg, 
                                    boolean cancelableArg, 
                                    AbstractView viewArg, 
                                    String keyIdentifierArg, 
                                    int keyLocationArg, 
                                    String modifiersList);
}
