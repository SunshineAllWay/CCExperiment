package org.apache.batik.swing.gvt;
import java.awt.event.ComponentEvent;
public interface JGVTComponentListener {
    int COMPONENT_TRANSFORM_CHANGED =
        ComponentEvent.COMPONENT_LAST+1234;
    void componentTransformChanged
        (ComponentEvent event);
}
