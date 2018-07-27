package org.apache.batik.gvt.event;
import java.util.EventListener;
public interface GraphicsNodeMouseListener extends EventListener {
    void mouseClicked(GraphicsNodeMouseEvent evt);
    void mousePressed(GraphicsNodeMouseEvent evt);
    void mouseReleased(GraphicsNodeMouseEvent evt);
    void mouseEntered(GraphicsNodeMouseEvent evt);
    void mouseExited(GraphicsNodeMouseEvent evt);
    void mouseDragged(GraphicsNodeMouseEvent evt);
    void mouseMoved(GraphicsNodeMouseEvent evt);
}
