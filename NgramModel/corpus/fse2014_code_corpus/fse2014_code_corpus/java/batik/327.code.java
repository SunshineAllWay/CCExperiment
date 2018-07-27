package org.apache.batik.css.engine.value;
import org.w3c.dom.DOMException;
public class ComputedValue implements Value {
    protected Value cascadedValue;
    protected Value computedValue;
    public ComputedValue(Value cv) {
        cascadedValue = cv;
    }
    public Value getComputedValue() {
        return computedValue;
    }
    public Value getCascadedValue() {
        return cascadedValue;
    }
    public void setComputedValue(Value v) {
        computedValue = v;
    }
    public String getCssText() {
        return computedValue.getCssText();
    }
    public short getCssValueType() {
        return computedValue.getCssValueType();
    }
    public short getPrimitiveType() {
        return computedValue.getPrimitiveType();
    }
    public float getFloatValue() throws DOMException {
        return computedValue.getFloatValue();
    }
    public String getStringValue() throws DOMException {
        return computedValue.getStringValue();
    }
    public Value getRed() throws DOMException {
        return computedValue.getRed();
    }
    public Value getGreen() throws DOMException {
        return computedValue.getGreen();
    }
    public Value getBlue() throws DOMException {
        return computedValue.getBlue();
    }
    public int getLength() throws DOMException {
        return computedValue.getLength();
    }
    public Value item(int index) throws DOMException {
        return computedValue.item(index);
    }
    public Value getTop() throws DOMException {
        return computedValue.getTop();
    }
    public Value getRight() throws DOMException {
        return computedValue.getRight();
    }
    public Value getBottom() throws DOMException {
        return computedValue.getBottom();
    }
    public Value getLeft() throws DOMException {
        return computedValue.getLeft();
    }
    public String getIdentifier() throws DOMException {
        return computedValue.getIdentifier();
    }
    public String getListStyle() throws DOMException {
        return computedValue.getListStyle();
    }
    public String getSeparator() throws DOMException {
        return computedValue.getSeparator();
    }
}
