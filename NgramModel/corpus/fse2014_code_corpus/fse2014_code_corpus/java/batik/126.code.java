package org.apache.batik.bridge;
import org.apache.batik.ext.awt.image.renderable.ClipRable;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;
public interface ClipBridge extends Bridge {
    ClipRable createClip(BridgeContext ctx,
                         Element clipElement,
                         Element clipedElement,
                         GraphicsNode clipedNode);
}
