package org.apache.batik.gvt.event;
import org.apache.batik.gvt.GraphicsNode;
public class GraphicsNodeChangeEvent extends GraphicsNodeEvent {
    static final int CHANGE_FIRST = 9800;
    public static final int CHANGE_STARTED = CHANGE_FIRST;
    public static final int CHANGE_COMPLETED = CHANGE_FIRST+1;
    protected GraphicsNode changeSource;
    public GraphicsNodeChangeEvent(GraphicsNode source, int id) {
        super(source, id);
    }
    public void setChangeSrc(GraphicsNode gn) { this.changeSource = gn; }
    public GraphicsNode getChangeSrc() { return changeSource; }
}
