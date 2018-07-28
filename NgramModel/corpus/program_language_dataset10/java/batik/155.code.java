package org.apache.batik.bridge;
import java.awt.Paint;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;
public interface PaintBridge extends Bridge {
    Paint createPaint(BridgeContext ctx,
                      Element paintElement,
                      Element paintedElement,
                      GraphicsNode paintedNode,
                      float opacity);
}
