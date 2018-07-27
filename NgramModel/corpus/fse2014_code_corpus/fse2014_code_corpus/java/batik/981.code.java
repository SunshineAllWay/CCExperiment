package org.apache.batik.gvt.event;
import org.apache.batik.gvt.GraphicsNode;
public class GraphicsNodeKeyEvent extends GraphicsNodeInputEvent {
    static final int KEY_FIRST = 400;
    public static final int KEY_TYPED = KEY_FIRST;
    public static final int KEY_PRESSED = 1 + KEY_FIRST;
    public static final int KEY_RELEASED = 2 + KEY_FIRST;
    protected int keyCode;
    protected char keyChar;
    protected int keyLocation;
    public GraphicsNodeKeyEvent(GraphicsNode source, int id,
                                long when, int modifiers, int lockState,
                                int keyCode, char keyChar, int keyLocation) {
        super(source, id, when, modifiers, lockState);
        this.keyCode = keyCode;
        this.keyChar = keyChar;
        this.keyLocation = keyLocation;
    }
    public int getKeyCode() {
        return keyCode;
    }
    public char getKeyChar() {
        return keyChar;
    }
    public int getKeyLocation() {
        return keyLocation;
    }
}
