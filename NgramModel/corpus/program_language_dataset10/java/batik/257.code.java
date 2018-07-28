package org.apache.batik.bridge.svg12;
import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.SVGTextElementBridge;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.svg12.XBLEventSupport;
import org.apache.batik.dom.events.EventSupport;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.xbl.NodeXBL;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.MutationEvent;
public class SVG12TextElementBridge
        extends SVGTextElementBridge
        implements SVG12BridgeUpdateHandler {
    public Bridge getInstance() {
        return new SVG12TextElementBridge();
    }
    protected void addTextEventListeners(BridgeContext ctx, NodeEventTarget e) {
        if (childNodeRemovedEventListener == null) {
            childNodeRemovedEventListener =
                new DOMChildNodeRemovedEventListener();
        }
        if (subtreeModifiedEventListener == null) {
            subtreeModifiedEventListener =
                new DOMSubtreeModifiedEventListener();
        }
        SVG12BridgeContext ctx12 = (SVG12BridgeContext) ctx;
        AbstractNode n = (AbstractNode) e;
        XBLEventSupport evtSupport =
            (XBLEventSupport) n.initializeEventSupport();
        evtSupport.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMNodeRemoved",
             childNodeRemovedEventListener, true);
        ctx12.storeImplementationEventListenerNS
            (e, XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMNodeRemoved",
             childNodeRemovedEventListener, true);
        evtSupport.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMSubtreeModified",
             subtreeModifiedEventListener, false);
        ctx12.storeImplementationEventListenerNS
            (e, XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMSubtreeModified",
             subtreeModifiedEventListener, false);
    }
    protected void removeTextEventListeners(BridgeContext ctx,
                                            NodeEventTarget e) {
        AbstractNode n = (AbstractNode) e;
        XBLEventSupport evtSupport =
            (XBLEventSupport) n.initializeEventSupport();
        evtSupport.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMNodeRemoved",
             childNodeRemovedEventListener, true);
        evtSupport.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMSubtreeModified",
             subtreeModifiedEventListener, false);
    }
    protected class DOMChildNodeRemovedEventListener
            extends SVGTextElementBridge.DOMChildNodeRemovedEventListener {
        public void handleEvent(Event evt) {
            super.handleEvent(EventSupport.getUltimateOriginalEvent(evt));
        }
    }
    protected class DOMSubtreeModifiedEventListener
            extends SVGTextElementBridge.DOMSubtreeModifiedEventListener {
        public void handleEvent(Event evt) {
            super.handleEvent(EventSupport.getUltimateOriginalEvent(evt));
        }
    }
    protected Node getFirstChild(Node n) {
        return ((NodeXBL) n).getXblFirstChild();
    }
    protected Node getNextSibling(Node n) {
        return ((NodeXBL) n).getXblNextSibling();
    }
    protected Node getParentNode(Node n) {
        return ((NodeXBL) n).getXblParentNode();
    }
    public void handleDOMCharacterDataModified(MutationEvent evt) {
        Node childNode = (Node)evt.getTarget();
        if (isParentDisplayed(childNode)) {
            if (getParentNode(childNode) != childNode.getParentNode()) {
                computeLaidoutText(ctx, e, node);
            } else {
                laidoutText = null;
            }
        }
    }
    public void handleBindingEvent(Element bindableElement,
                                   Element shadowTree) {
    }
    public void handleContentSelectionChangedEvent
            (ContentSelectionChangedEvent csce) {
        computeLaidoutText(ctx, e, node);
    }
}
