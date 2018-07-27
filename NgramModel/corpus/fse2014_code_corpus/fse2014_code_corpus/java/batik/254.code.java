package org.apache.batik.bridge.svg12;
import org.apache.batik.bridge.BridgeUpdateHandler;
import org.w3c.dom.Element;
public interface SVG12BridgeUpdateHandler extends BridgeUpdateHandler {
    void handleBindingEvent(Element bindableElement, Element shadowTree);
    void handleContentSelectionChangedEvent(ContentSelectionChangedEvent csce);
}
