package org.apache.batik.css.engine.value;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
public class FloatValue extends AbstractValue {
    public static String getCssText(short unit, float value) {
        if (unit < 0 || unit >= UNITS.length) {
            throw new DOMException(DOMException.SYNTAX_ERR, "");
        }
        String s = String.valueOf(value);
        if (s.endsWith(".0")) {
            s = s.substring(0, s.length() - 2);
        }
        return s + UNITS[unit - CSSPrimitiveValue.CSS_NUMBER];
    }
    protected static final String[] UNITS = {
        "", "%", "em", "ex", "px", "cm", "mm", "in", "pt",
        "pc", "deg", "rad", "grad", "ms", "s", "Hz", "kHz", ""
    };
    protected float floatValue;
    protected short unitType;
    public FloatValue(short unitType, float floatValue) {
        this.unitType   = unitType;
        this.floatValue = floatValue;
    }
    public short getPrimitiveType() {
        return unitType;
    }
    public float getFloatValue() {
        return floatValue;
    }
    public String getCssText() {
        return getCssText(unitType, floatValue);
    }
    public String toString() {
        return getCssText();
    }
}
