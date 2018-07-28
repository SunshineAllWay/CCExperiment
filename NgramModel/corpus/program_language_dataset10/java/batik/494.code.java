package org.apache.batik.dom.events;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.util.HashTable;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventException;
import org.w3c.dom.events.EventListener;
public class EventSupport {
    protected HashTable capturingListeners;
    protected HashTable bubblingListeners;
    protected AbstractNode node;
    public EventSupport(AbstractNode n) {
        node = n;
    }
    public void addEventListener(String type, EventListener listener,
                                 boolean useCapture) {
        addEventListenerNS(null, type, listener, useCapture, null);
    }
    public void addEventListenerNS(String namespaceURI,
                                   String type,
                                   EventListener listener,
                                   boolean useCapture,
                                   Object group) {
        HashTable listeners;
        if (useCapture) {
            if (capturingListeners == null) {
                capturingListeners = new HashTable();
            }
            listeners = capturingListeners;
        } else {
            if (bubblingListeners == null) {
                bubblingListeners = new HashTable();
            }
            listeners = bubblingListeners;
        }
        EventListenerList list = (EventListenerList) listeners.get(type);
        if (list == null) {
            list = new EventListenerList();
            listeners.put(type, list);
        }
        list.addListener(namespaceURI, group, listener);
    }
    public void removeEventListener(String type, EventListener listener,
                                    boolean useCapture) {
        removeEventListenerNS(null, type, listener, useCapture);
    }
    public void removeEventListenerNS(String namespaceURI,
                                      String type,
                                      EventListener listener,
                                      boolean useCapture) {
        HashTable listeners;
        if (useCapture) {
            listeners = capturingListeners;
        } else {
            listeners = bubblingListeners;
        }
        if (listeners == null) {
            return;
        }
        EventListenerList list = (EventListenerList) listeners.get(type);
        if (list != null) {
            list.removeListener(namespaceURI, listener);
            if (list.size() == 0) {
                listeners.remove(type);
            }
        }
    }
    public void moveEventListeners(EventSupport other) {
        other.capturingListeners = capturingListeners;
        other.bubblingListeners = bubblingListeners;
        capturingListeners = null;
        bubblingListeners = null;
    }
    public boolean dispatchEvent(NodeEventTarget target, Event evt)
            throws EventException {
        if (evt == null) {
            return false;
        }
        if (!(evt instanceof AbstractEvent)) {
            throw createEventException(DOMException.NOT_SUPPORTED_ERR,
                                       "unsupported.event",
                                       new Object[] { });
        }
        AbstractEvent e = (AbstractEvent) evt;
        String type = e.getType();
        if (type == null || type.length() == 0) {
            throw createEventException
                (EventException.UNSPECIFIED_EVENT_TYPE_ERR,
                 "unspecified.event",
                 new Object[] {});
        }
        e.setTarget(target);
        e.stopPropagation(false);
        e.stopImmediatePropagation(false);
        e.preventDefault(false);
        NodeEventTarget[] ancestors = getAncestors(target);
        e.setEventPhase(Event.CAPTURING_PHASE);
        HashSet stoppedGroups = new HashSet();
        HashSet toBeStoppedGroups = new HashSet();
        for (int i = 0; i < ancestors.length; i++) {
            NodeEventTarget node = ancestors[i];
            e.setCurrentTarget(node);
            fireEventListeners(node, e, true, stoppedGroups,
                               toBeStoppedGroups);
            stoppedGroups.addAll(toBeStoppedGroups);
            toBeStoppedGroups.clear();
        }
        e.setEventPhase(Event.AT_TARGET);
        e.setCurrentTarget(target);
        fireEventListeners(target, e, false, stoppedGroups,
                           toBeStoppedGroups);
        stoppedGroups.addAll(toBeStoppedGroups);
        toBeStoppedGroups.clear();
        if (e.getBubbles()) {
            e.setEventPhase(Event.BUBBLING_PHASE);
            for (int i = ancestors.length - 1; i >= 0; i--) {
                NodeEventTarget node = ancestors[i];
                e.setCurrentTarget(node);
                fireEventListeners(node, e, false, stoppedGroups,
                                   toBeStoppedGroups);
                stoppedGroups.addAll(toBeStoppedGroups);
                toBeStoppedGroups.clear();
            }
        }
        if (!e.getDefaultPrevented()) {
            runDefaultActions(e);
        }
        return e.getDefaultPrevented();
    }
    protected void runDefaultActions(AbstractEvent e) {
        List runables = e.getDefaultActions();
        if (runables != null) {
            Iterator i = runables.iterator();
            while (i.hasNext()) {
                Runnable r = (Runnable)i.next();
                r.run();
            }
        }
    }
    protected void fireEventListeners(NodeEventTarget node,
                                      AbstractEvent e,
                                      EventListenerList.Entry[] listeners,
                                      HashSet stoppedGroups,
                                      HashSet toBeStoppedGroups) {
        if (listeners == null) {
            return;
        }
        String eventNS = e.getNamespaceURI();
        for (int i = 0; i < listeners.length; i++) {
            try {
                String listenerNS = listeners[i].getNamespaceURI();
                if (listenerNS != null && eventNS != null
                        && !listenerNS.equals(eventNS)) {
                    continue;
                }
                Object group = listeners[i].getGroup();
                if (stoppedGroups == null || !stoppedGroups.contains(group)) {
                    listeners[i].getListener().handleEvent(e);
                    if (e.getStopImmediatePropagation()) {
                        if (stoppedGroups != null) {
                            stoppedGroups.add(group);
                        }
                        e.stopImmediatePropagation(false);
                    } else if (e.getStopPropagation()) {
                        if (toBeStoppedGroups != null) {
                            toBeStoppedGroups.add(group);
                        }
                        e.stopPropagation(false);
                    }
                }
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }
    protected void fireEventListeners(NodeEventTarget node,
                                      AbstractEvent e,
                                      boolean useCapture,
                                      HashSet stoppedGroups,
                                      HashSet toBeStoppedGroups) {
        String type = e.getType();
        EventSupport support = node.getEventSupport();
        if (support == null) {
            return;
        }
        EventListenerList list = support.getEventListeners(type, useCapture);
        if (list == null) {
            return;
        }
        EventListenerList.Entry[] listeners = list.getEventListeners();
        fireEventListeners(node, e, listeners, stoppedGroups,
                           toBeStoppedGroups);
    }
    protected NodeEventTarget[] getAncestors(NodeEventTarget node) {
        node = node.getParentNodeEventTarget(); 
        int nancestors = 0;
        for (NodeEventTarget n = node;
             n != null;
             n = n.getParentNodeEventTarget(), nancestors++) {
        }
        NodeEventTarget[] ancestors = new NodeEventTarget[nancestors];
        for (int i = nancestors - 1;
             i >= 0;
             --i, node = node.getParentNodeEventTarget()) {
            ancestors[i] = node;
        }
        return ancestors;
    }
    public boolean hasEventListenerNS(String namespaceURI, String type) {
        if (capturingListeners != null) {
            EventListenerList ell
                = (EventListenerList) capturingListeners.get(type);
            if (ell != null) {
                if (ell.hasEventListener(namespaceURI)) {
                    return true;
                }
            }
        }
        if (bubblingListeners != null) {
            EventListenerList ell
                = (EventListenerList) capturingListeners.get(type);
            if (ell != null) {
                return ell.hasEventListener(namespaceURI);
            }
        }
        return false;
    }
    public EventListenerList getEventListeners(String type,
                                               boolean useCapture) {
        HashTable listeners
            = useCapture ? capturingListeners : bubblingListeners;
        if (listeners == null) {
            return null;
        }
        return (EventListenerList) listeners.get(type);
    }
    protected EventException createEventException(short code,
                                                  String key,
                                                  Object[] args) {
        try {
            AbstractDocument doc = (AbstractDocument) node.getOwnerDocument();
            return new EventException(code, doc.formatMessage(key, args));
        } catch (Exception e) {
            return new EventException(code, key);
        }
    }
    protected void setTarget(AbstractEvent e, NodeEventTarget target) {
        e.setTarget(target);
    }
    protected void stopPropagation(AbstractEvent e, boolean b) {
        e.stopPropagation(b);
    }
    protected void stopImmediatePropagation(AbstractEvent e, boolean b) {
        e.stopImmediatePropagation(b);
    }
    protected void preventDefault(AbstractEvent e, boolean b) {
        e.preventDefault(b);
    }
    protected void setCurrentTarget(AbstractEvent e, NodeEventTarget target) {
        e.setCurrentTarget(target);
    }
    protected void setEventPhase(AbstractEvent e, short phase) {
        e.setEventPhase(phase);
    }
    public static Event getUltimateOriginalEvent(Event evt) {
        AbstractEvent e = (AbstractEvent) evt;
        for (;;) {
            AbstractEvent origEvt = (AbstractEvent) e.getOriginalEvent();
            if (origEvt == null) {
                break;
            }
            e = origEvt;
        }
        return e;
    }
}
