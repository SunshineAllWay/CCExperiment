package org.apache.batik.dom.anim;
public interface AnimationTargetListener {
    void baseValueChanged(AnimationTarget t, String ns, String ln,
                          boolean isCSS);
}
