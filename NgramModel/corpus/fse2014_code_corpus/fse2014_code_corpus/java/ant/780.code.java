package org.apache.tools.ant.util;
import java.util.Stack;
public class IdentityStack extends Stack {
    private static final long serialVersionUID = -5555522620060077046L;
    public static IdentityStack getInstance(Stack s) {
        if (s instanceof IdentityStack) {
            return (IdentityStack) s;
        }
        IdentityStack result = new IdentityStack();
        if (s != null) {
            result.addAll(s);
        }
        return result;
    }
    public IdentityStack() {
    }
    public IdentityStack(Object o) {
        super();
        push(o);
    }
    public synchronized boolean contains(Object o) {
        return indexOf(o) >= 0;
    }
    public synchronized int indexOf(Object o, int pos) {
        for (int i = pos; i < size(); i++) {
            if (get(i) == o) {
                return i;
            }
        }
        return -1;
    }
    public synchronized int lastIndexOf(Object o, int pos) {
        for (int i = pos; i >= 0; i--) {
            if (get(i) == o) {
                return i;
            }
        }
        return -1;
    }
}
