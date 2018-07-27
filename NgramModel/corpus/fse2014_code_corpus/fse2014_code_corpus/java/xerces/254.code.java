package org.apache.xerces.dom.events;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;
public class EventImpl implements Event {
    public String type = null;
    public EventTarget target;
    public EventTarget currentTarget;
    public short eventPhase;
    public boolean initialized = false, bubbles = true, cancelable = false;
    public boolean stopPropagation = false, preventDefault = false;
    protected long timeStamp = System.currentTimeMillis();
    public void initEvent(String eventTypeArg, boolean canBubbleArg, 
            boolean cancelableArg) {
        type = eventTypeArg;
        bubbles = canBubbleArg;
        cancelable = cancelableArg;
        initialized = true;
    }
    public boolean getBubbles() {
        return bubbles;
    }
    public boolean getCancelable() {
        return cancelable;
    }
    public EventTarget getCurrentTarget() {
        return currentTarget;
    }
    public short getEventPhase() {
        return eventPhase;
    }
    public EventTarget getTarget() {
        return target;
    }
    public String getType() {
        return type;
    }
    public long getTimeStamp() {
        return timeStamp;
    }
    public void stopPropagation() {
        stopPropagation = true;
    }
    public void preventDefault() {
        preventDefault = true;
    }
}
