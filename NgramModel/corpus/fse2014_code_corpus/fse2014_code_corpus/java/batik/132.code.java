package org.apache.batik.bridge;
import org.apache.batik.gvt.RootGraphicsNode;
import org.w3c.dom.Document;
public interface DocumentBridge extends Bridge {
    RootGraphicsNode createGraphicsNode(BridgeContext ctx, Document doc);
    void buildGraphicsNode(BridgeContext ctx, Document doc, RootGraphicsNode node);
}
