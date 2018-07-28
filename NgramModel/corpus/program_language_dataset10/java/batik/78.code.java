package org.apache.batik.apps.svgbrowser;
import org.apache.batik.swing.gvt.Overlay;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
public interface DOMViewerController {
    void performUpdate(Runnable r);
    ElementOverlayManager createSelectionManager();
    void removeSelectionOverlay(Overlay selectionOverlay);
    Document getDocument();
    void selectNode(Node node);
    boolean canEdit();
}
