package org.apache.batik.css.engine.value;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSValue;
public abstract class AbstractValue implements Value {
    public short getCssValueType() {
        return CSSValue.CSS_PRIMITIVE_VALUE;
    }
    public short getPrimitiveType() {
        throw createDOMException();
    }
    public float getFloatValue() throws DOMException {
        throw createDOMException();
    }
    public String getStringValue() throws DOMException {
        throw createDOMException();
    }
    public Value getRed() throws DOMException {
        throw createDOMException();
    }
    public Value getGreen() throws DOMException {
        throw createDOMException();
    }
    public Value getBlue() throws DOMException {
        throw createDOMException();
    }
    public int getLength() throws DOMException {
        throw createDOMException();
    }
    public Value item(int index) throws DOMException {
        throw createDOMException();
    }
    public Value getTop() throws DOMException {
        throw createDOMException();
    }
    public Value getRight() throws DOMException {
        throw createDOMException();
    }
    public Value getBottom() throws DOMException {
        throw createDOMException();
    }
    public Value getLeft() throws DOMException {
        throw createDOMException();
    }
    public String getIdentifier() throws DOMException {
        throw createDOMException();
    }
    public String getListStyle() throws DOMException {
        throw createDOMException();
    }
    public String getSeparator() throws DOMException {
        throw createDOMException();
    }
    protected DOMException createDOMException() {
        Object[] p = new Object[] { new Integer(getCssValueType()) };
        String s = Messages.formatMessage("invalid.value.access", p);
        return new DOMException(DOMException.INVALID_ACCESS_ERR, s);
    }
}
