package org.apache.batik.dom.svg;
import org.w3c.dom.Element;
public interface AnimatedAttributeListener {
    void animatedAttributeChanged(Element e, AnimatedLiveAttributeValue alav);
    void otherAnimationChanged(Element e, String type);
}
