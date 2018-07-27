package org.apache.batik.dom.svg;
import org.apache.batik.dom.anim.AnimationTargetListener;
public interface SVGAnimationTargetContext extends SVGContext {
    void addTargetListener(String pn, AnimationTargetListener l);
    void removeTargetListener(String pn, AnimationTargetListener l);
}
