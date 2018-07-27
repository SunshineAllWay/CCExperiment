package org.apache.batik.gvt.filter;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.gvt.GraphicsNode;
public interface GraphicsNodeRable extends Filter {
    GraphicsNode getGraphicsNode();
    void setGraphicsNode(GraphicsNode node);
    boolean getUsePrimitivePaint();
    void setUsePrimitivePaint(boolean usePrimitivePaint);
}
