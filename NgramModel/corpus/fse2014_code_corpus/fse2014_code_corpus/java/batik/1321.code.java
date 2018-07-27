package org.apache.batik.swing.svg;
public interface SVGLoadEventDispatcherListener {
    void svgLoadEventDispatchStarted(SVGLoadEventDispatcherEvent e);
    void svgLoadEventDispatchCompleted(SVGLoadEventDispatcherEvent e);
    void svgLoadEventDispatchCancelled(SVGLoadEventDispatcherEvent e);
    void svgLoadEventDispatchFailed(SVGLoadEventDispatcherEvent e);
}
