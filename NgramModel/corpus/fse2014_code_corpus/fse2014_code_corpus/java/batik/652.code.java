package org.apache.batik.dom.svg;
import java.util.List;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.Element;
public interface SVGSVGContext extends SVGContext {
    List getIntersectionList(SVGRect svgRect, Element end);
    List getEnclosureList(SVGRect rect, Element end);
    boolean checkIntersection(Element element, SVGRect rect);
    boolean checkEnclosure(Element element, SVGRect rect);
    void deselectAll();
    int suspendRedraw(int max_wait_milliseconds);
    boolean unsuspendRedraw(int suspend_handle_id);
    void unsuspendRedrawAll();
    void forceRedraw();
    void pauseAnimations();
    void unpauseAnimations();
    boolean animationsPaused();
    float getCurrentTime();
    void setCurrentTime(float t);
}
