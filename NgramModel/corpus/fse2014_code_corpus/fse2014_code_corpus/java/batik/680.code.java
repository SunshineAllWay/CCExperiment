package org.apache.batik.dom.svg12;
import java.util.HashSet;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.events.AbstractEvent;
import org.apache.batik.dom.events.EventListenerList;
import org.apache.batik.dom.events.EventSupport;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.util.HashTable;
import org.apache.batik.dom.xbl.NodeXBL;
import org.apache.batik.dom.xbl.ShadowTreeEvent;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventException;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.MutationEvent;
public class XBLEventSupport extends EventSupport {
    protected HashTable capturingImplementationListeners;
    protected HashTable bubblingImplementationListeners;
    protected static HashTable eventTypeAliases = new HashTable();
    static {
        eventTypeAliases.put("SVGLoad",   "load");
        eventTypeAliases.put("SVGUnoad",  "unload");
        eventTypeAliases.put("SVGAbort",  "abort");
        eventTypeAliases.put("SVGError",  "error");
        eventTypeAliases.put("SVGResize", "resize");
        eventTypeAliases.put("SVGScroll", "scroll");
        eventTypeAliases.put("SVGZoom",   "zoom");
    }
    public XBLEventSupport(AbstractNode n) {
        super(n);
    }
    public void addEventListenerNS(String namespaceURI,
                                   String type,
                                   EventListener listener,
                                   boolean useCapture,
                                   Object group) {
        super.addEventListenerNS
            (namespaceURI, type, listener, useCapture, group);
        if (namespaceURI == null
                || namespaceURI.equals(XMLConstants.XML_EVENTS_NAMESPACE_URI)) {
            String alias = (String) eventTypeAliases.get(type);
            if (alias != null) {
                super.addEventListenerNS
                    (namespaceURI, alias, listener, useCapture, group);
            }
        }
    }
    public void removeEventListenerNS(String namespaceURI,
                                      String type,
                                      EventListener listener,
                                      boolean useCapture) {
        super.removeEventListenerNS(namespaceURI, type, listener, useCapture);
        if (namespaceURI == null
                || namespaceURI.equals(XMLConstants.XML_EVENTS_NAMESPACE_URI)) {
            String alias = (String) eventTypeAliases.get(type);
            if (alias != null) {
                super.removeEventListenerNS
                    (namespaceURI, alias, listener, useCapture);
            }
        }
    }
    public void addImplementationEventListenerNS(String namespaceURI,
                                                 String type,
                                                 EventListener listener,
                                                 boolean useCapture) {
        HashTable listeners;
        if (useCapture) {
            if (capturingImplementationListeners == null) {
                capturingImplementationListeners = new HashTable();
            }
            listeners = capturingImplementationListeners;
        } else {
            if (bubblingImplementationListeners == null) {
                bubblingImplementationListeners = new HashTable();
            }
            listeners = bubblingImplementationListeners;
        }
        EventListenerList list = (EventListenerList) listeners.get(type);
        if (list == null) {
            list = new EventListenerList();
            listeners.put(type, list);
        }
        list.addListener(namespaceURI, null, listener);
    }
    public void removeImplementationEventListenerNS(String namespaceURI,
                                                    String type,
                                                    EventListener listener,
                                                    boolean useCapture) {
        HashTable listeners = useCapture ? capturingImplementationListeners
                                         : bubblingImplementationListeners;
        if (listeners == null) {
            return;
        }
        EventListenerList list = (EventListenerList) listeners.get(type);
        if (list == null) {
            return;
        }
        list.removeListener(namespaceURI, listener);
        if (list.size() == 0) {
            listeners.remove(type);
        }
    }
    public void moveEventListeners(EventSupport other) {
        super.moveEventListeners(other);
        XBLEventSupport es = (XBLEventSupport) other;
        es.capturingImplementationListeners = capturingImplementationListeners;
        es.bubblingImplementationListeners = bubblingImplementationListeners;
        capturingImplementationListeners = null;
        bubblingImplementationListeners = null;
    }
    public boolean dispatchEvent(NodeEventTarget target, Event evt) 
            throws EventException {
        if (evt == null) {
            return false;
        }
        if (!(evt instanceof AbstractEvent)) {
            throw createEventException
                (DOMException.NOT_SUPPORTED_ERR,
                 "unsupported.event",
                 new Object[] {});
        }
        AbstractEvent e = (AbstractEvent) evt;
        String type = e.getType();
        if (type == null || type.length() == 0) {
            throw createEventException
                (EventException.UNSPECIFIED_EVENT_TYPE_ERR,
                 "unspecified.event",
                 new Object[] {});
        }
        setTarget(e, target);
        stopPropagation(e, false);
        stopImmediatePropagation(e, false);
        preventDefault(e, false);
        NodeEventTarget[] ancestors = getAncestors(target);
        int bubbleLimit = e.getBubbleLimit();
        int minAncestor = 0;
        if (isSingleScopeEvent(e)) {
            AbstractNode targetNode = (AbstractNode) target;
            Node boundElement = targetNode.getXblBoundElement();
            if (boundElement != null) {
                minAncestor = ancestors.length;
                while (minAncestor > 0) {
                    AbstractNode ancestorNode =
                        (AbstractNode) ancestors[minAncestor - 1];
                    if (ancestorNode.getXblBoundElement() != boundElement) {
                        break;
                    }
                    minAncestor--;
                }
            }
        } else if (bubbleLimit != 0) {
            minAncestor = ancestors.length - bubbleLimit + 1;
            if (minAncestor < 0) {
                minAncestor = 0;
            }
        }
        AbstractEvent[] es = getRetargettedEvents(target, ancestors, e);
        boolean preventDefault = false;
        HashSet stoppedGroups = new HashSet();
        HashSet toBeStoppedGroups = new HashSet();
        for (int i = 0; i < minAncestor; i++) {
            NodeEventTarget node = ancestors[i];
            setCurrentTarget(es[i], node);
            setEventPhase(es[i], Event.CAPTURING_PHASE);
            fireImplementationEventListeners(node, es[i], true);
        }
        for (int i = minAncestor; i < ancestors.length; i++) {
            NodeEventTarget node = ancestors[i];
            setCurrentTarget(es[i], node);
            setEventPhase(es[i], Event.CAPTURING_PHASE);
            fireImplementationEventListeners(node, es[i], true);
            fireEventListeners(node, es[i], true, stoppedGroups,
                               toBeStoppedGroups);
            fireHandlerGroupEventListeners(node, es[i], true, stoppedGroups,
                                           toBeStoppedGroups);
            preventDefault = preventDefault || es[i].getDefaultPrevented();
            stoppedGroups.addAll(toBeStoppedGroups);
            toBeStoppedGroups.clear();
        }
        setEventPhase(e, Event.AT_TARGET);
        setCurrentTarget(e, target);
        fireImplementationEventListeners(target, e, false);
        fireEventListeners(target, e, false, stoppedGroups,
                           toBeStoppedGroups);
        fireHandlerGroupEventListeners(node, e, false, stoppedGroups,
                                       toBeStoppedGroups);
        stoppedGroups.addAll(toBeStoppedGroups);
        toBeStoppedGroups.clear();
        preventDefault = preventDefault || e.getDefaultPrevented();
        if (e.getBubbles()) {
            for (int i = ancestors.length - 1; i >= minAncestor; i--) {
                NodeEventTarget node = ancestors[i];
                setCurrentTarget(es[i], node);
                setEventPhase(es[i], Event.BUBBLING_PHASE);
                fireImplementationEventListeners(node, es[i], false);
                fireEventListeners(node, es[i], false, stoppedGroups,
                                   toBeStoppedGroups);
                fireHandlerGroupEventListeners
                    (node, es[i], false, stoppedGroups, toBeStoppedGroups);
                preventDefault =
                    preventDefault || es[i].getDefaultPrevented();
                stoppedGroups.addAll(toBeStoppedGroups);
                toBeStoppedGroups.clear();
            }
            for (int i = minAncestor - 1; i >= 0; i--) {
                NodeEventTarget node = ancestors[i];
                setCurrentTarget(es[i], node);
                setEventPhase(es[i], Event.BUBBLING_PHASE);
                fireImplementationEventListeners(node, es[i], false);
                preventDefault =
                    preventDefault || es[i].getDefaultPrevented();
            }
        }
        if (!preventDefault) {
            runDefaultActions(e);
        }
        return preventDefault;
    }
    protected void fireHandlerGroupEventListeners(NodeEventTarget node, 
                                                  AbstractEvent e,
                                                  boolean useCapture,
                                                  HashSet stoppedGroups,
                                                  HashSet toBeStoppedGroups) {
        NodeList defs = ((NodeXBL) node).getXblDefinitions();
        for (int j = 0; j < defs.getLength(); j++) {
            Node n = defs.item(j).getFirstChild();
            while (n != null &&
                    !(n instanceof XBLOMHandlerGroupElement)) {
                n = n.getNextSibling();
            }
            if (n == null) {
                continue;
            }
            node = (NodeEventTarget) n;
            String type = e.getType();
            EventSupport support = node.getEventSupport();
            if (support == null) {
                continue;
            }
            EventListenerList list = support.getEventListeners(type, useCapture);
            if (list == null) {
                return;
            }
            EventListenerList.Entry[] listeners = list.getEventListeners();
            fireEventListeners(node, e, listeners, stoppedGroups,
                               toBeStoppedGroups);
        }
    }
    protected boolean isSingleScopeEvent(Event evt) {
        return evt instanceof MutationEvent
            || evt instanceof ShadowTreeEvent;
    }
    protected AbstractEvent[] getRetargettedEvents(NodeEventTarget target,
                                                   NodeEventTarget[] ancestors,
                                                   AbstractEvent e) {
        boolean singleScope = isSingleScopeEvent(e);
        AbstractNode targetNode = (AbstractNode) target;
        AbstractEvent[] es = new AbstractEvent[ancestors.length];
        if (ancestors.length > 0) {
            int index = ancestors.length - 1;
            Node boundElement = targetNode.getXblBoundElement();
            AbstractNode ancestorNode = (AbstractNode) ancestors[index];
            if (!singleScope &&
                    ancestorNode.getXblBoundElement() != boundElement) {
                es[index] = retargetEvent(e, ancestors[index]);
            } else {
                es[index] = e;
            }
            while (--index >= 0) {
                ancestorNode = (AbstractNode) ancestors[index + 1];
                boundElement = ancestorNode.getXblBoundElement();
                AbstractNode nextAncestorNode = (AbstractNode) ancestors[index];
                Node nextBoundElement = nextAncestorNode.getXblBoundElement();
                if (!singleScope && nextBoundElement != boundElement) {
                    es[index] = retargetEvent(es[index + 1], ancestors[index]);
                } else {
                    es[index] = es[index + 1];
                }
            }
        }
        return es;
    }
    protected AbstractEvent retargetEvent(AbstractEvent e,
                                          NodeEventTarget target) {
        AbstractEvent clonedEvent = e.cloneEvent();
        setTarget(clonedEvent, target);
        return clonedEvent;
    }
    public EventListenerList getImplementationEventListeners
            (String type, boolean useCapture) {
        HashTable listeners = useCapture ? capturingImplementationListeners
                                         : bubblingImplementationListeners;
        if (listeners == null) {
            return null;
        }
        return (EventListenerList) listeners.get(type);
    }
    protected void fireImplementationEventListeners(NodeEventTarget node, 
                                                    AbstractEvent e,
                                                    boolean useCapture) {
        String type = e.getType();
        XBLEventSupport support = (XBLEventSupport) node.getEventSupport();
        if (support == null) {
            return;
        }
        EventListenerList list =
            support.getImplementationEventListeners(type, useCapture);
        if (list == null) {
            return;
        }
        EventListenerList.Entry[] listeners = list.getEventListeners();
        fireEventListeners(node, e, listeners, null, null);
    }
}
