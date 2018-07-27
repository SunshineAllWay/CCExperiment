package org.apache.batik.gvt;
import org.apache.batik.gvt.event.GraphicsNodeChangeListener;
import org.apache.batik.gvt.event.GraphicsNodeKeyListener;
import org.apache.batik.gvt.event.GraphicsNodeMouseListener;
import org.apache.batik.gvt.event.SelectionListener;
public interface Selector extends GraphicsNodeMouseListener,
                                  GraphicsNodeKeyListener,
                                  GraphicsNodeChangeListener {
    Object getSelection();
    boolean isEmpty();
    void addSelectionListener(SelectionListener l);
    void removeSelectionListener(SelectionListener l);
}
