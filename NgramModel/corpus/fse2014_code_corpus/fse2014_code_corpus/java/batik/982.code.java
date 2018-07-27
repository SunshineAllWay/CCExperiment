package org.apache.batik.gvt.event;
import java.util.EventListener;
public interface GraphicsNodeKeyListener extends EventListener {
    void keyPressed(GraphicsNodeKeyEvent evt);
    void keyReleased(GraphicsNodeKeyEvent evt);
    void keyTyped(GraphicsNodeKeyEvent evt);
}
