package org.apache.batik.dom.svg;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.anim.AnimationTarget;
public interface AnimatedLiveAttributeValue extends LiveAttributeValue {
    String getNamespaceURI();
    String getLocalName();
    AnimatableValue getUnderlyingValue(AnimationTarget target);
    void addAnimatedAttributeListener(AnimatedAttributeListener aal);
    void removeAnimatedAttributeListener(AnimatedAttributeListener aal);
}
