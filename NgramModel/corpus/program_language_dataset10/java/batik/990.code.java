package org.apache.batik.gvt.event;
import java.util.EventListener;
public interface SelectionListener extends EventListener {
    void selectionChanged(SelectionEvent evt);
    void selectionDone(SelectionEvent evt);
    void selectionCleared(SelectionEvent evt);
    void selectionStarted(SelectionEvent evt);
}
