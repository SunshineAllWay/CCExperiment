package org.apache.batik.gvt;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.gvt.event.GraphicsNodeChangeListener;
public class RootGraphicsNode extends CompositeGraphicsNode {
    List treeGraphicsNodeChangeListeners = null;
    public RootGraphicsNode() {}
    public RootGraphicsNode getRoot() {
        return this;
    }
    public List getTreeGraphicsNodeChangeListeners() {
        if (treeGraphicsNodeChangeListeners == null) {
            treeGraphicsNodeChangeListeners = new LinkedList();
        }
        return treeGraphicsNodeChangeListeners;
    }
    public void addTreeGraphicsNodeChangeListener
        (GraphicsNodeChangeListener l) {
        getTreeGraphicsNodeChangeListeners().add(l);
    }
    public void removeTreeGraphicsNodeChangeListener
        (GraphicsNodeChangeListener l) {
        getTreeGraphicsNodeChangeListeners().remove(l);
    }
}
