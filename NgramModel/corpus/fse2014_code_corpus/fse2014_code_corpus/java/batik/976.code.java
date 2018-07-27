package org.apache.batik.gvt.event;
import java.util.EventObject;
import org.apache.batik.gvt.GraphicsNode;
public class GraphicsNodeEvent extends EventObject {
    private boolean consumed = false;
    protected int id;
    public GraphicsNodeEvent(GraphicsNode source, int id) {
        super(source);
        this.id = id;
    }
    public int getID() {
        return id;
    }
    public GraphicsNode getGraphicsNode() {
        return (GraphicsNode) source;
    }
    public void consume() {
        consumed = true;
    }
    public boolean isConsumed() {
        return consumed;
    }
}
