package org.apache.batik.dom.svg12;
import org.w3c.dom.events.EventTarget;
public interface SVGGlobal extends Global {
    void startMouseCapture(EventTarget target, boolean sendAll,
                           boolean autoRelease);
    void stopMouseCapture();
}
