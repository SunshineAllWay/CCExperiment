package org.apache.batik.dom.events;
import org.apache.batik.dom.xbl.OriginalEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;
import java.util.ArrayList;
import java.util.List;
public abstract class AbstractEvent
        implements Event, OriginalEvent, Cloneable {
    protected String type;
    protected boolean isBubbling;
    protected boolean cancelable;
    protected EventTarget currentTarget;
    protected EventTarget target;
    protected short eventPhase;
    protected long timeStamp = System.currentTimeMillis();
    protected boolean stopPropagation = false;
    protected boolean stopImmediatePropagation = false;
    protected boolean preventDefault = false;
    protected String namespaceURI;
    protected Event originalEvent;
    protected List defaultActions;
    protected int bubbleLimit = 0;
    public String getType() {
        return type;
    }
    public EventTarget getCurrentTarget() {
        return currentTarget;
    }
    public EventTarget getTarget() {
        return target;
    }
    public short getEventPhase() {
        return eventPhase;
    }
    public boolean getBubbles() {
        return isBubbling;
    }
    public boolean getCancelable() {
        return cancelable;
    }
    public long getTimeStamp() {
        return timeStamp;
    }
    public String getNamespaceURI() {
        return namespaceURI;
    }
    public Event getOriginalEvent() {
        return originalEvent;
    }
    public void stopPropagation() {
        this.stopPropagation = true;
    }
    public void preventDefault() {
        this.preventDefault = true;
    }
    public boolean getDefaultPrevented() {
        return preventDefault;
    }
    public List getDefaultActions() { return defaultActions; }
    public void addDefaultAction(Runnable rable) { 
        if (defaultActions == null) defaultActions = new ArrayList();
        defaultActions.add(rable); 
    }
    public void stopImmediatePropagation() {
        this.stopImmediatePropagation = true;
    }
    public void initEvent(String eventTypeArg, 
                          boolean canBubbleArg, 
                          boolean cancelableArg) {
        this.type = eventTypeArg;
        this.isBubbling = canBubbleArg;
        this.cancelable = cancelableArg;
    }
    public void initEventNS(String namespaceURIArg,
                            String eventTypeArg,
                            boolean canBubbleArg,
                            boolean cancelableArg) {
        if (namespaceURI != null && namespaceURI.length() == 0) {
            namespaceURI = null;
        }
        namespaceURI = namespaceURIArg;
        type = eventTypeArg;
        isBubbling = canBubbleArg;
        cancelable = cancelableArg;
    }
    boolean getStopPropagation() {
        return stopPropagation;
    }
    boolean getStopImmediatePropagation() {
        return stopImmediatePropagation;
    }
    void setEventPhase(short eventPhase) {
        this.eventPhase = eventPhase;
    }
    void stopPropagation(boolean state) {
        this.stopPropagation = state;
    }
    void stopImmediatePropagation(boolean state) {
        this.stopImmediatePropagation = state;
    }
    void preventDefault(boolean state) {
        this.preventDefault = state;
    }
    void setCurrentTarget(EventTarget currentTarget) {
        this.currentTarget = currentTarget;
    }
    void setTarget(EventTarget target) {
        this.target = target;
    }
    public Object clone() throws CloneNotSupportedException {
        AbstractEvent newEvent = (AbstractEvent) super.clone();
        newEvent.timeStamp = System.currentTimeMillis();
        return newEvent;
    }
    public AbstractEvent cloneEvent() {
        try {
            AbstractEvent newEvent = (AbstractEvent) clone();
            newEvent.originalEvent = this;
            return newEvent;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
    public int getBubbleLimit() {
        return bubbleLimit;
    }
    public void setBubbleLimit(int n) {
        bubbleLimit = n;
    }
}
