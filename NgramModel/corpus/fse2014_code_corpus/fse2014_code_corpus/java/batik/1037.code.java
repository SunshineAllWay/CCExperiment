package org.apache.batik.gvt.renderer;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.ext.awt.geom.RectListManager;
public interface Renderer {
    void setTree(GraphicsNode treeRoot);
    GraphicsNode getTree();
    void repaint(Shape area);
    void repaint(RectListManager areas);
    void setTransform(AffineTransform usr2dev);
    AffineTransform getTransform();
    boolean isDoubleBuffered();
    void setDoubleBuffered(boolean isDoubleBuffered);
    void dispose();
}
