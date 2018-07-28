package org.apache.batik.swing.svg;
public interface SVGDocumentLoaderListener {
    void documentLoadingStarted(SVGDocumentLoaderEvent e);
    void documentLoadingCompleted(SVGDocumentLoaderEvent e);
    void documentLoadingCancelled(SVGDocumentLoaderEvent e);
    void documentLoadingFailed(SVGDocumentLoaderEvent e);
}
