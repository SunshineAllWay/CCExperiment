package org.apache.batik.css.engine.value;
import org.apache.batik.css.engine.CSSEngine;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
public interface ShorthandManager {
    String getPropertyName();
    boolean isAnimatableProperty();
    boolean isAdditiveProperty();
    void setValues(CSSEngine eng,
                   PropertyHandler ph,
                   LexicalUnit lu,
                   boolean imp)
        throws DOMException;
    interface PropertyHandler {
        void property(String name, LexicalUnit value,
                             boolean important);
    }
}
