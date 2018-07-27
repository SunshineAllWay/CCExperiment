package org.apache.batik.dom.svg;
import org.w3c.dom.svg.SVGNumber;
public abstract class AbstractSVGNumber implements SVGNumber {
    protected float value;
    public float getValue() {
        return value;
    }
    public void setValue(float f) {
        value = f;
    }
}
