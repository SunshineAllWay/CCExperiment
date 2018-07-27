package org.apache.batik.gvt.event;
import org.apache.batik.gvt.GraphicsNode;
public class GraphicsNodeFocusEvent extends GraphicsNodeEvent {
    static final int FOCUS_FIRST = 1004;
    public static final int FOCUS_GAINED = FOCUS_FIRST;
    public static final int FOCUS_LOST = FOCUS_FIRST + 1;
    public GraphicsNodeFocusEvent(GraphicsNode source, int id) {
        super(source, id);
    }
}
