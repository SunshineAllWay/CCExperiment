package org.apache.batik.bridge;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;
public interface GraphicsNodeBridge extends Bridge {
    GraphicsNode createGraphicsNode(BridgeContext ctx, Element e);
    void buildGraphicsNode(BridgeContext ctx, Element e, GraphicsNode node);
    boolean isComposite();
    boolean getDisplay(Element e);
    Bridge getInstance();
}
