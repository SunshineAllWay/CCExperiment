package org.apache.batik.css.engine.value;
import org.w3c.dom.css.CSSValue;
public class InheritValue extends AbstractValue {
    public static final InheritValue INSTANCE = new InheritValue();
    protected InheritValue() {
    }
    public String getCssText() {
        return "inherit";
    }
    public short getCssValueType() {
        return CSSValue.CSS_INHERIT;
    }
    public String toString() {
        return getCssText();
    }
}
