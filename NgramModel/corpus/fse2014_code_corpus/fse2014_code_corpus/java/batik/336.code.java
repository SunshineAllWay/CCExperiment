package org.apache.batik.css.engine.value;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
public class RGBColorValue extends AbstractValue {
    protected Value red;
    protected Value green;
    protected Value blue;
    public RGBColorValue(Value r, Value g, Value b) {
        red = r;
        green = g;
        blue = b;
    }
    public short getPrimitiveType() {
        return CSSPrimitiveValue.CSS_RGBCOLOR;
    }
    public String getCssText() {
        return "rgb(" +
            red.getCssText() + ", " +
            green.getCssText() + ", " +
            blue.getCssText() + ')';
    }
    public Value getRed() throws DOMException {
        return red;
    }
    public Value getGreen() throws DOMException {
        return green;
    }
    public Value getBlue() throws DOMException {
        return blue;
    }
    public String toString() {
        return getCssText();
    }
}
