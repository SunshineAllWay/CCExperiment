package org.apache.batik.anim.timing;
import org.apache.batik.dom.events.DOMKeyEvent;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.KeyboardEvent;
public class AccesskeyTimingSpecifier
        extends EventLikeTimingSpecifier
        implements EventListener {
    protected char accesskey;
    protected boolean isSVG12AccessKey;
    protected String keyName;
    public AccesskeyTimingSpecifier(TimedElement owner, boolean isBegin,
                                    float offset, char accesskey) {
        super(owner, isBegin, offset);
        this.accesskey = accesskey;
    }
    public AccesskeyTimingSpecifier(TimedElement owner, boolean isBegin,
                                    float offset, String keyName) {
        super(owner, isBegin, offset);
        this.isSVG12AccessKey = true;
        this.keyName = keyName;
    }
    public String toString() {
        if (isSVG12AccessKey) {
            return "accessKey(" + keyName + ")"
                + (offset != 0 ? super.toString() : "");
        }
        return "accesskey(" + accesskey + ")"
            + (offset != 0 ? super.toString() : "");
    }
    public void initialize() {
        if (isSVG12AccessKey) {
            NodeEventTarget eventTarget =
                (NodeEventTarget) owner.getRootEventTarget();
            eventTarget.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "keydown",
                 this, false, null);
        } else {
            EventTarget eventTarget = owner.getRootEventTarget();
            eventTarget.addEventListener("keypress", this, false);
        }
    }
    public void deinitialize() {
        if (isSVG12AccessKey) {
            NodeEventTarget eventTarget =
                (NodeEventTarget) owner.getRootEventTarget();
            eventTarget.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "keydown",
                 this, false);
        } else {
            EventTarget eventTarget = owner.getRootEventTarget();
            eventTarget.removeEventListener("keypress", this, false);
        }
    }
    public void handleEvent(Event e) {
        boolean matched;
        if (e.getType().charAt(3) == 'p') {
            DOMKeyEvent evt = (DOMKeyEvent) e;
            matched = evt.getCharCode() == accesskey;
        } else {
            KeyboardEvent evt = (KeyboardEvent) e;
            matched = evt.getKeyIdentifier().equals(keyName);
        }
        if (matched) {
            owner.eventOccurred(this, e);
        }
    }
    public void resolve(Event e) {
        float time = owner.getRoot().convertEpochTime(e.getTimeStamp());
        InstanceTime instance = new InstanceTime(this, time + offset, true);
        owner.addInstanceTime(instance, isBegin);
    }
}
