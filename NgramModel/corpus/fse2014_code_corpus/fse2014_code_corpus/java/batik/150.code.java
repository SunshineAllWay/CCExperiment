package org.apache.batik.bridge;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.filter.Mask;
import org.w3c.dom.Element;
public interface MaskBridge extends Bridge {
    Mask createMask(BridgeContext ctx,
                    Element maskElement,
                    Element maskedElement,
                    GraphicsNode maskedNode);
}
